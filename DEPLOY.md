# psylog-api — Production Deploy

Hedef: backend `https://api.psktugcetekin.com`, frontend `https://psktugcetekin.com` (Vercel).

## Nasıl çalışıyor

- Lokal geliştirme `src/main/resources/application.properties` ile çalışır. Bu dosya `.gitignore`'da,
  sunucuya hiç gitmez.
- Production `application-prod.properties` ile çalışır. Bu dosya repoda **var** ama içinde hiçbir
  gizli değer yok — hepsi environment variable'dan okunur.
- `Dockerfile` prod profilini otomatik aktif eder (`SPRING_PROFILES_ACTIVE=prod`).

## 1. Postgres oluştur

Railway → New → Database → PostgreSQL. Panelden `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`,
`PGPASSWORD` değerlerini al.

> Railway'in verdiği `DATABASE_URL` (`postgresql://...`) Spring için **kullanılamaz**.
> JDBC formatına çevirmek gerekir (aşağıda).

## 2. Backend servisini oluştur

Railway → New → Deploy from GitHub repo → `psylog-api`. Repoda `Dockerfile` olduğu için Railway
onu kullanır; ayrı bir build ayarı gerekmez.

## 3. Environment variable'lar

| Değişken               | Değer                                                                              |
| ---------------------- | ---------------------------------------------------------------------------------- |
| `DATABASE_URL`         | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `DATABASE_USERNAME`    | `${{Postgres.PGUSER}}`                                                             |
| `DATABASE_PASSWORD`    | `${{Postgres.PGPASSWORD}}`                                                         |
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

İlk deploy log'unda `Admin user created: ...` satırı görünmeli — seed admin sadece kullanıcı
tablosu boşken oluşur. Admin şifresini sonradan değiştirmek istersen env'i değiştirmek yetmez,
DB'deki satırı güncellemek gerekir.

## Notlar

- `ddl-auto=update` ilk deploy için tabloları oluşturur. Şema oturduktan sonra `JPA_DDL_AUTO=validate`
  yapıp migration'a geçmek daha güvenli.
- Swagger production'da kapalı (`springdoc.*.enabled=false`).
- Hata gövdesi frontend'in beklediği formatta (`timestamp/status/error/message/path`), stack trace sızmıyor.
