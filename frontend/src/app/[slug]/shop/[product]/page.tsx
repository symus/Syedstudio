import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { liveLinks, StorefrontPage } from "@/components/storefront/Storefront";
import { loadStorefront } from "@/components/storefront/load";
import { publicApi } from "@/lib/api";
import type { PublicProduct } from "@/lib/types";

type Params = Promise<{ slug: string; product: string }>;

async function loadProduct(slug: string, product: string, locale: string) {
  return publicApi<PublicProduct>(`/api/public/products/${slug}/${encodeURIComponent(product)}?locale=${locale}`, 60);
}

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { slug, product } = await params;
  const view = await loadStorefront(slug);
  const p = await loadProduct(slug, product, view.config.language);
  if (!p) return {};
  return {
    title: { absolute: `${p.name} — ${view.maker.businessName}` },
    description: (p.description ?? p.name).slice(0, 160),
    // The marketplace page is the canonical copy of the listing to avoid duplicate content.
    alternates: { canonical: `/p/${slug}/${product}` },
    openGraph: { images: p.images[0] ? [{ url: p.images[0].url }] : undefined },
  };
}

export default async function Page({ params }: { params: Params }) {
  const { slug, product } = await params;
  const view = await loadStorefront(slug);
  const p = await loadProduct(slug, product, view.config.language);
  if (!p) notFound();
  return <StorefrontPage view={view} links={liveLinks(slug)} page="product" product={p} />;
}
