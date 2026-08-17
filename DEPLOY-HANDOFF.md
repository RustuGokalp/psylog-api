# Backend Production Deploy — Frontend Devir Notu

Bu dosya `psylog-web` (Next.js frontend) tarafındaki bir Claude oturumu tarafından yazıldı.
Amaç: `psylog-api`'yi canlıya çıkarırken frontend'in beklentilerini gözetmek.

**Hedef domain:** `https://psktugcetekin.com` (frontend, Vercel)
**Frontend deploy durumu:** Henüz çıkılmadı. Backend canlıya çıktıktan sonra çıkılacak.
**Sıralama kararı:** Backend + DB önce. Frontend'in tüm public sayfaları `Dynamic` render — backend olmadan site boş görünür ve Google boş sayfaları indeksler.

---

## 1. En kritik karar: domain topolojisi

**Backend'i `api.psktugcetekin.com` alt alan adına bağla.** Railway/Render'ın verdiği `*.railway.app` gibi bir adreste bırakma.

Sebebi teknik ve zorunlu:

Frontend'de `src/middleware.ts` admin route'larını korurken **`token` adlı cookie'yi kendi domaininden okuyor**:

```ts
const token = request.cookies.get("token")?.value;
```

Cookie'yi backend set ediyor. Backend `psylog-api.railway.app` üzerindeyse cookie o domaine yazılır ve `psktugcetekin.com` üzerinde çalışan Next.js middleware **cookie'yi asla göremez**. Sonuç: login başarılı olur ama kullanıcı sürekli `/admin/login`'e geri atılır.

Çözüm: backend `api.psktugcetekin.com` olursa cookie `.psktugcetekin.com` parent domain'ine yazılabilir ve iki taraf da görür.

```java
ResponseCookie.from("token", token)
    .domain(".psktugcetekin.com")   // parent domain — iki subdomain de okur
    ...
```

> Not: Bu ayarın lokal geliştirmeyi bozmaması gerekiyor. `.domain()` değeri de env'den okunmalı ve lokalde boş/`null` bırakılmalı (localhost'ta domain attribute'u verilmemeli).

---

## 2. Canlıda kırılacak üç sabit kod

### 2.1 CORS localhost'a kilitli

`src/main/java/com/gokalp/psylog_api/config/CorsConfig.java:19`

```java
config.setAllowedOrigins(List.of("http://localhost:3000"));
```

Vercel'den gelen her istek CORS'ta reddedilir. Env'den okunacak şekilde çevrilmeli, virgülle ayrılmış liste desteklesin:

```
CORS_ALLOWED_ORIGINS=https://psktugcetekin.com,https://www.psktugcetekin.com
```

`setAllowCredentials(true)` zaten doğru ve **kalmalı** — frontend tüm isteklerde `withCredentials: true` gönderiyor (`src/lib/api.ts`). `allowCredentials(true)` ile `allowedOrigins("*")` birlikte kullanılamaz, o yüzden origin listesi açıkça yazılmalı.

Vercel preview deployment'larını da test edeceksen (`*.vercel.app`) origin listesine onu da eklemen gerekir; `setAllowedOriginPatterns` kullanmak gerekebilir.

### 2.2 Cookie ayarları cross-site'ta çalışmaz

`src/main/java/com/gokalp/psylog_api/controller/AuthController.java:30-35` ve `45-50`

```java
.secure(false)       // HTTPS'te cookie hiç gönderilmez
.sameSite("Strict")  // farklı subdomain'e cookie gitmez
```

Production'da olması gereken:

- `.secure(true)` — HTTPS zorunlu
- `.sameSite("None")` — cross-site istek için şart (`Secure` olmadan tarayıcı reddeder)
- `.domain(".psktugcetekin.com")` — bkz. bölüm 1
- `.httpOnly(true)` — zaten öyleyse dokunma, frontend kuralı gereği JWT asla JS'e açılmamalı

