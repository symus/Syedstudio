# Decision Log

Format: Decision · Context · Options · Reason · Consequences/risks · Date/status.

---

### D1 — Launch segment: social-native fashion & accessory sellers in Poland
- **Context:** Stage 1 compared home food, fashion, handmade, second-hand and small retail.
- **Options:** Home food; fashion boutiques; handmade; second-hand; small retail.
- **Reason:** Highest chat-order pain, limited stock, repeat drops, proven willingness to pay (Selmo), low regulatory burden. Home food has sanepid/legal risk; second-hand competes with free Vinted.
- **Consequences/risks:** Competes near Selmo and TikTok Shop; variants (sizes) needed in MVP. Registered home bakers tested lightly as back-up.
- **Date/status:** 2026-11-17 · **Accepted by founder** (pending validation gates).

### D2 — Build on the existing Craftora codebase
- **Context:** Repo already has a Spring Boot 4.1 + Next.js 16 + PostgreSQL product (auth, storefronts, products, media, plans, admin, GDPR).
- **Options:** Replace (start fresh); build on Craftora; run as a separate product.
- **Reason:** ~50–60% of MVP infrastructure exists and follows the preferred stack and modular-monolith approach. Saves weeks.
- **Consequences/risks:** Must remove/flag Craftora-specific flows (approval gate, inquiries, marketplace). Code quality and fit must be reviewed in Stage 4. Brand/package naming to be decided later.
- **Date/status:** 2026-11-17 · **Accepted**.

### D3 — Manual payments first; Stripe Connect after validation
- **Context:** Target sellers are paid today by bank transfer, BLIK to phone, or cash on pickup.
- **Options:** Manual only; Stripe Connect from day 1; Polish PSP (e.g. P24/PayU) from day 1.
- **Reason:** Lowest build cost and no payment-licensing questions during the pilot. Stripe supports BLIK and Przelewy24 on Connect, so the upgrade path exists.
- **Consequences/risks:** Payment chasing remains the seller's job; some buyers may expect online payment. Re-check after pilot (assumption A9).
- **Date/status:** 2026-11-17 · **Accepted**.

### D4 — Poland only at launch; global-ready design
- **Reason:** Founder is in Poland; local payments, language and seller access. Architecture keeps currency, locale, time zone and payment adapters generic.
- **Date/status:** 2026-11-17 · **Accepted**.

### D5 — Marketplace and live video deferred
- **Reason:** Strong incumbents (Allegro, Vinted, TikTok Shop, Selmo); chicken-and-egg problem; not needed to test the core job.
- **Revisit when:** ≥ 100 weekly transacting sellers (marketplace) / evidence that live sessions drive most seller revenue (live).
- **Date/status:** 2026-11-17 · **Accepted**.

### D6 — No commission on direct shop orders
- **Reason:** A commission would discourage sellers from sharing their link and pushes them back to DMs. Revenue comes from subscriptions later.
- **Date/status:** 2026-11-17 · **Accepted**.

### D7 — Shops go live without admin approval (post-moderation)
- **Context:** Craftora requires admin approval for new makers; this breaks the "< 5 min to shop" goal.
- **Reason:** Time-to-value. Use reporting, rate limits and manual review of flagged shops instead.
- **Consequences/risks:** More spam/fake-shop risk; mitigate with email verification before first order is accepted, report button, admin suspend.
- **Date/status:** 2026-11-17 · **Proposed** (part of Stage 2 draft).
