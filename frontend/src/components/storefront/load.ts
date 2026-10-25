import "server-only";
import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { cache } from "react";
import { publicApi } from "@/lib/api";
import type { StorefrontView } from "@/lib/types";

/** Published storefront for a slug, de-duplicated across metadata + page within a request. */
export const loadStorefront = cache(async (slug: string): Promise<StorefrontView> => {
  if (!/^[a-z0-9-]{3,60}$/.test(slug)) notFound();
  const view = await publicApi<StorefrontView>(`/api/public/storefronts/${slug}`, 60);
  if (!view) notFound();
  return view;
});

export async function storefrontMetadata(slug: string, suffix?: string, path = ""): Promise<Metadata> {
  const view = await loadStorefront(slug);
  const title = view.config.seoTitle || view.maker.businessName;
  const description = view.config.seoDescription || view.maker.tagline || view.config.heroSubtitle || undefined;
  const image = view.heroImageUrl ?? view.maker.coverUrl;
  return {
    title: { absolute: suffix ? `${suffix} — ${title}` : title },
    description,
    alternates: { canonical: `/${slug}${path}` },
    openGraph: { title, description, type: "website", images: image ? [{ url: image }] : undefined },
  };
}
