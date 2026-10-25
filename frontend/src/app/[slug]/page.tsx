import type { Metadata } from "next";
import { liveLinks, StorefrontPage } from "@/components/storefront/Storefront";
import { loadStorefront, storefrontMetadata } from "@/components/storefront/load";

type Params = Promise<{ slug: string }>;

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  return storefrontMetadata((await params).slug);
}

export default async function StorefrontHomePage({ params }: { params: Params }) {
  const { slug } = await params;
  const view = await loadStorefront(slug);
  return <StorefrontPage view={view} links={liveLinks(slug)} page="home" />;
}
