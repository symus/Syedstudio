"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { InquiryStatusBadge } from "@/components/inquiry/StatusBadge";
import { Alert, Button, ButtonLink, Card, Container, EmptyState } from "@/components/ui";
import { Field, Input, Select } from "@/components/ui/form";
import { LoadState } from "@/components/ui/LoadState";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { formatDate, formatPrice } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import type { CustomerInquiry, Me } from "@/lib/types";

export default function AccountPage() {
  const { t, locale } = useI18n();
  const { me, loading, setMe } = useAuth();
  const router = useRouter();
  const inquiries = useApi<CustomerInquiry[]>(me ? "/api/account/inquiries" : null);
  const [msg, setMsg] = useState<{ tone: "success" | "error"; text: string } | null>(null);

  if (loading || !me) return <Container className="py-10"><LoadState loading error={null} onRetry={() => {}}>{null}</LoadState></Container>;

  async function run(action: () => Promise<void>) {
    setMsg(null);
    try { await action(); } catch (e) { setMsg({ tone: "error", text: e instanceof ApiError ? e.message : t.errors.generic }); }
  }

  const saveProfile = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    void run(async () => {
      const updated = await api<Me>("/api/me", { method: "PATCH", body: { displayName: d.get("displayName"), preferredLocale: d.get("locale") } });
      setMe(updated);
      document.cookie = `locale=${updated.preferredLocale}; path=/; max-age=31536000; samesite=lax`;
      setMsg({ tone: "success", text: t.common.saved });
      router.refresh();
    });
  };

  const changePassword = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    void run(async () => {
      await api("/api/me/password", { body: { currentPassword: d.get("current"), newPassword: d.get("next") } });
      setMe(null);
      router.push("/login");
    });
  };

  const deleteAccount = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    void run(async () => {
      await api("/api/me", { method: "DELETE", body: { password: d.get("password") } });
      setMe(null);
      router.push("/");
      router.refresh();
    });
  };

  return (
    <Container className="max-w-3xl space-y-8 py-10">
      <h1 className="text-4xl">{t.account.title}</h1>
      {msg && <Alert tone={msg.tone}>{msg.text}</Alert>}

      <Card className="p-6">
        <h2 className="text-xl">{t.account.profile}</h2>
        <form onSubmit={saveProfile} className="mt-4 grid gap-4 sm:grid-cols-2">
          <Field label={t.auth.displayName}>{(p) => <Input name="displayName" defaultValue={me.displayName} required maxLength={100} {...p} />}</Field>
          <Field label={t.account.language}>{(p) => (
            <Select name="locale" defaultValue={me.preferredLocale} {...p}><option value="pl">Polski</option><option value="en">English</option></Select>
          )}</Field>
          <p className="text-sm text-muted sm:col-span-2">{t.auth.email}: {me.email}</p>
          <div><Button type="submit">{t.common.save}</Button></div>
        </form>
      </Card>

      {!me.roles.includes("MAKER") && (
        <Card className="p-6">
          <h2 className="text-xl">{t.account.becomeMaker}</h2>
          <p className="mt-2 text-sm text-ink-soft">{t.account.becomeMakerText}</p>
          <Button variant="sage" className="mt-4" onClick={() => run(async () => {
            setMe(await api<Me>("/api/me/become-maker", { method: "POST" }));
            router.push("/dashboard/profile");
          })}>{t.account.becomeMaker}</Button>
        </Card>
      )}

      <section>
        <h2 className="mb-4 text-xl">{t.account.inquiries}</h2>
        <LoadState loading={inquiries.loading} error={inquiries.error} onRetry={inquiries.reload}>
          {inquiries.data?.length ? (
            <ul className="space-y-3">
              {inquiries.data.map((i) => (
                <li key={i.id}>
                  <Card className="p-5">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <Link href={`/makers/${i.makerSlug}`} className="font-medium underline">{i.makerName}</Link>
                      <InquiryStatusBadge status={i.status} />
                    </div>
                    <p className="mt-1 text-xs text-muted">{formatDate(i.createdAt, locale)}{i.productName ? ` · ${i.productName}` : ""}
                      {i.budgetMinor !== null ? ` · ${formatPrice(i.budgetMinor, i.currency, locale)}` : ""}</p>
                    <p className="mt-3 line-clamp-3 text-sm text-ink-soft">{i.message}</p>
                    {i.history.filter((h) => h.message).map((h) => (
                      <p key={h.at} className="mt-3 rounded-xl bg-sand/70 p-3 text-sm"><span className="text-xs text-muted">{formatDate(h.at, locale)} · {i.makerName}:</span><br />{h.message}</p>
                    ))}
                  </Card>
                </li>
              ))}
            </ul>
          ) : <EmptyState title={t.account.noInquiries} action={<ButtonLink href="/marketplace" variant="secondary">{t.nav.marketplace}</ButtonLink>} />}
        </LoadState>
      </section>

      <Card className="p-6">
        <h2 className="text-xl">{t.account.changePassword}</h2>
        <form onSubmit={changePassword} className="mt-4 grid gap-4 sm:grid-cols-2">
          <Field label={t.account.currentPassword}>{(p) => <Input name="current" type="password" autoComplete="current-password" required {...p} />}</Field>
          <Field label={t.auth.newPassword} hint={t.auth.passwordHint}>{(p) => <Input name="next" type="password" autoComplete="new-password" minLength={8} maxLength={64} required {...p} />}</Field>
          <div><Button type="submit" variant="secondary">{t.account.changePassword}</Button></div>
        </form>
      </Card>

      <Card className="p-6">
        <h2 className="text-xl">{t.account.privacy}</h2>
        <a href="/api/me/export" download className="mt-4 inline-flex min-h-[44px] items-center rounded-full border border-line px-5 text-sm hover:border-ink/40">{t.account.export}</a>
        <form onSubmit={deleteAccount} className="mt-8 space-y-3 rounded-2xl border border-red-200 bg-red-50/50 p-5">
          <h3 className="font-sans text-base font-semibold text-danger">{t.account.deleteTitle}</h3>
          <p className="text-sm text-ink-soft">{t.account.deleteText}</p>
          <Field label={t.account.deleteConfirm}>{(p) => <Input name="password" type="password" autoComplete="current-password" required {...p} />}</Field>
          <Button type="submit" variant="danger">{t.account.deleteButton}</Button>
        </form>
      </Card>
    </Container>
  );
}
