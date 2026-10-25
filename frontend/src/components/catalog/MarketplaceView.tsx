import Link from "next/link";
import { Pagination } from "@/components/catalog/Pagination";
import { ProductGrid } from "@/components/catalog/ProductCard";
import { SearchBar } from "@/components/catalog/SearchBar";
import { Container, EmptyState } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { fmt, type Dictionary } from "@/lib/i18n";
import { OCCASIONS, type Category, type Locale, type Page, type ProductCard } from "@/lib/types";

export type MarketParams = {
  q?: string; category?: string; city?: string; minPrice?: string; maxPrice?: string; occasion?: string;
  availability?: string; customizable?: string; sort?: string; page?: string;
};

const toMinor = (v?: string) => {
  const n = v ? Number.parseFloat(v.replace(",", ".")) : Number.NaN;
  return Number.isFinite(n) && n >= 0 ? String(Math.round(n * 100)) : undefined;
};

/** Server-rendered marketplace: filters are a plain GET form, so results are crawlable and shareable. */
export async function MarketplaceView({ params, locale, t, title, subtitle, fixedCategory }: {
  params: MarketParams; locale: Locale; t: Dictionary; title: string; subtitle?: string; fixedCategory?: string;
}) {
  const category = fixedCategory ?? params.category;
  const page = Math.max(0, Number.parseInt(params.page ?? "0", 10) || 0);
  const qs = new URLSearchParams();
  const set = (k: string, v?: string) => { if (v) qs.set(k, v); };
  set("q", params.q); set("category", category); set("city", params.city);
  set("minPrice", toMinor(params.minPrice)); set("maxPrice", toMinor(params.maxPrice));
  set("occasion", params.occasion); set("availability", params.availability);
  set("customizable", params.customizable === "true" ? "true" : undefined); set("sort", params.sort);
  qs.set("page", String(page)); qs.set("size", "24");

  const [results, categories, cities] = await Promise.all([
    publicApi<Page<ProductCard>>(`/api/public/products?${qs}`, 30),
    publicApi<Category[]>(`/api/public/categories?locale=${locale}`, 300),
    publicApi<string[]>("/api/public/cities", 300),
  ]);
  const items = results?.items ?? [];
  const basePath = fixedCategory ? `/categories/${fixedCategory}` : "/marketplace";
  const hasFilters = Boolean(params.q || params.city || params.minPrice || params.maxPrice || params.occasion || params.availability || params.customizable || (!fixedCategory && params.category));

  return (
    <Container className="py-10">
      <h1 className="text-4xl">{title}</h1>
      {subtitle && <p className="mt-2 text-muted">{subtitle}</p>}
      <div className="mt-6 max-w-2xl">
        <SearchBar action={basePath} label={t.common.search} placeholder={t.home.searchPlaceholder} defaultValue={params.q}
          hidden={{ sort: params.sort }} />
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-[260px_1fr]">
        <aside>
          <details className="group rounded-2xl border border-line bg-paper" open>
            <summary className="cursor-pointer list-none p-4 font-medium lg:hidden">{t.market.filters}</summary>
            <form method="get" action={basePath} className="space-y-4 p-4 pt-0 lg:pt-4">
              {params.q && <input type="hidden" name="q" value={params.q} />}
              {!fixedCategory && (
                <FilterSelect name="category" label={t.market.category} value={params.category}
                  options={[{ value: "", label: t.market.allCategories }, ...(categories ?? []).map((c) => ({ value: c.slug, label: c.name }))]} />
              )}
              <FilterSelect name="city" label={t.market.city} value={params.city}
                options={[{ value: "", label: t.market.anyCity }, ...(cities ?? []).map((c) => ({ value: c, label: c }))]} />
              <div className="grid grid-cols-2 gap-2">
                <FilterInput name="minPrice" label={t.market.minPrice} value={params.minPrice} />
                <FilterInput name="maxPrice" label={t.market.maxPrice} value={params.maxPrice} />
              </div>
              <FilterSelect name="occasion" label={t.market.occasion} value={params.occasion}
                options={[{ value: "", label: t.market.anyOccasion }, ...OCCASIONS.map((o) => ({ value: o, label: t.occasions[o] }))]} />
              <FilterSelect name="availability" label={t.market.availability} value={params.availability}
                options={[{ value: "", label: t.market.anyAvailability }, { value: "available", label: t.market.availableOnly },
                  { value: "in_stock", label: t.market.inStock }, { value: "made_to_order", label: t.market.madeToOrder }]} />
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" name="customizable" value="true" defaultChecked={params.customizable === "true"} className="h-5 w-5 accent-terracotta" />
                {t.market.customOrders}
              </label>
              <FilterSelect name="sort" label={t.market.sort} value={params.sort}
                options={[{ value: "", label: t.market.sortRelevance }, { value: "newest", label: t.market.sortNewest },
                  { value: "price_asc", label: t.market.sortPriceAsc }, { value: "price_desc", label: t.market.sortPriceDesc }]} />
              <div className="flex gap-2 pt-2">
                <button type="submit" className="min-h-[44px] flex-1 rounded-full bg-ink px-4 text-sm text-cream">{t.market.apply}</button>
                {hasFilters && <Link href={basePath} className="grid min-h-[44px] place-items-center rounded-full border border-line px-4 text-sm">{t.market.clear}</Link>}
              </div>
            </form>
          </details>
        </aside>

        <section aria-live="polite">
          <p className="mb-4 text-sm text-muted">{fmt(t.market.results, { count: results?.totalItems ?? 0 })}</p>
          {items.length ? (
            <>
              <ProductGrid products={items} locale={locale} t={t} />
              <Pagination page={page} totalPages={results?.totalPages ?? 0} params={params} basePath={basePath} t={t} />
            </>
          ) : (
            <EmptyState title={t.market.noResults} text={t.market.noResultsHint}
              action={
                <div className="flex flex-wrap justify-center gap-2">
                  {(categories ?? []).slice(0, 6).map((c) => (
                    <Link key={c.id} href={`/categories/${c.slug}`} className="rounded-full border border-line px-3 py-1.5 text-sm hover:border-terracotta">{c.name}</Link>
                  ))}
                </div>
              } />
          )}
        </section>
      </div>
    </Container>
  );
}

function FilterSelect({ name, label, value, options }: { name: string; label: string; value?: string; options: { value: string; label: string }[] }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium">{label}</span>
      <select name={name} defaultValue={value ?? ""} className="w-full rounded-xl border border-line bg-paper px-3 py-2.5">
        {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </label>
  );
}

function FilterInput({ name, label, value }: { name: string; label: string; value?: string }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium">{label}</span>
      <input name={name} defaultValue={value} inputMode="decimal" className="w-full rounded-xl border border-line bg-paper px-3 py-2.5" />
    </label>
  );
}
