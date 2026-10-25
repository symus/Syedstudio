import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Gallery } from "@/components/catalog/Gallery";
import { ProductGrid } from "@/components/catalog/ProductCard";
import { ReportButton } from "@/components/forms/ReportButton";
import { Badge, ButtonLink, Container } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { brand } from "@/lib/config";
import { formatPrice } from "@/lib/format";
import { fmt } from "@/lib/i18n";
import { getI18n } from "@/lib/i18n/server";
import type { PublicProduct } from "@/lib/types";

type Params = Promise<{ maker: string; product: string }>;

async function load(maker: string, product: string, locale: string) {
  return publicApi<PublicProduct>(`/api/public/products/${encodeURIComponent(maker)}/${encodeURIComponent(product)}?locale=${locale}`, 60);
}

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { maker, product } = await params;
  const { locale } = await getI18n();
  const p = await load(maker, product, locale);
  if (!p) return {};
  const description = (p.description ?? `${p.name} — ${p.maker.businessName}`).slice(0, 160);
  return {
    title: `${p.name} — ${p.maker.businessName}`,
    description,
    alternates: { canonical: `/p/${maker}/${product}` },
    openGraph: { title: p.name, description, images: p.images[0] ? [{ url: p.images[0].url }] : undefined },
  };
}

export default async function ProductPage({ params }: { params: Params }) {
  const { maker, product } = await params;
  const { locale, t } = await getI18n();
  const p = await load(maker, product, locale);
  if (!p) notFound();
  const price = formatPrice(p.priceMinor, p.currency, locale);
  const inquiryHref = `/makers/${p.maker.slug}/inquiry?product=${p.id}`;

  // Structured data only with facts we actually have (no invented ratings or shipping promises).
  const jsonLd = {
    "@context": "https://schema.org", "@type": "Product", name: p.name, description: p.description ?? undefined,
    image: p.images.map((i) => `${brand.siteUrl}${i.url}`), brand: { "@type": "Brand", name: p.maker.businessName },
    ...(p.priceMinor !== null && {
      offers: { "@type": "Offer", price: (p.priceMinor / 100).toFixed(2), priceCurrency: p.currency,
        availability: p.available ? "https://schema.org/InStock" : "https://schema.org/OutOfStock",
        url: `${brand.siteUrl}/p/${p.maker.slug}/${p.slug}` },
    }),
  };

  return (
    <Container className="py-10">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd).replace(/</g, "\\u003c") }} />
      <nav aria-label="Breadcrumb" className="mb-6 text-sm text-muted">
        <Link href="/marketplace" className="hover:underline">{t.nav.marketplace}</Link>
        {p.categorySlug && <> / <Link href={`/categories/${p.categorySlug}`} className="hover:underline">{p.categoryName}</Link></>}
      </nav>
      <div className="grid gap-10 lg:grid-cols-2">
        <Gallery images={p.images} name={p.name} />
        <div>
          <h1 className="text-3xl sm:text-4xl">{p.name}</h1>
          <p className="mt-2 text-ink-soft">
            {t.product.by} <Link href={`/makers/${p.maker.slug}`} className="font-medium underline">{p.maker.businessName}</Link>
            {p.maker.city && <> · {p.maker.city}</>}
          </p>
          <p className="mt-5 font-serif text-3xl">{price ?? <span className="text-xl text-muted">{t.common.priceOnRequest}</span>}</p>
          <div className="mt-3 flex flex-wrap gap-2">
            <Badge tone={p.availabilityType === "READY_MADE" ? "success" : "accent"}>
              {p.availabilityType === "READY_MADE" ? t.product.readyMade : t.product.madeToOrder}
            </Badge>
            {p.customizable && <Badge tone="accent">{t.product.personalisable}</Badge>}
            {!p.available && <Badge tone="warning">{t.product.unavailable}</Badge>}
          </div>
          <div className="mt-6 flex flex-wrap gap-3">
            <ButtonLink href={inquiryHref} variant="sage">{t.product.askMaker}</ButtonLink>
            {p.maker.hasStorefront && <ButtonLink href={`/${p.maker.slug}`} variant="secondary">{t.product.visitStore}</ButtonLink>}
          </div>

          {p.description && <div className="mt-8 whitespace-pre-line leading-relaxed text-ink-soft">{p.description}</div>}

          <dl className="mt-8 divide-y divide-line rounded-2xl border border-line bg-paper text-sm">
            {p.materials && <Row label={t.product.materials} value={p.materials} />}
            {p.dimensions && <Row label={t.product.dimensions} value={p.dimensions} />}
            {p.productionDays !== null && <Row label={t.product.productionTime} value={fmt(t.product.days, { n: p.productionDays })} />}
            <Row label={t.product.shipping} value={p.shippingInfo ?? t.product.noShipping} />
            {p.occasions.length > 0 && <Row label={t.product.occasions} value={p.occasions.map((o) => t.occasions[o]).join(", ")} />}
            {p.tags.length > 0 && <Row label={t.product.tags} value={p.tags.join(", ")} />}
          </dl>

          {p.options.length > 0 && (
            <div className="mt-6">
              <h2 className="text-lg">{t.product.options}</h2>
              <ul className="mt-2 space-y-1 text-sm text-ink-soft">
                {p.options.map((o) => <li key={o.id}>• {o.name}{o.choices.length ? `: ${o.choices.join(" / ")}` : ""}</li>)}
              </ul>
            </div>
          )}

          <div className="mt-8 rounded-2xl bg-sand/70 p-5 text-sm">
            <p>{fmt(t.product.sellerNotice, { maker: p.maker.businessName })}</p>
            {p.maker.seller && (
              <div className="mt-3 text-ink-soft">
                <p className="font-medium text-ink">{t.product.seller}</p>
                {[p.maker.seller.legalName, p.maker.seller.taxId && `NIP: ${p.maker.seller.taxId}`, p.maker.seller.businessAddress].filter(Boolean).map((l) => <p key={l as string}>{l}</p>)}
              </div>
            )}
          </div>
          <div className="mt-4"><ReportButton targetType="PRODUCT" targetId={p.id} label={t.product.report} /></div>
        </div>
      </div>

      {p.moreFromMaker.length > 0 && (
        <section className="mt-16">
          <h2 className="mb-5 text-2xl">{fmt(t.product.moreFromMaker, { maker: p.maker.businessName })}</h2>
          <ProductGrid products={p.moreFromMaker} locale={locale} t={t} />
        </section>
      )}
      {p.related.length > 0 && (
        <section className="mt-16">
          <h2 className="mb-5 text-2xl">{t.product.related}</h2>
          <ProductGrid products={p.related} locale={locale} t={t} />
        </section>
      )}
    </Container>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid grid-cols-[140px_1fr] gap-3 px-4 py-3">
      <dt className="text-muted">{label}</dt><dd className="whitespace-pre-line">{value}</dd>
    </div>
  );
}
