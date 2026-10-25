import Link from "next/link";
import { MakerCard } from "@/components/catalog/MakerCard";
import { ProductGrid } from "@/components/catalog/ProductCard";
import { SearchBar } from "@/components/catalog/SearchBar";
import { PlanCards } from "@/components/site/PlanCards";
import { Badge, ButtonLink, Card, Container, EmptyState, SectionTitle } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { getI18n } from "@/lib/i18n/server";
import type { Home, Plan } from "@/lib/types";

const TEMPLATE_PREVIEWS = [
  { name: "Atelier", bg: "#2F2A26", accent: "#B5654A", font: "var(--font-serif)", title: "Pracownia Glina" },
  { name: "Studio", bg: "#1F2933", accent: "#6B8F71", font: "var(--font-sans)", title: "Nordic Knits" },
  { name: "Market", bg: "#3B2F2F", accent: "#C98B2E", font: "var(--font-rounded)", title: "Candle & Co." },
];

export default async function HomePage() {
  const { locale, t } = await getI18n();
  const [home, plans] = await Promise.all([
    publicApi<Home>(`/api/public/home?locale=${locale}`),
    publicApi<Plan[]>("/api/public/plans", 300),
  ]);
  const h = home ?? { featuredMakers: [], trendingProducts: [], recentProducts: [], popularCategories: [] };
  const heroImages = h.recentProducts.filter((p) => p.imageUrl).slice(0, 4);

  return (
    <>
      <section className="relative overflow-hidden">
        <div aria-hidden className="pointer-events-none absolute -right-32 -top-32 h-96 w-96 rounded-full bg-[#ecd3c6]/60 blur-3xl" />
        <div aria-hidden className="pointer-events-none absolute -bottom-40 -left-24 h-96 w-96 rounded-full bg-sage-soft blur-3xl" />
        <Container className="relative grid items-center gap-10 py-16 sm:py-24 lg:grid-cols-[1.4fr_1fr]">
          <div className="max-w-3xl animate-fade-up">
            <h1 className="text-4xl leading-[1.08] sm:text-6xl">{t.home.heroTitle}</h1>
            <p className="mt-6 max-w-2xl text-lg text-ink-soft">{t.home.heroText}</p>
            <div className="mt-8 flex flex-wrap gap-3">
              <ButtonLink href="/register?type=maker" variant="sage" className="px-6">{t.home.ctaPrimary}</ButtonLink>
              <ButtonLink href="/makers" variant="secondary" className="px-6">{t.home.ctaSecondary}</ButtonLink>
            </div>
            <div className="mt-10 max-w-xl">
              <SearchBar large label={t.common.search} placeholder={t.home.searchPlaceholder} />
            </div>
          </div>
          {heroImages.length > 0 && (
            <div aria-hidden className="hidden grid-cols-2 gap-4 lg:grid">
              {heroImages.map((p, i) => (
                <img key={p.id} src={p.imageUrl!} alt="" className={`aspect-[4/5] w-full rounded-[var(--radius-card)] object-cover shadow-[var(--shadow-lift)] ${i === 1 ? "mt-10" : ""} ${i === 2 ? "-mt-10" : ""}`} />
              ))}
            </div>
          )}
        </Container>
      </section>

      <Container className="space-y-20">
        <section>
          <SectionTitle title={t.home.featuredMakers} action={<Link href="/makers" className="text-sm underline">{t.common.viewAll}</Link>} />
          {h.featuredMakers.length ? (
            <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              {h.featuredMakers.slice(0, 4).map((m) => <li key={m.id}><MakerCard maker={m} t={t} /></li>)}
            </ul>
          ) : <EmptyState title={t.home.emptyMakers} action={<ButtonLink href="/register?type=maker">{t.home.ctaPrimary}</ButtonLink>} />}
        </section>

        {h.trendingProducts.length > 0 && (
          <section>
            <SectionTitle title={t.home.trending} />
            <ProductGrid products={h.trendingProducts} locale={locale} t={t} />
          </section>
        )}

        <section>
          <SectionTitle title={t.home.categories} action={<Link href="/marketplace" className="text-sm underline">{t.common.viewAll}</Link>} />
          <ul className="flex flex-wrap gap-2">
            {h.popularCategories.map((c) => (
              <li key={c.id}>
                <Link href={`/categories/${c.slug}`} className="inline-flex min-h-[44px] items-center gap-2 rounded-full border border-line bg-paper px-4 text-sm hover:border-terracotta hover:text-terracotta-dark">
                  {c.name} <span className="text-xs text-muted">{c.productCount}</span>
                </Link>
              </li>
            ))}
          </ul>
        </section>

        <section>
          <SectionTitle title={t.home.recent} action={<Link href="/marketplace?sort=newest" className="text-sm underline">{t.common.viewAll}</Link>} />
          {h.recentProducts.length ? <ProductGrid products={h.recentProducts} locale={locale} t={t} />
            : <EmptyState title={t.home.emptyProducts} />}
        </section>

        <section aria-labelledby="how" className="rounded-[2rem] bg-sand/70 px-6 py-12 sm:px-12">
          <h2 id="how" className="text-3xl">{t.home.howTitle}</h2>
          <ol className="mt-8 grid gap-6 md:grid-cols-3">
            {t.home.steps.map((s, i) => (
              <li key={s.title} className="rounded-2xl bg-paper p-6 shadow-[var(--shadow-soft)]">
                <span className="grid h-10 w-10 place-items-center rounded-full bg-terracotta font-serif text-lg text-white">{i + 1}</span>
                <h3 className="mt-4 text-xl">{s.title}</h3>
                <p className="mt-2 text-sm text-ink-soft">{s.text}</p>
              </li>
            ))}
          </ol>
        </section>

        <section>
          <SectionTitle title={t.home.examplesTitle} subtitle={t.home.examplesText} />
          <ul className="grid gap-5 md:grid-cols-3">
            {TEMPLATE_PREVIEWS.map((tpl) => (
              <li key={tpl.name}>
                <Card className="overflow-hidden">
                  <div className="p-5" style={{ background: tpl.bg, color: "#fff", fontFamily: tpl.font }}>
                    <div className="flex items-center justify-between text-xs opacity-80"><span>{tpl.title}</span><span>☰</span></div>
                    <p className="mt-8 text-2xl">{tpl.title}</p>
                    <span className="mt-4 inline-block rounded-full px-3 py-1 text-xs" style={{ background: tpl.accent }}>{t.storefront.shop}</span>
                  </div>
                  <div className="grid grid-cols-3 gap-2 p-4">
                    {[0, 1, 2].map((i) => <div key={i} className="aspect-square rounded-lg bg-sand" />)}
                  </div>
                  <div className="flex items-center justify-between border-t border-line px-4 py-3 text-sm">
                    <span className="font-medium">{tpl.name}</span><Badge>{t.common.demo}</Badge>
                  </div>
                </Card>
              </li>
            ))}
          </ul>
        </section>

        <section>
          <SectionTitle title={t.home.benefitsTitle} />
          <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {t.home.benefits.map((b) => (
              <li key={b.title} className="rounded-2xl border border-line bg-paper p-6">
                <h3 className="text-lg">{b.title}</h3>
                <p className="mt-2 text-sm text-ink-soft">{b.text}</p>
              </li>
            ))}
          </ul>
        </section>

        {plans && plans.length > 0 && (
          <section>
            <SectionTitle title={t.home.pricingTitle} action={<Link href="/pricing" className="text-sm underline">{t.nav.pricing}</Link>} />
            <PlanCards plans={plans} locale={locale} t={t} />
          </section>
        )}

        <section>
          <SectionTitle title={t.home.testimonialsTitle} subtitle={t.home.testimonialsDemo} />
          <ul className="grid gap-5 md:grid-cols-2">
            {t.home.testimonials.map((q) => (
              <li key={q.author}>
                <figure className="h-full rounded-2xl border border-dashed border-line bg-paper p-6">
                  <Badge>{t.common.demo}</Badge>
                  <blockquote className="mt-3 font-serif text-xl">“{q.quote}”</blockquote>
                  <figcaption className="mt-3 text-sm text-muted">— {q.author}</figcaption>
                </figure>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="faq" className="max-w-3xl">
          <h2 id="faq" className="text-3xl">{t.home.faqTitle}</h2>
          <div className="mt-6 divide-y divide-line rounded-2xl border border-line bg-paper">
            {t.home.faq.map((f) => (
              <details key={f.q} className="group p-5">
                <summary className="flex cursor-pointer list-none items-center justify-between gap-4 font-medium">
                  {f.q}<span aria-hidden className="text-muted transition group-open:rotate-45">+</span>
                </summary>
                <p className="mt-3 text-ink-soft">{f.a}</p>
              </details>
            ))}
          </div>
        </section>
      </Container>
    </>
  );
}
