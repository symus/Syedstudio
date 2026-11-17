# Live Shop Link — Stage 1: Strategic Validation

Status: **Draft for founder review** · Date: 2026-11-17 · No code in this stage.

Labels used in this document:
- **[Fact]** — backed by a cited source (verify prices again before you use them; they change often).
- **[Assumption]** — what we believe but have not tested.
- **[Recommendation]** — my advice.

> **Important note about this repository.** The repo already contains **Craftora**, a working Phase-1 build
> (Spring Boot 4.1 + Next.js 16 + PostgreSQL) for handmade makers in Poland: maker websites, a shared marketplace,
> products, custom-order *inquiries*, plans, admin moderation and GDPR export. It has **no guest order checkout,
> no stock, no customer list and no share flow**. Live Shop Link overlaps ~50–60% with it. We must decide whether
> Live Shop Link *replaces*, *extends* or *runs beside* Craftora (see Decisions at the end).

---

## 1. The concept in one paragraph

**Live Shop Link is an "order desk" for small sellers who already sell through WhatsApp, Instagram and Facebook.**
The seller gets one permanent link that shows their products with live stock. Buyers order in a few taps without
creating an account. The seller sees every order, buyer and payment status in one place instead of scattered chat
messages. Marketplace discovery and live video come later, once sellers use it every week.

What it is **not** (yet): a Shopify competitor, a marketplace, or a live-streaming app.

---

## 2. The three strongest customer problems

