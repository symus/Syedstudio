"use client";

import type { ReactNode } from "react";
import { EmptyState } from "@/components/ui";
import { useI18n } from "@/lib/i18n/client";

export function AdminTable({ headers, rows, page, totalPages, onPage }: {
  headers: string[]; rows: ReactNode[][]; page?: number; totalPages?: number; onPage?: (p: number) => void;
}) {
  const { t } = useI18n();
  if (!rows.length) return <EmptyState title={t.common.none} />;
  return (
    <div className="space-y-4">
      <div className="overflow-x-auto rounded-2xl border border-line bg-paper">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead className="border-b border-line text-xs uppercase tracking-wide text-muted">
            <tr>{headers.map((h) => <th key={h} className="p-3 font-medium">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-line">
            {rows.map((r, i) => <tr key={i} className="align-top">{r.map((c, j) => <td key={j} className="p-3">{c}</td>)}</tr>)}
          </tbody>
        </table>
      </div>
      {onPage && totalPages !== undefined && totalPages > 1 && page !== undefined && (
        <div className="flex justify-center gap-3 text-sm">
          <button type="button" disabled={page === 0} onClick={() => onPage(page - 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">← {t.common.previous}</button>
          <span className="py-2 text-muted">{page + 1} / {totalPages}</span>
          <button type="button" disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">{t.common.next} →</button>
        </div>
      )}
    </div>
  );
}

export function SmallButton({ children, onClick, tone = "default", disabled }: { children: ReactNode; onClick: () => void; tone?: "default" | "danger"; disabled?: boolean }) {
  return (
    <button type="button" disabled={disabled} onClick={onClick}
      className={`min-h-[36px] rounded-full border px-3 text-xs disabled:opacity-40 ${tone === "danger" ? "border-red-200 text-danger hover:bg-red-50" : "border-line hover:border-ink/40"}`}>
      {children}
    </button>
  );
}
