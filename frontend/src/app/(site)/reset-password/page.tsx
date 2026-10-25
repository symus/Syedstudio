"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useState, type FormEvent } from "react";
import { AuthCard } from "@/components/site/AuthCard";
import { Alert, Button } from "@/components/ui";
import { Field, Input } from "@/components/ui/form";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";

function ResetForm() {
  const { t } = useI18n();
  const token = useSearchParams().get("token") ?? "";
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api("/api/auth/reset-password", { body: { token, password: new FormData(e.currentTarget).get("password") } });
      setDone(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.errors.generic);
    } finally {
      setBusy(false);
    }
  }

  if (done) return <div className="space-y-4"><Alert tone="success">{t.auth.resetDone}</Alert><Link href="/login" className="underline">{t.nav.login}</Link></div>;
  return (
    <form onSubmit={submit} className="space-y-4">
      <Field label={t.auth.newPassword} hint={t.auth.passwordHint}>
        {(p) => <Input name="password" type="password" autoComplete="new-password" required minLength={8} maxLength={64} {...p} />}
      </Field>
      {error && <Alert tone="error">{error}</Alert>}
      <Button type="submit" disabled={busy || !token} className="w-full">{t.common.save}</Button>
    </form>
  );
}

export default function ResetPasswordPage() {
  const { t } = useI18n();
  return <AuthCard title={t.auth.resetTitle}><Suspense><ResetForm /></Suspense></AuthCard>;
}
