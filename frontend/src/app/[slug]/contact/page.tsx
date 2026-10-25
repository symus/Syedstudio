import type { Metadata } from "next";
import { liveLinks, StorefrontPage } from "@/components/storefront/Storefront";
import { loadStorefront, storefrontMetadata } from "@/components/storefront/load";
import { dictionaryFor } from "@/lib/i18n";

type Params = Promise<{ slug: string }>;

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { slug } = await params;
  const view = await loadStorefront(slug);
  return storefrontMetadata(slug, dictionaryFor(view.config.language).storefront.contact, "/contact");
}

export default async function Page({ params }: { params: Params }) {
  const { slug } = await params;
  return <StorefrontPage view={await loadStorefront(slug)} links={liveLinks(slug)} page="contact" />;
}
