import type { Metadata } from "next";
import { MakerCard } from "@/components/catalog/MakerCard";
import { Pagination } from "@/components/catalog/Pagination";
import { Container, EmptyState } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { getI18n } from "@/lib/i18n/server";
import type { Category, MakerCard as Card, Page } from "@/lib/types";

export async function generateMetadata(): Promise<Metadata> {
  const { t } = await getI18n();
  return { title: t.makers.title, description: t.makers.subtitle, alternates: { canonical: "/makers" } };
}

type Params = { q?: string; city?: string; category?: string; page?: string };

export default async function MakersPage({ searchParams }: { searchParams: Promise<Params> }) {
  const params = await searchParams;
  const { locale, t } = await getI18n();
  const page = Math.max(0, Number.parseInt(params.page ?? "0", 10) || 0);
  const qs = new URLSearchParams({ page: String(page), size: "24" });
  if (params.q) qs.set("q", params.q);
  if (params.city) qs.set("city", params.city);
  if (params.category) qs.set("category", params.category);
  const [makers, categories, cities] = await Promise.all([
    publicApi<Page<Card>>(`/api/public/makers?${qs}`, 60),
    publicApi<Category[]>(`/api/public/categories?locale=${locale}`, 300),
    publicApi<string[]>("/api/public/cities", 300),
  ]);

  return (
    <Container className="py-10">
      <h1 className="text-4xl">{t.makers.title}</h1>
      <p className="mt-2 text-muted">{t.makers.subtitle}</p>
      <form method="get" action="/makers" className="mt-6 grid gap-3 sm:grid-cols-[1fr_200px_200px_auto]">
        <label className="sr-only" htmlFor="maker-q">{t.common.search}</label>
        <input id="maker-q" name="q" type="search" defaultValue={params.q} placeholder={t.makers.searchPlaceholder}
          className="h-12 rounded-full border border-line bg-paper px-5" />
        <select name="city" defaultValue={params.city ?? ""} aria-label={t.market.city} className="h-12 rounded-full border border-line bg-paper px-4">
          <option value="">{t.market.anyCity}</option>
          {(cities ?? []).map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <select name="category" defaultValue={params.category ?? ""} aria-label={t.market.category} className="h-12 rounded-full border border-line bg-paper px-4">
          <option value="">{t.market.allCategories}</option>
          {(categories ?? []).map((c) => <option key={c.id} value={c.slug}>{c.name}</option>)}
        </select>
        <button type="submit" className="h-12 rounded-full bg-ink px-6 text-sm text-cream">{t.common.search}</button>
      </form>
      <div className="mt-8">
        {makers?.items.length ? (
          <>
            <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              {makers.items.map((m) => <li key={m.id}><MakerCard maker={m} t={t} /></li>)}
            </ul>
            <Pagination page={page} totalPages={makers.totalPages} params={params} basePath="/makers" t={t} />
          </>
        ) : <EmptyState title={t.makers.noResults} />}
      </div>
    </Container>
  );
}
