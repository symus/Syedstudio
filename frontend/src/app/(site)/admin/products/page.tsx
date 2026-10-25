"use client";

import Link from "next/link";
import { useState } from "react";
import { AdminTable, SmallButton } from "@/components/dashboard/AdminTable";
import { Alert, Badge } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { api } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";
import { useAction } from "@/lib/useAction";
import { useApi } from "@/lib/useApi";
import type { Page, ProductStatus } from "@/lib/types";

type AdminProduct = { id: string; slug: string; name: string; makerSlug: string; makerName: string; status: ProductStatus;
  moderation: "VISIBLE" | "HIDDEN"; moderationReason: string | null; imageUrl: string | null };

export default function AdminProducts() {
  const { t } = useI18n();
  const [q, setQ] = useState("");
  const [query, setQuery] = useState("");
  const [hiddenOnly, setHiddenOnly] = useState(false);
  const [page, setPage] = useState(0);
  const list = useApi<Page<AdminProduct>>(`/api/admin/products?page=${page}${query ? `&q=${encodeURIComponent(query)}` : ""}${hiddenOnly ? "&moderation=HIDDEN" : ""}`);
  const { busy, error, run } = useAction(list.reload);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.nav.products}</h1>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); setQuery(q); setPage(0); }}>
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t.admin.search} aria-label={t.admin.search} className="h-11 flex-1 rounded-full border border-line bg-paper px-4" />
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={hiddenOnly} onChange={(e) => { setHiddenOnly(e.target.checked); setPage(0); }} className="h-5 w-5 accent-terracotta" />{t.products.hidden}</label>
      </form>
      {error && <Alert tone="error">{error}</Alert>}
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable page={page} totalPages={list.data?.totalPages} onPage={setPage}
          headers={["", t.common.name, t.admin.makers, t.common.status, t.common.actions]}
          rows={(list.data?.items ?? []).map((p) => [
            p.imageUrl ? <img key="i" src={p.imageUrl} alt="" className="h-12 w-12 rounded-lg object-cover" /> : null,
            <Link key="n" href={`/p/${p.makerSlug}/${p.slug}`} target="_blank" className="underline">{p.name}</Link>,
            p.makerName,
            <span key="s" className="flex flex-col gap-1"><Badge>{t.products.status[p.status]}</Badge>
              {p.moderation === "HIDDEN" && <Badge tone="danger">{t.products.hidden}{p.moderationReason ? `: ${p.moderationReason}` : ""}</Badge>}</span>,
            p.moderation === "VISIBLE"
              ? <SmallButton key="a" tone="danger" disabled={busy} onClick={() => {
                  const reason = window.prompt(t.admin.hideReason);
                  if (reason !== null) void run(() => api(`/api/admin/products/${p.id}/moderation`, { body: { status: "HIDDEN", reason } }));
                }}>{t.admin.hide}</SmallButton>
              : <SmallButton key="a" disabled={busy} onClick={() => run(() => api(`/api/admin/products/${p.id}/moderation`, { body: { status: "VISIBLE" } }))}>{t.admin.unhide}</SmallButton>,
          ])} />
      </LoadState>
    </div>
  );
}
