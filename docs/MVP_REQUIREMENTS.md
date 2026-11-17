# Live Shop Link — MVP Requirements

Status: **Draft v0.1 (Stage 2)** · Date: 2026-11-17 · Companion to `PRODUCT_VISION.md`.
Base: the MVP is built **on top of the existing Craftora code** (decision D2 in `DECISION_LOG.md`).

---

## 1. Core user journeys

Times are **targets**, measured in moderated tests with ≥ 5 sellers / ≥ 5 buyers on a mid-range phone.

| # | Journey | Steps (happy path) | Target |
|---|---|---|---|
| J1 | Seller signs up | Email + password → verify email (can continue before verifying) | < 1 min |
| J2 | Create shop | Shop name → link (auto-suggested `/kasia-boutique`) → WhatsApp/phone → pickup and/or shipping → payment instructions (bank account / BLIK phone) | < 3 min |
| J3 | Add first product | Photo(s) → name → price → sizes/colours with quantity (or one quantity) → Save | < 2 min |
| J4 | Publish & preview | Shop is **live immediately** (no admin approval); "View as buyer" button | 1 tap |
| J5 | Share | Share sheet (Web Share API) / Copy link / WhatsApp / QR code; good link preview (OG image, title, price) | 1–2 taps |
| J6 | Buyer browses | Opens link from Instagram/WhatsApp → shop → product → sees "only 1 left" / "sold out" | page < 2 s on 4G |
| J7 | Buyer orders | Choose size → "Order" → name, phone, (email optional), pickup or InPost locker code/address → payment method → confirm → order page with number and payment instructions | ≤ 3 screens, < 60 s |
| J8 | Seller gets order | Email + badge in order desk; stock is **held** | < 1 min after order |
| J9 | Seller manages order | Confirm → Mark paid → Mark shipped/ready → Complete; or Cancel (stock returns). One-tap WhatsApp message to buyer with order summary | each step 1 tap |
| J10 | DM order (manual) | Seller taps "+ Order" → picks product/size → buyer name/phone → stock held | < 30 s |
| J11 | Repeat customers & stats | Customer list (orders count, total spent, last order); simple week stats | — |

### Order status model

```
NEW ──confirm──► CONFIRMED ──mark paid──► PAID ──► SHIPPED | READY_FOR_PICKUP ──► COMPLETED
 │                  │                       │
 └──────────────────┴────── cancel ─────────┴──► CANCELLED   (held stock is released)
NEW / CONFIRMED and unpaid for longer than the seller's hold time (default 48 h) → EXPIRED (stock released)
```
Seller can mark paid directly from NEW. Stock moves: **held** on NEW, **sold** on PAID, **released** on CANCELLED/EXPIRED.

---

## 2. Prioritisation (impact vs effort)

Impact = value to the primary persona's #1–#3 problems (1–5). Effort = my estimate in developer-days on top of
Craftora (S ≤ 2, M 3–5, L 6–10). Reuse = what Craftora already gives us.

| ID | Feature | Impact | Effort | Reuse from Craftora | Priority |
|---|---|---|---|---|---|
| F1 | Sign-up, login, sessions, email verify, password reset | 3 | S | ✅ auth/, user/ | **Must** |
| F2 | Fast onboarding wizard + **auto-publish** (remove admin approval gate; post-moderation + report button) | 5 | M | Partly (maker/, storefront/) | **Must** |
| F3 | Products with photos and price | 5 | S | ✅ product/, media/ | **Must** |
| F4 | **Variants (size/colour) with quantity** | 5 | M | ❌ new (Craftora options are not stock) | **Must** |
| F5 | **Stock holds + concurrency-safe ordering** (no oversell) | 5 | M | ❌ new | **Must** |
| F6 | Mobile storefront (shop + product pages, sold-out state) | 5 | S–M | ✅ storefront templates, `[slug]` pages | **Must** |
| F7 | **Guest order form** (pickup / InPost locker / address, manual payment choice) | 5 | M | ❌ new (replace "inquiry" flow) | **Must** |
| F8 | Buyer order page (secret link: status + payment instructions) | 4 | S | ❌ new | **Must** |
| F9 | **Seller order desk** (list, filters, status actions, cancel) | 5 | M | Partly (inquiry status history pattern) | **Must** |
| F10 | **Manual "DM order"** creation | 5 | S | ❌ new | **Must** |
| F11 | Share tools (Web Share, copy, `wa.me`, QR, Open Graph images) | 5 | S | Partly | **Must** |
| F12 | Email notifications (seller: new order; buyer: confirmation if email given) | 4 | S | ✅ mail/ | **Must** |
| F13 | Abuse protection on guest orders (rate limit, honeypot, max qty, hold expiry, phone validation) | 4 | S | ✅ RateLimiter (in-memory) | **Must** |
| F14 | PL + EN UI, PLN, trader info, terms/privacy/consent texts | 4 | S | ✅ i18n, trader fields, GDPR export | **Must** |
| F15 | Customer list derived from orders + CSV export | 4 | S | ❌ new | **Should** |
| F16 | One-tap WhatsApp message templates to buyer | 4 | S | ❌ new | **Should** (≈ Must) |
| F17 | Basic weekly stats (views, orders, revenue, link vs manual share) | 3 | S | Partly (analytics flag) | **Should** |
| F18 | Scheduled drop (product goes live at a set time) | 3 | S | ❌ new | **Could** |
| F19 | Discount codes | 2 | M | ❌ | Won't (MVP) |
| F20 | Stripe Connect online payment (BLIK, P24, card) | 4 | L | ❌ | Won't (MVP) → Phase 2 |
| F21 | Subscriptions & billing | 2 (now) | M | Partly (plans) | Won't (MVP) |
| F22 | Marketplace discovery | 1 (now) | — | ✅ exists | Won't — hide behind flag |
| F23 | Live video | 1 (now) | L | ❌ | Won't → Phase 3 |
| F24 | Staff accounts / multi-shop | 1 | M | ❌ | Won't |
| F25 | AI listing from photo | 2 | M | ❌ | Won't → Phase 2 test |

