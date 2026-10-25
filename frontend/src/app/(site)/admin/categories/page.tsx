"use client";

import { useState, type FormEvent } from "react";
import { AdminTable, SmallButton } from "@/components/dashboard/AdminTable";
import { Alert, Badge, Button, Card } from "@/components/ui";
import { Field, Input } from "@/components/ui/form";
import { LoadState } from "@/components/ui/LoadState";
import { api } from "@/lib/client";
import { slugify } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useAction } from "@/lib/useAction";
import { useApi } from "@/lib/useApi";
import type { AdminCategory } from "@/lib/types";

export default function AdminCategories() {
  const { t } = useI18n();
  const list = useApi<AdminCategory[]>("/api/admin/categories");
  const { busy, error, run } = useAction(list.reload);
  const [editing, setEditing] = useState<AdminCategory | null>(null);
  const cats = list.data ?? [];

  function move(index: number, delta: number) {
    const ids = cats.map((c) => c.id);
    const target = index + delta;
    if (target < 0 || target >= ids.length) return;
    [ids[index], ids[target]] = [ids[target], ids[index]];
    void run(() => api("/api/admin/categories/order", { method: "PUT", body: { ids } }));
  }

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    const body = { slug: d.get("slug"), names: { en: d.get("en"), pl: d.get("pl") }, active: d.get("active") === "on" };
    const form = e.currentTarget;
    void run(async () => {
      if (editing) await api(`/api/admin/categories/${editing.id}`, { method: "PUT", body });
      else await api("/api/admin/categories", { body });
      setEditing(null);
      form.reset();
    });
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.nav.categories}</h1>
      {error && <Alert tone="error">{error}</Alert>}
      <Card className="p-6">
        <h2 className="text-lg">{editing ? `${t.common.edit}: ${editing.slug}` : t.admin.newCategory}</h2>
        <form key={editing?.id ?? "new"} onSubmit={submit} className="mt-4 grid gap-4 sm:grid-cols-4">
          <Field label={t.admin.namePl}>{(f) => <Input name="pl" required maxLength={100} defaultValue={editing?.names.pl} {...f} />}</Field>
          <Field label={t.admin.nameEn}>{(f) => <Input name="en" required maxLength={100} defaultValue={editing?.names.en}
            onBlur={(e) => { const slug = e.currentTarget.form?.elements.namedItem("slug") as HTMLInputElement | null; if (slug && !slug.value) slug.value = slugify(e.target.value); }} {...f} />}</Field>
          <Field label={t.admin.slug}>{(f) => <Input name="slug" required pattern="[a-z0-9]+(-[a-z0-9]+)*" maxLength={80} defaultValue={editing?.slug} {...f} />}</Field>
          <label className="flex items-end gap-2 pb-3 text-sm"><input type="checkbox" name="active" defaultChecked={editing?.active ?? true} className="h-5 w-5 accent-terracotta" />{t.admin.active}</label>
          <div className="flex gap-2 sm:col-span-4">
            <Button type="submit" disabled={busy}>{t.common.save}</Button>
            {editing && <Button type="button" variant="ghost" onClick={() => setEditing(null)}>{t.common.cancel}</Button>}
          </div>
        </form>
      </Card>
      <LoadState loading={list.loading} error={list.error} onRetry={list.reload}>
        <AdminTable headers={["#", t.admin.namePl, t.admin.nameEn, t.admin.slug, t.common.status, t.common.actions]}
          rows={cats.map((c, i) => [
            i + 1, c.names.pl ?? "—", c.names.en ?? "—", c.slug,
            <Badge key="s" tone={c.active ? "success" : "neutral"}>{c.active ? t.admin.active : t.admin.inactive}</Badge>,
            <span key="a" className="flex gap-1">
              <SmallButton disabled={busy || i === 0} onClick={() => move(i, -1)}>↑<span className="sr-only">{t.admin.moveUp}</span></SmallButton>
              <SmallButton disabled={busy || i === cats.length - 1} onClick={() => move(i, 1)}>↓<span className="sr-only">{t.admin.moveDown}</span></SmallButton>
              <SmallButton onClick={() => { setEditing(c); window.scrollTo({ top: 0, behavior: "smooth" }); }}>{t.common.edit}</SmallButton>
            </span>,
          ])} />
      </LoadState>
    </div>
  );
}
