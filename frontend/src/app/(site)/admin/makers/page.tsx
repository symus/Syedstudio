"use client";

import Link from "next/link";
import { useState } from "react";
import { AdminTable, SmallButton } from "@/components/dashboard/AdminTable";
import { Alert, Badge, cx } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { api } from "@/lib/client";
import { formatDate } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useAction } from "@/lib/useAction";
import { useApi } from "@/lib/useApi";
import type { MakerStatus, Page } from "@/lib/types";

type AdminMaker = { id: string; slug: string; businessName: string; ownerEmail: string; status: MakerStatus; featured: boolean;
  planCode: string | null; publishedProducts: number; createdAt: string };

export default function AdminMakers() {
  const { t, locale } = useI18n();
  const [status, setStatus] = useState<MakerStatus | "">("PENDING");
  const [page, setPage] = useState(0);
  const list = useApi<Page<AdminMaker>>(`/api/admin/makers?page=${page}${status ? `&status=${status}` : ""}`);
  const { busy, error, run } = useAction(list.reload);
  const setMakerStatus = (id: string, s: MakerStatus) => run(() => api(`/api/admin/makers/${id}/status`, { body: { status: s } }));

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.makers}</h1>
      <div className="flex flex-wrap gap-2">
        {(["PENDING", "APPROVED", "SUSPENDED", ""] as const).map((s) => (
          <button key={s || "all"} type="button" aria-pressed={status === s} onClick={() => { setStatus(s); setPage(0); }}
            className={cx("min-h-[40px] rounded-full border px-4 text-sm", status === s ? "border-ink bg-ink text-cream" : "border-line bg-paper")}>
            {s ? t.admin.makerStatus[s] : t.common.all}
          </button>
        ))}
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable page={page} totalPages={list.data?.totalPages} onPage={setPage}
          headers={[t.common.name, t.admin.owner, t.common.status, t.admin.plan, t.dash.products, t.common.created, t.common.actions]}
          rows={(list.data?.items ?? []).map((m) => [
            <Link key="n" href={`/makers/${m.slug}`} target="_blank" className="font-medium underline">{m.businessName}</Link>,
            m.ownerEmail,
            <span key="s" className="flex flex-wrap gap-1"><Badge tone={m.status === "APPROVED" ? "success" : m.status === "SUSPENDED" ? "danger" : "warning"}>{t.admin.makerStatus[m.status]}</Badge>{m.featured && <Badge tone="accent">{t.admin.featured}</Badge>}</span>,
            <select key="p" aria-label={t.admin.plan} defaultValue={m.planCode ?? ""} disabled={busy} className="rounded-lg border border-line bg-paper px-2 py-1"
              onChange={(e) => run(() => api(`/api/admin/makers/${m.id}/plan`, { method: "PUT", body: { planCode: e.target.value } }))}>
              {["FREE", "CREATOR", "PRO"].map((p) => <option key={p}>{p}</option>)}
            </select>,
            m.publishedProducts,
            formatDate(m.createdAt, locale),
            <span key="a" className="flex flex-wrap gap-1">
              {m.status !== "APPROVED" && <SmallButton disabled={busy} onClick={() => setMakerStatus(m.id, "APPROVED")}>{m.status === "SUSPENDED" ? t.admin.reactivate : t.admin.approve}</SmallButton>}
              {m.status !== "SUSPENDED" && <SmallButton tone="danger" disabled={busy} onClick={() => setMakerStatus(m.id, "SUSPENDED")}>{t.admin.suspend}</SmallButton>}
              {m.status === "APPROVED" && <SmallButton disabled={busy} onClick={() => run(() => api(`/api/admin/makers/${m.id}/featured`, { body: { featured: !m.featured } }))}>{m.featured ? t.admin.unfeature : t.admin.feature}</SmallButton>}
            </span>,
          ])} />
      </LoadState>
    </div>
  );
}