| # | Problem (seller words) | Why it matters | Evidence level |
|---|---|---|---|
| 1 | **"Orders are lost in my DMs."** Orders arrive as chat messages, comments and screenshots. The seller copies them into a notebook or spreadsheet. Mistakes, double-selling and forgotten buyers follow. | Happens every day for active social sellers; costs time and money. | [Fact] WhatsApp Business catalogs have no stock tracking and no order dashboard; orders arrive as chat messages ([Chatarmin](https://chatarmin.com/en/blog/whatsapp-business-catalog), [Spur](https://www.spurnow.com/en/blogs/whatsapp-business-catalog)). Real pain size is **[Assumption]**. |
| 2 | **"I sold the same item twice / I can't track what is reserved."** Limited or one-off stock (clothes, drops, bakes) gets over-promised. | Directly loses money and trust. Strongest for unique or small-batch items. | [Fact] Selmo, a Polish tool, sells comment-to-order automation for Facebook/Instagram live sales to boutiques, which shows a paying market for this pain ([Selmo](https://www.selmo.io/en/live-commerce)). |
| 3 | **"A real shop is too much work and too expensive, but a chat is too little."** Shopify/Wix feel heavy for a 1-person seller. | Sets our positioning: lighter than a website builder, more structured than chat. | [Fact] Wix's cheap plans cannot sell; selling starts at ~$29/mo ([WebsiteBuilderExpert](https://www.websitebuilderexpert.com/ecommerce-website-builders/free-ecommerce-platforms)). Shopify Starter (~$5/mo) targets exactly this social-link use case ([Launchtip](https://www.launchtip.com/the-essential-guide-to-shopify-transaction-fees-plans-costs/)), so this gap is **partly already filled**. |

**Critical challenge:** Problem 3 alone is weak — Shopify Starter, Square Free and Take App already offer cheap
link-based shops. We only win if we solve **Problems 1 and 2 much better** for a specific seller type.

---

## 3. Launch category comparison

Score 1 (bad) – 5 (good). Scores are my judgement **[Assumption]**, to be checked in interviews.

| Criterion | Home food | Fashion boutiques (social-native) | Handmade / craft | Second-hand | Small retail shops |
|---|---|---|---|---|---|
| Order frequency / repeat buying | 5 | 4 | 2 | 2 | 3 |
| Pain from chat-based ordering | 4 | **5** | 3 | 3 | 2 |
| Seller access for you in Poland | 3 | 4 | 4 (Craftora contacts?) | 3 | 3 |
| Regulatory simplicity | **1** | 4 | 4 | 4 | 4 |
| Operational simplicity (fulfilment) | 2 (cut-offs, pickup slots, freshness) | 3 (shipping, sizes) | 3 | 3 | 3 |
| Willingness to pay | 2 | **4** | 2 | **1** | 3 |
| Competitor strength against us | Low | Medium (Selmo, TikTok Shop) | High (Etsy) | **Very high** (Vinted free for sellers) | High (Shopify, Allegro) |
| **Total (of 30)** | 18 | **27** | 20 | 17 | 21 |

Key facts behind the scores:
- **Home food:** In Poland, food sold to the public needs sanitary (sanepid) registration and EU hygiene rules
  (Reg. 852/2004); farm direct sales (RHD) have their own limits and record-keeping ([GIS Warszawa](https://www.gov.pl/web/wsse-warszawa/rolniczy-handel-detaliczny2),
  [WSSE Białystok](https://www.gov.pl/web/wsse-bialystok/produkcja-pierwotna-dostawy-bezposrednie-rhd-rolniczy-handel-detaliczny)).
  Many home cooks are not registered. A platform that makes unregistered food sales easy carries legal and
  reputational risk. **Needs a Polish lawyer before any food launch.**
- **Second-hand:** Vinted is free for sellers (the buyer pays a protection fee) ([Koongo](https://www.koongo.com/blog/marketplace-fees-comparison-2026/)). Hard to charge sellers here.
- **Handmade:** Low frequency; Etsy takes ~6.5% transaction fee + $0.20 listing ([Checkoutpage](https://checkoutpage.com/blog/etsy-fees)) but brings buyers we cannot bring.
- **Fashion boutiques:** Polish "live sprzedaż" boutiques already pay for Selmo (Shopify-app price listed at $89/mo
  for Basic — Polish price not confirmed) ([Shopify App Store](https://apps.shopify.com/selmo?locale=th)). That proves
  willingness to pay **and** shows a strong local competitor.

### [Recommendation] Launch segment

**Primary:** *Social-native fashion & accessories sellers in Poland (1–3 people) who sell through Instagram,
Facebook and WhatsApp in weekly "drops", and take orders by DM or comment.*
Examples: small clothing boutiques, resellers of new stock, jewellery and accessory sellers.

**Why:** highest pain from chat ordering, limited stock (reservation pain), repeat buyers who follow the seller,
proven willingness to pay, low regulatory burden.

**Our angle vs Selmo:** Selmo is built around *live* sessions. We target the **everyday, non-live** selling
(drops, stories, WhatsApp status, "link in bio") — a simpler, cheaper tool — and add live later.
**[Assumption — must test]** that many boutiques sell mostly *outside* live sessions.

**Back-up segment (test lightly in parallel):** *registered* home bakers / cake makers taking pre-orders with
pickup slots. Only registered sellers — never position the product as a way around sanepid rules.

---

## 4. Competitors and differentiation

| Competitor | Type | Who it serves | Price signal | Weakness for our segment |
|---|---|---|---|---|
| WhatsApp Business catalog | Indirect, free | Any small seller | Free | Max ~500 items, no stock, no order dashboard ([Chatarmin](https://chatarmin.com/en/blog/whatsapp-business-catalog)) |
| Instagram / Facebook Shops | Indirect | Brands | Free, but **native checkout removed in 2025** — buyers sent to a website ([PPC Land](https://ppc.land/meta-phases-out-facebook-and-instagram-shops-checkout-by-august-2025/)) | Seller needs their own checkout → **opportunity for us** |
| Shopify Starter | Direct | Social sellers | ~$5/mo ([Launchtip](https://www.launchtip.com/the-essential-guide-to-shopify-transaction-fees-plans-costs/)); Linkpop link-in-bio reportedly retired mid-2025 ([Nextsky](https://nextsky.co/blogs/shopify/shopify-starter-plan)) | English-first admin, card-checkout focus, weak at DM/reservation workflow |
| Square Online Free | Direct | US/UK/… merchants | $0 + higher card fee ([WebsiteBuilderExpert](https://www.websitebuilderexpert.com/ecommerce-website-builders/square-online-store-pricing/)) | **Not available in Poland** [verify] |
| Wix / WooCommerce | Indirect | Website owners | Wix selling from ~$29/mo | Too heavy for 1-person social sellers |
| Take App / Bumpa / Dukaan | Direct (WhatsApp stores) | Asia / Africa / India | Take App free tier, paid ~$29–50/mo ([Take App](https://www.take.app/help/subscription-plans-and-features)) | Weak local payments (BLIK) and Polish language [verify] |
| Selmo | Direct (PL) | FB/IG live boutiques | ~$89/mo (Shopify listing) | Live-first, more complex and pricier for non-live sellers [verify with sellers] |
| TikTok Shop | Indirect / channel | Creators & brands | Commission | **Launched in Poland 15 June 2026** ([Shopifreaks](https://www.shopifreaks.com/tiktok-expands-tiktok-shop-to-austria-belgium-the-netherlands-and-poland-starting-june-15/)); seller does not own the buyer |
| Allegro / Allegro Lokalnie / Vinted | Marketplaces | Everyone in PL | 1–17% commission (Allegro); Vinted free for sellers ([Koongo](https://www.koongo.com/blog/marketplace-fees-comparison-2026/)) | Buyer belongs to the marketplace, price competition |
| Etsy / eBay / Whatnot | Marketplaces / live | Global | ~6.5% + fees (Etsy) | Not built for a seller's own audience |

### [Recommendation] Differentiation (one sentence)

> **"Turn your DMs into orders": one Polish-first link with live stock, reservations and BLIK payment, plus an
> order desk built for sellers who sell in drops on Instagram, Facebook and WhatsApp — no commission on your own orders.**

Four concrete wedges:
1. **Reservation-aware stock** — an item is held for X minutes/hours after an order, then released. Stops double-selling.
2. **Chat-native order capture** — seller can turn a DM order into a real order in seconds (paste/quick-add), not only buyer self-checkout.
3. **Local payments** — BLIK and Przelewy24 work on Stripe Connect ([Stripe BLIK](https://docs.stripe.com/payments/blik), [Stripe P24](https://docs.stripe.com/payments/p24)); plus "pay by bank transfer / on pickup" at MVP.
4. **Buyer list the seller owns** — exportable, with marketing consent (GDPR).

Why a seller would use it *alongside* existing tools: they keep Instagram/WhatsApp for reach and chat; we replace
the notebook/spreadsheet + manual stock + payment chasing.

---

## 5. The ten riskiest assumptions

| # | Assumption | How to test | Kill / pass signal |
|---|---|---|---|
| A1 | Target sellers lose real time/money from chat orders | 15+ interviews; ask for last week's actual orders | Pass: ≥60% describe a recent costly mistake unprompted |
| A2 | Sellers will move buyers from DM to a link | Concierge test: seller shares our link for 2 weeks | Pass: ≥50% of their orders come through the link |
| A3 | Buyers will order through a link without an account | Track link → order conversion | Pass: ≥3% visit→order on drops |
| A4 | Most boutique sales happen *outside* live sessions | Interview + look at their posting history | Fail if most revenue is live → Selmo territory |
| A5 | Sellers will pay ~€9–19/month (≈ 39–79 PLN) | Pre-sale: founding-seller deposit/LOI | Pass: ≥5 of 20 commit money |
| A6 | Reservation/stock is the #1 pain (not payments, not shipping labels) | Ranking exercise in interviews | Re-scope if another pain wins clearly |
| A7 | A 1-person founder can onboard sellers in Poland personally | Track hours per onboarded seller | Fail if >3 h per active seller |
| A8 | Sellers send buyers back again (repeat drops) | Count shares/week per seller | Pass: ≥1 share/week for 4 weeks |
| A9 | Manual payment (transfer/BLIK phone) is acceptable at MVP | Ask sellers how they get paid today | Re-scope if card/BLIK online is a must from day 1 |
| A10 | The hybrid marketplace adds value later | Not testable now — **park it** | Revisit only after 100 weekly transacting sellers |

---

## 6. 30-day validation plan (no product code)

| Week | Activities | Output / metric |
|---|---|---|
| **1 — Find sellers** | List 80 Polish IG/FB boutiques & accessory sellers (hashtags, FB groups, "live sprzedaż"), plus 20 registered home bakers. DM 100. Book 15–20 calls. | ≥15 interviews booked |
| **2 — Interview** | 30-min problem interviews (no pitching). Ask them to show last week's orders. Record: channels, order count/week, mistakes, tools used, money spent on tools. | Pain ranking; A1, A4, A6, A9 answered |
| **3 — Prototype test** | Clickable Figma (or reuse Craftora screens) for: create shop → add product → share → buyer orders → seller order desk. Test with 8–10 sellers + 10 buyers. Measure time-to-shop and task success. | Shop created < 5 min in prototype; ≥80% task success |
| **4 — Concierge pilot + money test** | 5–8 sellers use a **no-code** version (e.g. a simple form + spreadsheet run by you, or a Craftora page) for one real drop. Offer a founding-seller deal (e.g. 3 months Pro for a small deposit). | Real orders captured; # of sellers paying/committing |

**Go to Stage 2 if all are true:**
- ≥10 of 15 sellers confirm the chat-order pain with concrete examples.
- ≥3 pilot sellers process real orders through the link in one drop.
- ≥5 sellers commit money or sign a clear letter of intent.

**Pivot / narrow if:** pain is real but mainly live → narrow to a live-first or Selmo-complement product; or pain is
real for bakers not boutiques → switch segment (with legal check).
**Stop if:** fewer than 5 sellers see the pain as worth changing their habits.

Positive feedback alone does not count. Only behaviour (sharing the link, real orders, money) counts.

---

## 7. Recommendation

**NARROW, then proceed.**

- Proceed with the idea, but **narrow** to one segment (Polish social-native fashion/accessory sellers) and one
  job ("DM orders → organised orders with stock").
- Global-ready **architecture** (currency, locale, time zone, payment adapters) — but launch **Poland only**.
- **Park** marketplace and live video. Both are expensive, and competitors (Allegro, Vinted, TikTok Shop, Selmo)
  are strong there.
- **Reuse Craftora** where possible instead of starting over: auth, sessions, storefront, products, media,
  subscriptions, admin and GDPR modules already exist. This could cut the MVP to ~3–5 weeks of work after validation.
  **[Assumption — needs a code review in Stage 4.]**
- The biggest risk is not technology. It is **sellers not changing their habit** of taking orders in DMs.

---

## 8. Sources

- [Chatarmin — WhatsApp catalog limits](https://chatarmin.com/en/blog/whatsapp-business-catalog)
- [Spur — WhatsApp Business catalog](https://www.spurnow.com/en/blogs/whatsapp-business-catalog)
- [Launchtip — Shopify pricing 2026](https://www.launchtip.com/the-essential-guide-to-shopify-transaction-fees-plans-costs/)
- [Nextsky — Shopify Starter / Linkpop](https://nextsky.co/blogs/shopify/shopify-starter-plan)
- [WebsiteBuilderExpert — free ecommerce platforms](https://www.websitebuilderexpert.com/ecommerce-website-builders/free-ecommerce-platforms)
- [WebsiteBuilderExpert — Square Online pricing](https://www.websitebuilderexpert.com/ecommerce-website-builders/square-online-store-pricing/)
- [Take App — plans](https://www.take.app/help/subscription-plans-and-features)
- [Selmo — live commerce](https://www.selmo.io/en/live-commerce), [Selmo on Shopify App Store](https://apps.shopify.com/selmo?locale=th)
- [PPC Land — Meta removes Shops checkout (2025)](https://ppc.land/meta-phases-out-facebook-and-instagram-shops-checkout-by-august-2025/)
- [Shopifreaks — TikTok Shop launches in Poland](https://www.shopifreaks.com/tiktok-expands-tiktok-shop-to-austria-belgium-the-netherlands-and-poland-starting-june-15/)
- [Koongo — marketplace fees 2026](https://www.koongo.com/blog/marketplace-fees-comparison-2026/)
- [Checkoutpage — Etsy fees 2026](https://checkoutpage.com/blog/etsy-fees)
- [Stripe — BLIK](https://docs.stripe.com/payments/blik), [Stripe — Przelewy24](https://docs.stripe.com/payments/p24)
- [GIS/WSSE Warszawa — RHD](https://www.gov.pl/web/wsse-warszawa/rolniczy-handel-detaliczny2), [WSSE Białystok — RHD](https://www.gov.pl/web/wsse-bialystok/produkcja-pierwotna-dostawy-bezposrednie-rhd-rolniczy-handel-detaliczny)

Most competitor prices come from third-party sites. Check each vendor's own pricing page before using them in a plan.
Nothing here is legal advice.
