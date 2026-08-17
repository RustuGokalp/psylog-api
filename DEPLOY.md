# psylog-api — Production Deploy

Hedef: backend `https://api.psktugcetekin.com`, frontend `https://psktugcetekin.com` (Vercel).

## Nasıl çalışıyor

- Lokal geliştirme `src/main/resources/application.properties` ile çalışır. Bu dosya `.gitignore`'da,
  sunucuya hiç gitmez.
- Production `application-prod.properties` ile çalışır. Bu dosya repoda **var** ama içinde hiçbir
  gizli değer yok — hepsi environment variable'dan okunur.
- `Dockerfile` prod profilini otomatik aktif eder (`SPRING_PROFILES_ACTIVE=prod`).

## 1. Veritabanı (Supabase)

Supabase → New project. Önemli ayarlar:

- **Region:** Central EU (Frankfurt) — sonradan değiştirilemez, backend ile aynı bölgede olmalı
- **Enable Data API:** kapalı. Spring doğrudan JDBC ile bağlanıyor, PostgREST'e ihtiyaç yok
- **Database password:** oluşturulduğu anda kaydet, bir daha gösterilmiyor

Bağlantı için **Session pooler** bilgilerini kullan (Connect → Direct → Session pooler).
"Direct connection" IPv6-only olduğu için çoğu hosting platformundan erişilemez; transaction
pooler (6543) ise Hibernate'in prepared statement'larıyla sorun çıkarır.

Supabase'de veritabanı adı `postgres`, kullanıcı adı `postgres.<project-ref>` şeklindedir.

### Lokal veriyi aktarma

```bash
pg_dump -d psylog --no-owner --no-privileges --clean --if-exists -f psylog-dump.sql
PGPASSWORD='<db-sifresi>' psql -h <pooler-host> -p 5432 -U postgres.<project-ref> -d postgres -f psylog-dump.sql
```

Dump admin kullanıcısını da taşır; bu durumda seed çalışmaz, giriş bilgileri lokaldekiyle aynı olur.

## 2. Backend servisini oluştur

Railway → New → Deploy from GitHub repo → `psylog-api`. Repoda `Dockerfile` olduğu için Railway
onu kullanır; ayrı bir build ayarı gerekmez. Servisi Frankfurt (EU) bölgesine kur.

## 3. Environment variable'lar

| Değişken               | Değer                                                                              |
| ---------------------- | ---------------------------------------------------------------------------------- |
| `DATABASE_URL`         | `jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require`                    |
| `DATABASE_USERNAME`    | `postgres.<project-ref>`                                                           |
| `DATABASE_PASSWORD`    | Supabase DB şifresi — olduğu gibi, encode etmeden                                  |
| `JWT_SECRET`           | yeni üretilmiş güçlü değer — lokaldekini taşıma (`openssl rand -base64 48`)         |
| `JWT_EXPIRATION`       | `86400000`                                                                         |
| `ADMIN_EMAIL`          | admin e-postası                                                                    |
| `ADMIN_PASSWORD`       | admin şifresi                                                                      |
| `CORS_ALLOWED_ORIGINS` | `https://psktugcetekin.com,https://www.psktugcetekin.com`                          |
| `COOKIE_SECURE`        | `true`                                                                             |
| `COOKIE_SAME_SITE`     | `None`                                                                             |
| `COOKIE_DOMAIN`        | `.psktugcetekin.com`                                                               |
| `MAIL_USERNAME`        | Gmail adresi                                                                       |
| `MAIL_APP_PASSWORD`    | Gmail **app password** (normal şifre çalışmaz)                                     |
| `NOTIFICATION_EMAIL`   | bildirimlerin gideceği adres                                                       |

`PORT` Railway tarafından otomatik verilir, elle girme.

Vercel preview deployment'larını da test edeceksen `CORS_ALLOWED_ORIGINS`'e
`https://*.vercel.app` ekleyebilirsin — origin listesi pattern destekliyor.

## 4. Custom domain

Railway servis ayarlarından `api.psktugcetekin.com` ekle, verilen CNAME'i DNS'e gir, SSL yeşile
dönene kadar bekle.

`COOKIE_DOMAIN=.psktugcetekin.com` sayesinde login cookie'si hem `api.psktugcetekin.com` hem de
`psktugcetekin.com` tarafından görülür — frontend middleware'inin çalışması buna bağlı.

## 5. Deploy sonrası kontrol

```bash
curl -i https://api.psktugcetekin.com/api/posts          # 200 + JSON
curl -i https://api.psktugcetekin.com/api/specializations # 200 + JSON

curl -i -X POST https://api.psktugcetekin.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -H 'Origin: https://psktugcetekin.com' \
  -d '{"email":"...","password":"..."}'
```

Login yanıtındaki `Set-Cookie` şöyle olmalı:

```
token=...; Path=/; Domain=.psktugcetekin.com; Max-Age=86400; Secure; HttpOnly; SameSite=None
```

`Access-Control-Allow-Origin: https://psktugcetekin.com` ve
`Access-Control-Allow-Credentials: true` başlıkları da dönmeli.

Seed admin sadece kullanıcı tablosu boşken oluşur; lokal veri aktarıldıysa admin satırı zaten
gelir ve log'da `Admin user created` görünmez — bu normaldir. Admin şifresini sonradan
değiştirmek istersen env'i değiştirmek yetmez, DB'deki satırı güncellemek gerekir.

## Notlar

- `ddl-auto=update` şema yoksa tabloları oluşturur. Şema oturduktan sonra `JPA_DDL_AUTO=validate`
  yapıp migration'a geçmek daha güvenli.
- Swagger production'da kapalı (`springdoc.*.enabled=false`).
- Hata gövdesi frontend'in beklediği formatta (`timestamp/status/error/message/path`), stack trace sızmıyor.
