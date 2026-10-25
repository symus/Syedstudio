# Craftora — website builder + handmade marketplace (Phase 1 MVP)

> **“Craftora” is a placeholder brand name.** Check trademark and domain availability before a commercial launch.
> The brand name, support e-mail and site URL can be changed through environment variables (see `.env.example`).

Craftora lets small creative businesses (ceramicists, jewellers, candle makers, illustrators, crochet studios…)
**create their own mini-website** and **list their products in a shared marketplace**. Customers can browse,
search and send **custom-order inquiries**. Administrators approve makers and moderate listings.
The first market is Poland: the UI is in Polish (default) and English.

---

## 1. Architecture

```
Browser ──► Next.js 16 (frontend, :3000) ──/api/* rewrite──► Spring Boot 4.1 (backend, :8080) ──► PostgreSQL 16
              │  SSR for public/SEO pages                         │  REST + OpenAPI
              │  client components for dashboards                 ├─► Object storage (local disk or S3/R2)
                                                                  └─► SMTP provider (or sandbox outbox)
```

| Decision | Why | Trade-off |
|---|---|---|
| **Two apps (Next.js + Spring Boot)**, as the brief specifies | Clear API boundary; the backend can later serve mobile apps | Two codebases to deploy |
| **Same-origin `/api` rewrite** | Session and CSRF cookies stay first-party; no CORS | The frontend proxies API traffic |
| **Database-backed opaque sessions** (httpOnly cookie, SHA-256 hash stored) | Revocable (logout, password reset, suspension), survive restarts, work across replicas | One indexed lookup per request (cached touch every 10 min) |
| **CSRF**: Spring Security SPA mode (`XSRF-TOKEN` cookie → `X-XSRF-TOKEN` header) | Required because auth uses cookies | The client must fetch `/api/auth/csrf` once (done automatically) |
| **Template-based storefront builder** (structured JSON config, draft + published copies) | No user HTML means no stored XSS; preview and publish are simple | Less freedom than drag-and-drop (planned later) |
| **PostgreSQL full-text search** (`tsvector`, `simple` config) + filters in one SQL query | Fast, no extra infrastructure | No fuzzy or semantic search yet |
| **Modular monolith, feature packages** (`auth`, `maker`, `product`, `storefront`, `inquiry`, `admin`…) | Simple to run, easy to split later | — |
| **In-memory rate limiter** | No extra infrastructure for the MVP | Per instance only — use Redis when scaling to several replicas |

No microservices, Kafka or Kubernetes, as the brief requires.

### Folder structure
```
backend/                      Spring Boot API (Java 21, Maven)
  src/main/java/com/craftora/
    auth/         register, login, sessions, e-mail verification, password reset
    user/         accounts, roles, GDPR export & deletion
    maker/        maker profiles, dashboard overview
    storefront/   website templates, draft/publish config, preview
    product/      products, images, options, tags, marketplace search
    category/     admin-managed categories (per-locale names)
    inquiry/      custom-order inquiries, private attachments, status history
    subscription/ plans and limit enforcement
    report/       abuse reports
    admin/        moderation, audit log, admin bootstrap
    media/        storage (local / S3), image validation, public media endpoint
    mail/         transactional e-mail (log sandbox / SMTP), PL+EN templates
    security/, common/   security config, errors (RFC 9457), paging, rate limiting
  src/main/resources/db/migration/   Flyway migrations
  src/test/                          unit + integration tests (real PostgreSQL)
frontend/                     Next.js 16 + React 19 + Tailwind 4 (TypeScript)
  src/app/(site)/             platform pages (home, marketplace, makers, auth, account, dashboard, admin)
  src/app/[slug]/             public maker websites (home, shop, about, contact, custom-order, product)
  src/app/preview/            authenticated draft preview (used by the builder's device frames)
  src/components/             UI kit, catalogue, storefront templates, dashboard widgets
  src/lib/                    API clients, i18n (pl/en), types, formatting
  tests/e2e/                  Playwright end-to-end tests
docker-compose.yml            local stack (Postgres, Mailpit, backend, frontend)
.github/workflows/ci.yml      lint, typecheck, unit/integration tests, build, E2E
```

## 2. Data model

`app_user`, `user_role`, `user_session`, `user_token` · `maker_profile`, `subscription_plan`, `subscription` ·
`storefront_template`, `storefront` · `category`, `category_translation`, `tag` · `product`, `product_image`,
`product_option`, `product_tag`, `product_occasion` · `custom_order_inquiry`, `inquiry_attachment`,
`inquiry_status_history` · `report`, `audit_log`.

