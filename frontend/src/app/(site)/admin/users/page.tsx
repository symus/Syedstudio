"use client";

import { useState } from "react";
import { AdminTable, SmallButton } from "@/components/dashboard/AdminTable";
import { Alert, Badge } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { useAuth } from "@/lib/auth";
import { api } from "@/lib/client";
import { formatDate } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useAction } from "@/lib/useAction";
import { useApi } from "@/lib/useApi";
import type { Page, Role } from "@/lib/types";

type AdminUser = { id: string; email: string; displayName: string; roles: Role[]; status: "ACTIVE" | "SUSPENDED"; emailVerified: boolean; createdAt: string };

export default function AdminUsers() {
  const { t, locale } = useI18n();
  const { me } = useAuth();
  const [q, setQ] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const list = useApi<Page<AdminUser>>(`/api/admin/users?page=${page}${query ? `&q=${encodeURIComponent(query)}` : ""}`);
  const { busy, error, run } = useAction(list.reload);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.users}</h1>
      <form onSubmit={(e) => { e.preventDefault(); setQuery(q); setPage(0); }}>
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t.admin.search} aria-label={t.admin.search} className="h-11 w-full rounded-full border border-line bg-paper px-4" />
      </form>
      {error && <Alert tone="error">{error}</Alert>}
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable page={page} totalPages={list.data?.totalPages} onPage={setPage}
          headers={[t.common.name, t.auth.email, "Roles", t.common.status, t.common.created, t.common.actions]}
          rows={(list.data?.items ?? []).map((u) => [
            u.displayName, <span key="e">{u.email}{!u.emailVerified && <span className="ml-1 text-xs text-warning">✱</span>}</span>, u.roles.join(", "),
            <Badge key="s" tone={u.status === "ACTIVE" ? "success" : "danger"}>{u.status}</Badge>, formatDate(u.createdAt, locale),
            u.id === me?.id ? "—" : u.status === "ACTIVE"
              ? <SmallButton key="a" tone="danger" disabled={busy} onClick={() => run(() => api(`/api/admin/users/${u.id}/status`, { body: { status: "SUSPENDED" } }))}>{t.admin.suspend}</SmallButton>
              : <SmallButton key="a" disabled={busy} onClick={() => run(() => api(`/api/admin/users/${u.id}/status`, { body: { status: "ACTIVE" } }))}>{t.admin.reactivate}</SmallButton>,
          ])} />
      </LoadState>
    </div>
  );
}
