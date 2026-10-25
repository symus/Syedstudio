import Link from "next/link";
import type { Dictionary } from "@/lib/i18n";

/** Link-based pagination: crawlable, works without JS, keeps all current filters. */
export function Pagination({ page, totalPages, params, basePath, t }: {
  page: number; totalPages: number; params: Record<string, string | undefined>; basePath: string; t: Dictionary;
}) {
  if (totalPages <= 1) return null;
  const href = (p: number) => {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v && k !== "page") qs.set(k, v); });
    if (p > 0) qs.set("page", String(p));
    const s = qs.toString();
    return s ? `${basePath}?${s}` : basePath;
  };
  return (
    <nav aria-label="Pagination" className="mt-10 flex items-center justify-center gap-3 text-sm">
      {page > 0 ? <Link className="rounded-full border border-line bg-paper px-4 py-2 hover:border-ink/40" href={href(page - 1)} rel="prev">← {t.common.previous}</Link>
        : <span className="px-4 py-2 text-muted/60">← {t.common.previous}</span>}
      <span className="text-muted">{t.common.page} {page + 1} {t.common.of} {totalPages}</span>
      {page + 1 < totalPages ? <Link className="rounded-full border border-line bg-paper px-4 py-2 hover:border-ink/40" href={href(page + 1)} rel="next">{t.common.next} →</Link>
        : <span className="px-4 py-2 text-muted/60">{t.common.next} →</span>}
    </nav>
  );
}