**MVP = F1–F16 (+F17 if time).** Rough total: **~25–35 developer-days** for one experienced developer plus
frontend help. Validate this estimate in Stage 6 after a code review of Craftora.

### Craftora features to switch off for this product (not delete)
Admin approval of new sellers, public marketplace and search pages, custom-order inquiries, multiple storefront
templates (keep one mobile-first template), Pinterest/occasion fields.

---

## 3. Acceptance criteria (measurable)

| ID | Criterion | How we check |
|---|---|---|
| AC1 | Median seller goes from sign-up to a live shop with 1 product in **≤ 5 min** | Moderated test, 5 sellers, phone |
| AC2 | Buyer places an order in **≤ 3 screens and ≤ 60 s** (median), no account | Moderated test, 5 buyers |
| AC3 | **Zero oversell:** 20 parallel orders for the last 1 unit → exactly 1 succeeds, 19 get "sold out" | Automated integration test |
| AC4 | Held stock is released within 5 min after the hold time ends or the order is cancelled | Automated test |
| AC5 | Seller sees a new order in the desk ≤ 10 s after placement (refresh/poll) and gets email ≤ 1 min | Automated + manual |
| AC6 | Shared link shows title, image and price preview in WhatsApp, Instagram DM and Messenger | Manual check on real devices |
| AC7 | Storefront loads in < 2 s (LCP) on simulated 4G; Lighthouse mobile performance ≥ 85, accessibility ≥ 90 | Lighthouse CI |
| AC8 | A seller can never read or change another seller's products, orders or customers | Automated authorisation tests per endpoint |
| AC9 | Guest order endpoint: max 5 orders / 10 min per IP+phone; honeypot rejects bots; duplicate submits are idempotent | Automated tests |
| AC10 | Money stored as integer minor units + ISO currency; all timestamps UTC | Schema review + tests |
| AC11 | All seller/buyer UI texts available in PL and EN | i18n key check in CI |
| AC12 | Seller can export their customers and orders (CSV) and delete their account (GDPR) | Manual + automated |

## 4. Pilot success metrics (first 8 weeks after MVP launch)

| Metric | Target (hypothesis) |
|---|---|
| Founding sellers onboarded personally | 20 |
| Activated (≥ 1 real order in first 14 days) | ≥ 50% (10) |
| Weekly transacting sellers in week 8 | ≥ 8 |
| Share of a seller's orders recorded in the platform | ≥ 60% |
| Orders per active seller per week | ≥ 5 |
| Oversell incidents | 0 |
| Seller would be "very disappointed" without the product (Sean Ellis question) | ≥ 40% |
| Sellers agreeing to pay after the free period | ≥ 5 |

## 5. Open items for later stages
- InPost locker: free-text locker code in MVP; API integration later (Stage 4 to check InPost API terms).
- Consumer-rights texts (distance-selling withdrawal right) shown on the order page — wording needs a Polish lawyer.
- Hold time default (48 h) — confirm with sellers in interviews.
