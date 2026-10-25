import type { Metadata } from "next";
import { MarketplaceView, type MarketParams } from "@/components/catalog/MarketplaceView";
import { getI18n } from "@/lib/i18n/server";

export async function generateMetadata(): Promise<Metadata> {
  const { t } = await getI18n();
  return { title: t.market.title, description: t.market.subtitle, alternates: { canonical: "/marketplace" } };
}

export default async function MarketplacePage({ searchParams }: { searchParams: Promise<MarketParams> }) {
  const { locale, t } = await getI18n();
  return <MarketplaceView params={await searchParams} locale={locale} t={t} title={t.market.title} subtitle={t.market.subtitle} />;
}