Bu değerler **hardcode edilmemeli**, env'den okunmalı ki lokalde `secure=false` / `sameSite=Lax` ile çalışmaya devam etsin. Logout endpoint'indeki cookie temizleme de **birebir aynı** attribute'larla yazılmalı (domain/path/secure/sameSite uyuşmazsa tarayıcı cookie'yi silmez).

### 2.3 Konfigürasyon `application.properties` içinde sabit

`src/main/resources/application.properties` gitignore'da (`.gitignore:39`) — yani deploy platformunda o dosya hiç olmayacak. Tüm değerler environment variable'dan okunmalı:

```properties
spring.datasource.url=${DATABASE_URL}
jwt.secret=${JWT_SECRET}
```

---

## 3. Gerekli environment variable listesi

Kodda `@Value` ile okunan ve `application.properties`'de tanımlı olan tüm anahtarlar:

| Env değişkeni                                          | Karşılığı                    | Not                                                                                                                                   |
| ------------------------------------------------------ | ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `DATABASE_URL`                                         | `spring.datasource.url`      | Managed Postgres'ten gelir. Railway'in verdiği `postgresql://` formatını Spring `jdbc:postgresql://` bekler — dönüştürmen gerekebilir |
| `DATABASE_USERNAME`                                    | `spring.datasource.username` |                                                                                                                                       |
| `DATABASE_PASSWORD`                                    | `spring.datasource.password` |                                                                                                                                       |
| `JWT_SECRET`                                           | `jwt.secret`                 | **Production'da yeni ve güçlü üret.** Lokaldekini taşıma                                                                              |
| `JWT_EXPIRATION`                                       | `jwt.expiration`             | Mevcut: 86400000 (24 saat)                                                                                                            |
| `ADMIN_EMAIL`                                          | `admin.email`                | Seed admin                                                                                                                            |
| `ADMIN_PASSWORD`                                       | `admin.password`             | **Güçlü bir şifre.** Tek admin var, public kayıt yok                                                                                  |
| `MAIL_HOST` / `MAIL_PORT`                              | `spring.mail.*`              | İletişim formu bildirimi için                                                                                                         |
| `MAIL_USERNAME` / `MAIL_PASSWORD`                      | `spring.mail.*`              | Gmail ise **app password** gerekir, normal şifre çalışmaz                                                                             |
| `APP_NOTIFICATION_EMAIL`                               | `app.notification.email`     | İletişim formu bildirimlerinin gideceği adres                                                                                         |
| `CORS_ALLOWED_ORIGINS`                                 | yeni                         | bkz. 2.1                                                                                                                              |
| `COOKIE_SECURE` / `COOKIE_SAME_SITE` / `COOKIE_DOMAIN` | yeni                         | bkz. 2.2                                                                                                                              |
| `PORT`                                                 | `server.port`                | Railway/Render portu env ile dayatır — `server.port=${PORT:8080}` ekle                                                                |

### `ddl-auto` uyarısı