- UUID keys, `created_at`/`updated_at`, foreign keys with `ON DELETE CASCADE` along ownership lines, unique
  constraints (e-mail, maker slug, product slug per maker), indexes for listing, search (GIN) and inboxes.
- **Tenant isolation:** every maker endpoint resolves the caller's own maker profile first. Products and inquiries
  are always loaded with `findByIdAndMakerId`, so another maker's IDs return 404.
- **Deferred entities (not created yet):** `payment_event` (Phase 2 payments), `favorite` and `review` (Phase 3),
  custom domains (Phase 2).

## 3. API

Interactive docs: **http://localhost:8080/api/docs** (OpenAPI JSON at `/api/docs/openapi.json`).

| Area | Endpoints |
|---|---|
| Auth | `GET /api/auth/csrf`, `POST /api/auth/{register,login,logout,verify-email,resend-verification,forgot-password,reset-password}` |
| Account | `GET/PATCH/DELETE /api/me`, `POST /api/me/password`, `POST /api/me/become-maker`, `GET /api/me/export`, `GET /api/account/inquiries` |
| Public | `GET /api/public/{home,categories,cities,plans,sitemap}`, `GET /api/public/products?q&category&city&minPrice&maxPrice&occasion&availability&customizable&sort&page`, `GET /api/public/products/{maker}/{product}`, `GET /api/public/makers[/{slug}]`, `GET /api/public/storefronts/{slug}`, `POST /api/public/inquiries` (multipart), `POST /api/public/reports`, `GET /api/media/public/**` |
| Maker | `/api/maker/profile` (+`/logo`, `/cover`), `/slug-available`, `/overview`, `/subscription`, `/plans`, `/products` CRUD (+`/status`, `/images`, `/images/order`), `/storefront` (+`/hero-image`, `/publish`, `/unpublish`, `/preview`), `/inquiries` (+`/status`, `/reply`, `/notes`, `/attachments/{id}`) |
| Admin | `/api/admin/{stats,users,makers,products,reports,categories,audit}` + status, feature, plan, moderation and resolve actions |

Errors use one problem format: `{ status, title, detail, code, errors? }`. API responses are built from DTOs,
never from entities.

## 4. Running locally

### Option A — Docker Compose (whole stack)
> **Windows users:** follow the step-by-step checklist in [`docs/LOCAL_SETUP_WINDOWS.md`](docs/LOCAL_SETUP_WINDOWS.md).

```bash
docker compose up --build
```
App: http://localhost:3000 · API docs: http://localhost:8080/api/docs · captured e-mails: http://localhost:8025.
Log in as the bootstrap admin (`APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`, defaults in `docker-compose.yml`; change them).

### Option B — run the apps directly
Requirements: Java 21, Maven 3.9+, Node 22, PostgreSQL 16.
```bash
# database
createuser -P craftora          # password: craftora
createdb -O craftora craftora
createdb -O craftora craftora_test

# backend (migrations run automatically); e-mails are logged, not sent (APP_MAIL_MODE=log)
cd backend
APP_ADMIN_EMAIL=admin@craftora.local APP_ADMIN_PASSWORD=admin-password-123 mvn spring-boot:run

# frontend (in another terminal)
cd frontend
npm ci
npm run dev                     # http://localhost:3000 (BACKEND_URL defaults to http://localhost:8080)
```
All settings are listed in **`.env.example`**.

**E-mail sandbox:** with `APP_MAIL_MODE=log`, e-mails are written to the log and not delivered. Setting
`APP_DEV_OUTBOX=true` also exposes the latest 50 at `GET /api/dev/outbox`, so you can open verification and reset
links. **Never enable this in production.**

## 5. Tests

| Suite | Command | What it covers |
|---|---|---|
| Backend unit + integration (42 tests) | `cd backend && mvn verify` (needs PostgreSQL DB `craftora_test`; override with `TEST_DATABASE_URL`) | auth flows, CSRF, roles, suspension, **cross-maker isolation**, product validation and publishing, plan limits, upload signature checks, storefront draft/publish, inquiries and attachments privacy, notes never shown to customers, admin moderation and audit, GDPR export/delete |
| Frontend unit (9 tests) | `cd frontend && npm test` | price parsing, slugs, open-redirect guard, PL/EN dictionary parity, storefront rendering |
| Lint / types | `npm run lint && npm run typecheck` | |
| End-to-end (Playwright) | start the backend with `APP_DEV_OUTBOX=true APP_ADMIN_EMAIL=admin@craftora.local APP_ADMIN_PASSWORD=admin-password-123`, then `cd frontend && npm run build && npx next start` and run `npm run test:e2e` | maker registers → profile → verifies e-mail → creates product with photo → publishes product and website → admin approves → visitor finds it in search, on the website and in the maker directory → sends an inquiry with an attachment → maker quotes it; route protection; mobile layout (no horizontal overflow, mobile menu) |

