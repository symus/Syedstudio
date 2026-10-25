"use client";

import Link from "next/link";
import { useState } from "react";
import { InquiryStatusBadge } from "@/components/inquiry/StatusBadge";
import { EmptyState, cx } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { formatDate, formatPrice } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import { INQUIRY_STATUSES, type InquiryStatus, type MakerInquirySummary, type Page } from "@/lib/types";

export default function InquiriesPage() {
  const { t, locale } = useI18n();
  const [status, setStatus] = useState<InquiryStatus | "">("");
  const [page, setPage] = useState(0);
  const { data, error, loading, reload } = useApi<Page<MakerInquirySummary>>(`/api/maker/inquiries?page=${page}${status ? `&status=${status}` : ""}`);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.inquiries.title}</h1>
      <div className="flex flex-wrap gap-2">
        {(["", ...INQUIRY_STATUSES] as const).map((s) => (
          <button key={s || "all"} type="button" aria-pressed={status === s} onClick={() => { setStatus(s); setPage(0); }}
            className={cx("min-h-[40px] rounded-full border px-4 text-sm", status === s ? "border-ink bg-ink text-cream" : "border-line bg-paper")}>
            {s ? t.inquiries.status[s] : t.common.all}
          </button>
        ))}
      </div>
      <LoadState loading={loading} error={error} onRetry={reload}>
        {data?.items.length ? (
          <>
            <div className="overflow-x-auto rounded-2xl border border-line bg-paper">
              <table className="w-full min-w-[640px] text-left text-sm">
                <thead className="border-b border-line text-xs uppercase tracking-wide text-muted">
                  <tr><th className="p-4">{t.inquiries.customer}</th><th className="p-4">{t.inquiries.product}</th><th className="p-4">{t.inquiries.budget}</th>
                    <th className="p-4">{t.inquiries.deadline}</th><th className="p-4">{t.inquiries.received}</th><th className="p-4">{t.common.status}</th></tr>
                </thead>
                <tbody className="divide-y divide-line">
                  {data.items.map((i) => (
                    <tr key={i.id} className={cx("hover:bg-sand/50", i.status === "NEW" && "font-medium")}>
                      <td className="p-4"><Link href={`/dashboard/inquiries/${i.id}`} className="underline-offset-2 hover:underline">{i.customerName}</Link></td>
                      <td className="p-4 text-ink-soft">{i.productName ?? t.inquiries.general}</td>
                      <td className="p-4">{formatPrice(i.budgetMinor, i.currency, locale) ?? "—"}</td>
                      <td className="p-4">{i.deadline ? formatDate(i.deadline, locale) : "—"}</td>
                      <td className="p-4 text-muted">{formatDate(i.createdAt, locale)}</td>
                      <td className="p-4"><InquiryStatusBadge status={i.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {data.totalPages > 1 && (
              <div className="flex justify-center gap-3 text-sm">
                <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">← {t.common.previous}</button>
                <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className="rounded-full border border-line px-4 py-2 disabled:opacity-40">{t.common.next} →</button>
              </div>
            )}
          </>
        ) : <EmptyState title={t.inquiries.empty} text={t.inquiries.emptyHint} />}
      </LoadState>
    </div>
  );
}
