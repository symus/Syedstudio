import type { MetadataRoute } from "next";
import { publicApi } from "@/lib/api";
import { brand } from "@/lib/config";

export const revalidate = 3600;

type SitemapData = { storefronts: string[]; makers: string[]; categories: string[]; products: string[] };

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const base = brand.siteUrl;
  const data = await publicApi<SitemapData>("/api/public/sitemap", 3600).catch(() => null);
  const staticPages = ["", "/marketplace", "/makers", "/pricing", "/legal/privacy", "/legal/terms"];
  return [
    ...staticPages.map((p) => ({ url: `${base}${p}`, changeFrequency: "daily" as const })),
    ...(data?.categories ?? []).map((s) => ({ url: `${base}/categories/${s}` })),
    ...(data?.makers ?? []).map((s) => ({ url: `${base}/makers/${s}` })),
    ...(data?.storefronts ?? []).map((s) => ({ url: `${base}/${s}` })),
    ...(data?.products ?? []).map((p) => ({ url: `${base}/p/${p}` })),
  ];
}