CI (`.github/workflows/ci.yml`) runs all of the above on every push.

## 6. Deployment

- **Backend:** `backend/Dockerfile` (non-root, JRE 21). Any container host works (Fly.io, Render, Railway, AWS ECS,
  Azure Container Apps). Point it at a **managed PostgreSQL** with automated backups; Flyway migrates on startup.
- **Frontend:** `frontend/Dockerfile` (Next.js standalone), or Vercel with `BACKEND_URL` set to the backend's internal URL.
- **Storage:** set `APP_STORAGE_PROVIDER=s3` with `S3_*` (AWS S3, Cloudflare R2 or another S3-compatible service).
  Use a **private** bucket: the API serves public images and checks access to private attachments. Put a CDN in front of `/api/media`.
- **E-mail:** `APP_MAIL_MODE=smtp` with your provider's SMTP credentials (e.g. Postmark, Brevo, SES).
- **Production settings:** `APP_COOKIE_SECURE=true`, HTTPS only, real `APP_PUBLIC_URL` and `NEXT_PUBLIC_SITE_URL`,
  strong admin password, `APP_DEV_OUTBOX=false`. Expose the backend only through the frontend or a trusted proxy:
  forwarded headers are trusted for client IPs.

## 7. Phase 1 — definition of done

- [x] A new maker can register and create a profile
- [x] The maker can publish products with images
- [x] A professional individual storefront can be generated and published (3 templates, colours, fonts, sections, SEO, desktop/tablet/mobile preview)
- [x] Published products appear in the shared marketplace
- [x] Customers can search for products and makers (keyword, category, city, price, occasion, availability, personalisation, sorting, pagination)
- [x] Customers can submit custom-order inquiries (budget, deadline, private reference images, consent)
- [x] Makers can view and manage inquiries (statuses, replies by e-mail, internal notes, history)
- [x] Administrators can moderate makers and listings (approval, suspension, hiding, reports, categories, featured makers, audit log)
- [x] Private records are protected by server-side authorisation
- [x] Core flows persist data in PostgreSQL
- [x] Works on mobile and desktop
- [x] Setup, testing, configuration and deployment are documented

## 8. Assumptions and known limitations

- **Maker approval is manual** by default (`APP_MAKERS_AUTO_APPROVE=true` disables it). Makers can prepare everything while pending. Publishing the website requires a verified e-mail.
- **Plans** (Free 0 / Creator 29 PLN / Pro 69 PLN) are **test prices** stored in the database. Limits are enforced on the server.
  **Online payment is Phase 2:** admins can change a maker's plan manually during the pilot. No card data is ever handled.
- **Publishing a product** requires at least one photo and a category.
- **Product images** are validated by file signature (JPEG/PNG/WebP, max 5 MB) and served at their original size.
  Server-side resizing and a CDN are recommended before launch.
- **"Trending"** means products with the most inquiries in the last 30 days, since there are no analytics yet. Website views and impressions are not tracked yet.
- **Testimonials** on the homepage are clearly marked as demo content.
- **Locale** is chosen by a cookie or the browser language. Storefronts render in the language the maker picks.
  Locale-prefixed URLs (`/en/...`) for hreflang SEO are planned for Phase 4.
- **Rate limiting is per instance** (in-memory). Use a shared store when running several backend replicas.
- **The S3 storage provider** is implemented, but this environment had no S3-compatible service available to test it against.
  Local-disk storage is covered by the tests.

## 9. Needs professional legal / accounting review before launch

The features below help with compliance, but **they do not on their own make the platform compliant**:
- Privacy policy and terms (`/legal/privacy`, `/legal/terms`) are **placeholders**, marked as such on the page.
- GDPR: controller details, legal bases, retention periods, processor agreements (hosting, e-mail, storage), and the records of processing.
- Trader / seller identity rules (Omnibus Directive, Polish consumer law) and DSA obligations for online platforms (notice-and-action, statement of reasons, trader traceability).
- Platform-to-business (P2B Regulation) terms for makers: ranking transparency, suspension procedures.
- Cookie notice: only strictly necessary cookies are used today. Adding analytics will require a consent banner.
- VAT/invoicing for subscriptions once payments are added.
