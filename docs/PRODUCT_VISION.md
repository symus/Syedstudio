# Live Shop Link — Product Vision

Status: **Draft v0.1 (Stage 2)** · Date: 2026-11-17
This draft is used to build the Week-3 prototype. It is **locked only after** the Stage 1 validation gates pass
(see `STAGE_1_STRATEGIC_VALIDATION.md`). Detailed scope lives in `MVP_REQUIREMENTS.md`.

---

## 1. Vision

**Every small seller who sells through chat should run their business from one link — without losing orders,
overselling stock, or giving their customers away to a marketplace.**

Long term: seller shop → repeat buyers → live selling → a marketplace of trusted independent sellers.
Short term: **one job done very well — turn DM orders into organised orders with correct stock.**

## 2. Positioning statement

> **For** small fashion and accessory sellers in Poland **who** sell in drops on Instagram, Facebook and WhatsApp
> and take orders by DM,
> **Live Shop Link is** a shop link plus order desk
> **that** turns chat orders into tracked orders with held stock and clear payment status, in minutes.
> **Unlike** Shopify or Wix, it is built for chat selling, not for websites; **unlike** Allegro, Vinted or TikTok Shop,
> the seller keeps the buyer and pays **no commission** on their own orders.

Tagline idea (to test): *"Z DM-ów do zamówień." / "From DMs to orders."*

## 3. Who it is for

> Personas are **hypotheses** built from desk research. They are not real interview results. Replace them with
> real quotes after Week 2 of validation.

### P1 — Primary: "The drop boutique owner"
- 1–3 person business, sells clothes/accessories. 2k–20k Instagram/Facebook followers.
- Posts new stock 1–3 times a week (stories, posts, WhatsApp status). Sometimes does a live session.
- Takes orders by DM and comments ("biorę", "rezerwuję"). Tracks them in a notebook, Excel or Notes app.
- Gets paid by bank transfer or BLIK to phone. Ships with InPost lockers or offers local pickup.
- **Pains:** lost orders, double-selling the last piece, chasing unpaid reservations, answering "is it still available?"
- **Success for them:** fewer mistakes, less time on admin, more repeat buyers.

### P2 — Buyer: "The follower"
- Follows the seller on Instagram, buys on a phone, often within minutes of a drop.
- Does **not** want an account or an app. Wants to know: is it available, how much, how do I pay, when will it arrive.

### P3 — Secondary (test only): "The registered home baker"
- Sanepid-registered, takes pre-orders for a pickup day. Same chat-order pain, plus cut-off times.
- Only tested lightly; not designed for in the MVP.

## 4. Problems we solve (in priority order)

1. Orders scattered across DMs, comments and chats → **one order list**.
2. Overselling limited stock → **stock that is held when ordered and released if unpaid**.
3. Chasing payment and status → **clear status per order + one-tap WhatsApp message to the buyer**.
4. No customer memory → **customer list built automatically from orders**.

## 5. Product principles (from the master prompt, made concrete)

| Principle | What it means in the MVP |
|---|---|
| Seller-first | Every MVP feature removes seller admin work. No buyer-only features. |
| Share-first | Share button on every screen that matters; link preview looks good in WhatsApp/Instagram. |
| Frictionless buying | Guest order in ≤ 3 screens and ≤ 60 seconds; no account, no password. |
| Seller-owned audience | Seller can export customers; we never message buyers for our own marketing. |
| Unified selling | One stock number shared by the shop link and manual "DM orders". |
| Global-ready, Poland first | Money in minor units + ISO currency, UTC timestamps, locale files — but PL/PLN only at launch. |
| Simple MVP | If a feature does not help a seller process an order this week, it waits. |
| Measurable | Every feature has an acceptance criterion in `MVP_REQUIREMENTS.md`. |

## 6. What we are NOT building now (non-goals)

- Marketplace discovery (Craftora's marketplace is hidden behind a feature flag for this product).
- Live video and comment-to-order (Selmo's ground; revisit in Phase 3).
- Native mobile apps (web/PWA only).
- Online card/BLIK checkout (manual payment first; Stripe Connect after validation).
- Paid subscriptions and billing (founding sellers are free during the pilot).
- AI features, reviews, loyalty, group orders, discount codes, custom domains, staff accounts, multi-country launch.

## 7. North-star metric

**Weekly transacting sellers (WTS):** sellers with ≥ 1 non-cancelled order recorded in the platform in a given week.
Supporting metrics: time to first published shop, time to first order, orders per active seller, share of a seller's
orders that go through the link (vs manual), repeat buyer rate, 30-day seller retention.

## 8. Naming note (challenge)

"**Live** Shop Link" suggests live streaming, which we are deliberately **not** building first. It may confuse
sellers and is hard to say in Polish. Keep it as a working name; test 2–3 alternatives during interviews before
buying a domain or registering a trademark.
