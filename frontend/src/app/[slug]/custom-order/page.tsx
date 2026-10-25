import type { Metadata } from "next";
import { liveLinks, StorefrontPage } from "@/components/storefront/Storefront";
import { loadStorefront, storefrontMetadata } from "@/components/storefront/load";
import { dictionaryFor } from "@/lib/i18n";

type Params = Promise<{ slug: string }>;

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { slug } = await params;
  const view = await loadStorefront(slug);
  return storefrontMetadata(slug, dictionaryFor(view.config.language).storefront.customOrder, "/custom-order");
}

export default async function Page({ params, searchParams }: { params: Params; searchParams: Promise<{ product?: string }> }) {
  const { slug } = await params;
  const { product } = await searchParams;
  return <StorefrontPage view={await loadStorefront(slug)} links={liveLinks(slug)} page="custom-order" productId={product} />;
}
