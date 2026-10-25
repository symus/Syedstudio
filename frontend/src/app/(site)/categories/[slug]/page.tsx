import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { MarketplaceView, type MarketParams } from "@/components/catalog/MarketplaceView";
import { publicApi } from "@/lib/api";
import { getI18n } from "@/lib/i18n/server";
import type { Category } from "@/lib/types";

async function findCategory(slug: string, locale: string) {
  const categories = await publicApi<Category[]>(`/api/public/categories?locale=${locale}`, 300);
  return categories?.find((c) => c.slug === slug) ?? null;
}

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const { locale, t } = await getI18n();
  const category = await findCategory(slug, locale);
  if (!category) return {};
  return { title: category.name, description: `${category.name} — ${t.market.subtitle}`, alternates: { canonical: `/categories/${slug}` } };
}

export default async function CategoryPage({ params, searchParams }: { params: Promise<{ slug: string }>; searchParams: Promise<MarketParams> }) {
  const { slug } = await params;
  const { locale, t } = await getI18n();
  const category = await findCategory(slug, locale);
  if (!category) notFound();
  return <MarketplaceView params={await searchParams} locale={locale} t={t} title={category.name} subtitle={t.market.subtitle} fixedCategory={slug} />;
}
