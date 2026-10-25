"use client";

import { useState } from "react";
import { AdminTable } from "@/components/dashboard/AdminTable";
import { LoadState } from "@/components/ui/LoadState";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import type { Page } from "@/lib/types";

type Entry = { id: string; actorEmail: string | null; action: string; targetType: string | null; targetId: string | null; details: string | null; createdAt: string };

export default function AdminAudit() {
  const { t, locale } = useI18n();
  const [page, setPage] = useState(0);
  const list = useApi<Page<Entry>>(`/api/admin/audit?page=${page}`);
  const time = new Intl.DateTimeFormat(locale === "pl" ? "pl-PL" : "en-GB", { dateStyle: "short", timeStyle: "short" });
  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.nav.audit}</h1>
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable page={page} totalPages={list.data?.totalPages} onPage={setPage}
          headers={[t.admin.when, t.admin.actor, t.admin.action, t.admin.target, t.admin.details]}
          rows={(list.data?.items ?? []).map((a) => [
            time.format(new Date(a.createdAt)), a.actorEmail ?? "—", <code key="c" className="text-xs">{a.action}</code>,
            a.targetType ? `${a.targetType} ${a.targetId?.slice(0, 8) ?? ""}` : "—", a.details ?? "—",
          ])} />
      </LoadState>
    </div>
  );
}
