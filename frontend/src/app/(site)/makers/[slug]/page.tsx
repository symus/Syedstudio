import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ProductGrid } from "@/components/catalog/ProductCard";
import { ReportButton } from "@/components/forms/ReportButton";
import { Badge, ButtonLink, Container, EmptyState } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { brand } from "@/lib/config";
import { formatDate } from "@/lib/format";
import { fmt } from "@/lib/i18n";
import { getI18n } from "@/lib/i18n/server";
import type { Page, ProductCard, PublicMaker } from "@/lib/types";

type Params = Promise<{ slug: string }>;

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { slug } = await params;
  const m = await publicApi<PublicMaker>(`/api/public/makers/${encodeURIComponent(slug)}`);
  if (!m) return {};
  const description = (m.tagline ?? m.bio ?? m.businessName).slice(0, 160);
  return { title: m.businessName, description, alternates: { canonical: `/makers/${slug}` },
    openGraph: { title: m.businessName, description, images: m.coverUrl ? [{ url: m.coverUrl }] : undefined } };
}

export default async function MakerPage({ params }: { params: Params }) {
  const { slug } = await params;
  const { locale, t } = await getI18n();
  const [m, products] = await Promise.all([
    publicApi<PublicMaker>(`/api/public/makers/${encodeURIComponent(slug)}`),
    publicApi<Page<ProductCard>>(`/api/public/products?maker=${encodeURIComponent(slug)}&sort=newest&size=48`, 60),
  ]);
  if (!m) notFound();
  const social = Object.entries(m.social).filter(([, url]) => Boolean(url)) as [string, string][];

  return (
    <>
      <div className="h-48 bg-gradient-to-br from-sand to-[#ead9cc] sm:h-64">
        {m.coverUrl && <img src={m.coverUrl} alt="" className="h-full w-full object-cover" />}
      </div>
      <Container>
        <div className="-mt-12 flex flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex items-end gap-4">
            <div className="h-24 w-24 shrink-0 overflow-hidden rounded-full border-4 border-cream bg-sand">
              {m.logoUrl ? <img src={m.logoUrl} alt={m.businessName} className="h-full w-full object-cover" />
                : <span className="grid h-full place-items-center font-serif text-3xl text-terracotta">{m.businessName.charAt(0)}</span>}
            </div>
            <div className="pb-1">
              <h1 className="text-3xl sm:text-4xl">{m.businessName}</h1>
              <p className="text-ink-soft">{[m.city, m.region].filter(Boolean).join(", ")}</p>
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            {m.hasStorefront && <ButtonLink href={`/${m.slug}`} variant="secondary">{t.product.visitStore}</ButtonLink>}
            <ButtonLink href={`/makers/${m.slug}/inquiry`} variant="sage">{t.product.askMaker}</ButtonLink>
          </div>
        </div>

        <div className="mt-10 grid gap-10 lg:grid-cols-[1fr_320px]">
          <section>
            <h2 className="mb-5 text-2xl">{t.makers.products_}</h2>
            {products?.items.length ? <ProductGrid products={products.items} locale={locale} t={t} />
              : <EmptyState title={t.makers.noProducts} />}
          </section>
          <aside className="space-y-6">
            {m.tagline && <p className="font-serif text-xl">{m.tagline}</p>}
            {m.bio && <div><h2 className="text-lg">{t.makers.about}</h2><p className="mt-2 whitespace-pre-line text-sm text-ink-soft">{m.bio}</p></div>}
            {m.acceptsCustomOrders && <Badge tone="accent">{t.makers.customOrders}</Badge>}
            {(m.contactEmail || m.phone) && (
              <div className="text-sm"><h2 className="text-lg">{t.makers.contact}</h2>
                {m.contactEmail && <p className="mt-2"><a className="underline" href={`mailto:${m.contactEmail}`}>{m.contactEmail}</a></p>}
                {m.phone && <p><a className="underline" href={`tel:${m.phone}`}>{m.phone}</a></p>}
              </div>
            )}
            {social.length > 0 && (
              <div className="text-sm"><h2 className="text-lg">{t.makers.social}</h2>
                <ul className="mt-2 flex flex-wrap gap-2">
                  {social.map(([name, url]) => <li key={name}><a href={url} rel="noopener noreferrer nofollow" target="_blank" className="rounded-full border border-line px-3 py-1 capitalize hover:border-ink/40">{name}</a></li>)}
                </ul>
              </div>
            )}
            {m.seller && (
              <div className="rounded-2xl bg-sand/70 p-4 text-sm text-ink-soft"><p className="font-medium text-ink">{t.product.seller}</p>
                {[m.seller.legalName, m.seller.taxId && `NIP: ${m.seller.taxId}`, m.seller.businessAddress].filter(Boolean).map((l) => <p key={l as string}>{l}</p>)}
              </div>
            )}
            {m.memberSince && <p className="text-xs text-muted">{fmt(t.makers.memberSince, { brand: brand.name, date: formatDate(m.memberSince, locale) })}</p>}
            <ReportButton targetType="MAKER" targetId={m.id} label={t.product.report} />
            <p><Link href="/makers" className="text-sm underline">← {t.makers.title}</Link></p>
          </aside>
        </div>
      </Container>
    </>
  );
}
