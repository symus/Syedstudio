"use client";

import Link from "next/link";
import { useState } from "react";
import { Badge, ButtonLink, EmptyState, cx } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { formatDate, formatPrice } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import type { MakerProduct, Page, ProductStatus } from "@/lib/types";

const FILTERS: (ProductStatus | "")[] = ["", "PUBLISHED", "DRAFT", "ARCHIVED"];

export default function ProductsPage() {
  const { t, locale } = useI18n();
  const [status, setStatus] = useState<ProductStatus | "">("");
  const [page, setPage] = useState(0);
  const { data, error, loading, reload } = useApi<Page<MakerProduct>>(`/api/maker/products?page=${page}&size=20${status ? `&status=${status}` : ""}`);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl">{t.products.title}</h1>
        <ButtonLink href="/dashboard/products/new" variant="sage">{t.products.add}</ButtonLink>
      </div>
      <div role="tablist" className="flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button key={f || "all"} role="tab" aria-selected={status === f} type="button" onClick={() => { setStatus(f); setPage(0); }}
            className={cx("min-h-[40px] rounded-full border px-4 text-sm", status === f ? "border-ink bg-ink text-cream" : "border-line bg-paper")}>
            {f ? t.products.status[f] : t.common.all}
          </button>
        ))}
      </div>
      <LoadState loading={loading} error={error} onRetry={reload}>
        {data?.items.length ? (
          <>
            <ul className="divide-y divide-line overflow-hidden rounded-2xl border border-line bg-paper">
              {data.items.map((p) => (
                <li key={p.id}>
                  <Link href={`/dashboard/products/${p.id}`} className="flex items-center gap-4 p-4 hover:bg-sand/50">
                    <div className="h-16 w-16 shrink-0 overflow-hidden rounded-xl bg-sand">
                      {p.images[0] && <img src={p.images[0].url} alt="" className="h-full w-full object-cover" />}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium">{p.name}</p>
                      <p className="text-sm text-muted">{formatPrice(p.priceMinor, p.currency, locale) ?? t.common.priceOnRequest} · {formatDate(p.updatedAt, locale)}</p>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <Badge tone={p.status === "PUBLISHED" ? "success" : "neutral"}>{t.products.status[p.status]}</Badge>
                      {p.moderationStatus === "HIDDEN" && <Badge tone="danger">{t.products.hidden}</Badge>}
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
            {data.totalPages > 1 && (
              <div className="flex justify-center gap-3 text-sm">
                <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">← {t.common.previous}</button>
                <span className="py-2 text-muted">{page + 1} / {data.totalPages}</span>
                <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">{t.common.next} →</button>
              </div>
            )}
          </>
        ) : <EmptyState title={t.products.empty} text={t.products.emptyHint} action={<ButtonLink href="/dashboard/products/new" variant="sage">{t.products.add}</ButtonLink>} />}
      </LoadState>
    </div>
  );
}