Şu an `spring.jpa.hibernate.ddl-auto=update`. Canlıda ilk deploy için tabloları oluşturması pratik, ama uzun vadede riskli (kolon silme/rename'lerde sessiz veri kaybı). En azından `spring.jpa.show-sql` ve `format_sql` production'da `false` olmalı — log gürültüsü ve hafif performans maliyeti.

---

## 4. Frontend'in backend'den beklentileri

Bunlar frontend kodunda sabit — backend bunlara uymalı, tersi değil.

**Base URL:** Frontend `NEXT_PUBLIC_API_URL` + path şeklinde çağırıyor. Yani backend'in tüm endpoint'leri **`/api/...` prefix'iyle kök dizinde** olmalı. Deploy platformunda `context-path` ekleme.

**Auth endpoint'leri** (`src/services/auth.service.ts`):

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET  /api/auth/validate`

**Cookie adı:** `token` — frontend middleware'i bu isme bakıyor, değiştirme.

**Sunucu tarafı çağrılar:** Public sayfalar Next.js **sunucusundan** (Vercel'in datacenter'ından) fetch atıyor, tarayıcıdan değil (`src/lib/server-fetch.ts`). Yani backend'e gelen isteklerin IP'si kullanıcı IP'si olmayacak. Eğer rate limiting / IP bazlı bir şey eklersen bunu hesaba kat. Ayrıca backend'e IP allowlist koyma — Vercel'in çıkış IP'leri sabit değil.

**Hata formatı:** Frontend `ApiException` üretirken response body'de şu alanları bekliyor (`src/lib/api.ts`, `src/types/common.ts`):

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "/api/..."
}
```

`message` alanı kullanıcıya toast olarak gösteriliyor — global exception handler bu formatı korumalı ve mesajlar Türkçe/kullanıcı dostu olmalı. Stack trace veya iç detay sızdırma.

**HTML içerik:** `about.message` ve post `content` alanları HTML dönüyor, frontend DOMPurify ile sanitize ediyor. Backend'in de kayıt sırasında sanitize etmesi iyi olur (defense in depth) ama frontend zaten koruyor.

**Sitemap:** Frontend `sitemap.xml` üretirken `getPosts({ size: 1000 })` ve `getSpecializations()` çağırıyor. Bu endpoint'lerin sayfalama parametresiyle çalışması ve `slug` + `createdAt`/`updatedAt` alanlarını döndürmesi gerekiyor. SEO için kritik — bu çağrı boş dönerse sitemap sadece statik sayfaları içerir.

---

## 5. Hosting önerisi

**Railway** öneriliyor: Spring Boot'u Dockerfile olmadan tanıyor (Nixpacks), managed Postgres'i aynı panelde veriyor, env bağlama kolay, custom domain destekliyor.

Render'ın ücretsiz katmanı 15 dk trafik yokken uyuyor — ilk istekte ~50 sn cold start. Googlebot için de kötü, kullanıcı için de. Kullanacaksan ücretli katman.

Java 21 + Spring Boot 4.0.3 kullanılıyor (`pom.xml`), platformun bu sürümü desteklediğinden emin ol.

---

## 6. Deploy checklist

1. [ ] `application.properties` tüm değerleri `${ENV_VAR}` okuyacak şekilde çevir, lokalde çalıştığını doğrula
2. [ ] `server.port=${PORT:8080}` ekle
3. [ ] CORS'u env'den okunur yap (bölüm 2.1)
4. [ ] Cookie `secure` / `sameSite` / `domain` env'den okunur yap (bölüm 2.2), logout'ta aynı attribute'lar
5. [ ] `application.properties.example`'ı güncelle
6. [ ] Managed Postgres oluştur, connection bilgilerini al
7. [ ] Backend'i deploy et, env değişkenlerini gir
8. [ ] `api.psktugcetekin.com` custom domain'ini bağla, SSL'in aktif olduğunu doğrula
9. [ ] Public bir endpoint'i tarayıcıdan aç, JSON döndüğünü gör
10. [ ] Seed admin'in oluştuğunu doğrula
11. [ ] Frontend deploy edildikten sonra: canlıda admin login → cookie'nin `.psktugcetekin.com` domaininde set edildiğini DevTools → Application → Cookies'ten doğrula, sayfa yenileyince oturumun kalıcı olduğunu gör

---

## 7. Backend bittikten sonra frontend tarafında yapılacaklar

Bunlar frontend oturumunun işi, bilgi olsun diye:

- Vercel'de `NEXT_PUBLIC_API_URL=https://api.psktugcetekin.com` set edilecek
- Diğer env'ler: `NEXT_PUBLIC_SITE_URL`, `NEXT_PUBLIC_GA_ID` (`G-REB40WN25D`), `NEXT_PUBLIC_GSC_VERIFICATION`, Cloudinary değerleri
- Google Search Console doğrulaması + sitemap gönderimi + indeksleme isteği (backend canlı olduktan **sonra**)
- Eklenecek SEO işleri: JSON-LD structured data (`Article`, `Person`/`ProfessionalService`), yazı başına Open Graph görseli
