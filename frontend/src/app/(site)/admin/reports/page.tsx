"use client";

import Link from "next/link";
import { useState } from "react";
import { AdminTable, SmallButton } from "@/components/dashboard/AdminTable";
import { Alert, cx } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { api } from "@/lib/client";
import { formatDate } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useAction } from "@/lib/useAction";
import { useApi } from "@/lib/useApi";
import type { Page } from "@/lib/types";

type Report = { id: string; targetType: string; targetLabel: string | null; targetPath: string | null; reason: string;
  details: string | null; reporterEmail: string | null; status: string; resolutionNote: string | null; createdAt: string };

export default function AdminReports() {
  const { t, locale } = useI18n();
  const [status, setStatus] = useState("OPEN");
  const [page, setPage] = useState(0);
  const list = useApi<Page<Report>>(`/api/admin/reports?status=${status}&page=${page}`);
  const { busy, error, run } = useAction(list.reload);
  const resolve = (id: string, outcome: "RESOLVED" | "DISMISSED") => {
    const note = window.prompt(t.admin.note) ?? "";
    void run(() => api(`/api/admin/reports/${id}/resolve`, { body: { status: outcome, note } }));
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.nav.reports}</h1>
      <div className="flex gap-2">
        {["OPEN", "RESOLVED", "DISMISSED"].map((s) => (
          <button key={s} type="button" aria-pressed={status === s} onClick={() => { setStatus(s); setPage(0); }}
            className={cx("min-h-[40px] rounded-full border px-4 text-sm", status === s ? "border-ink bg-ink text-cream" : "border-line bg-paper")}>{s}</button>
        ))}
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable page={page} totalPages={list.data?.totalPages} onPage={setPage}
          headers={[t.admin.target, t.admin.reason, t.admin.details, t.admin.reporter, t.admin.when, t.common.actions]}
          rows={(list.data?.items ?? []).map((r) => [
            r.targetPath ? <Link key="t" href={r.targetPath} target="_blank" className="underline">{r.targetLabel}</Link> : `${r.targetType} (deleted)`,
            t.report.reasons[r.reason] ?? r.reason,
            <span key="d" className="line-clamp-3 max-w-xs">{r.details ?? "—"}</span>,
            r.reporterEmail ?? "—",
            formatDate(r.createdAt, locale),
            r.status === "OPEN" ? (
              <span key="a" className="flex flex-wrap gap-1">
                <SmallButton disabled={busy} onClick={() => resolve(r.id, "RESOLVED")}>{t.admin.resolve}</SmallButton>
                <SmallButton disabled={busy} onClick={() => resolve(r.id, "DISMISSED")}>{t.admin.dismiss}</SmallButton>
              </span>
            ) : (r.resolutionNote ?? r.status),
          ])} />
      </LoadState>
    </div>
  );
}
