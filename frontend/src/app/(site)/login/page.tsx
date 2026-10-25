"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useState, type FormEvent } from "react";
import { AuthCard } from "@/components/site/AuthCard";
import { Alert, Button } from "@/components/ui";
import { Field, Input } from "@/components/ui/form";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";
import { safeNext } from "@/lib/redirect";
import type { Me } from "@/lib/types";

function LoginForm() {
  const { t } = useI18n();
  const { setMe } = useAuth();
  const router = useRouter();
  const search = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    setBusy(true);
    setError(null);
    try {
      const me = await api<Me>("/api/auth/login", { body: { email: data.get("email"), password: data.get("password") } });
      setMe(me);
      const fallback = me.roles.includes("MAKER") ? "/dashboard" : me.roles.includes("ADMIN") ? "/admin" : "/account";
      router.push(safeNext(search.get("next"), fallback));
      router.refresh();
    } catch (err) {
      setError(err instanceof ApiError && err.status === 429 ? t.errors.rateLimited : err instanceof Error ? err.message : t.errors.generic);
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      <Field label={t.auth.email}>{(p) => <Input name="email" type="email" autoComplete="email" required {...p} />}</Field>
      <Field label={t.auth.password}>{(p) => <Input name="password" type="password" autoComplete="current-password" required {...p} />}</Field>
      {error && <Alert tone="error">{error}</Alert>}
      <Button type="submit" disabled={busy} className="w-full">{busy ? t.common.loading : t.auth.submitLogin}</Button>
      <div className="flex justify-between text-sm">
        <Link href="/forgot-password" className="underline">{t.auth.forgot}</Link>
        <span>{t.auth.noAccount} <Link href="/register" className="underline">{t.nav.register}</Link></span>
      </div>
    </form>
  );
}

export default function LoginPage() {
  const { t } = useI18n();
  return <AuthCard title={t.auth.loginTitle}><Suspense><LoginForm /></Suspense></AuthCard>;
}
