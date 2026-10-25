"use client";

import { useState, type FormEvent } from "react";
import { AuthCard } from "@/components/site/AuthCard";
import { Alert, Button } from "@/components/ui";
import { Field, Input } from "@/components/ui/form";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";

export default function ForgotPasswordPage() {
  const { t } = useI18n();
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api("/api/auth/forgot-password", { body: { email: new FormData(e.currentTarget).get("email") } });
      setSent(true);
    } catch (err) {
      setError(err instanceof ApiError && err.status === 429 ? t.errors.rateLimited : t.errors.generic);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthCard title={t.auth.forgotTitle}>
      {sent ? <Alert tone="success">{t.auth.forgotSent}</Alert> : (
        <form onSubmit={submit} className="space-y-4">
          <p className="text-sm text-muted">{t.auth.forgotText}</p>
          <Field label={t.auth.email}>{(p) => <Input name="email" type="email" required autoComplete="email" {...p} />}</Field>
          {error && <Alert tone="error">{error}</Alert>}
          <Button type="submit" disabled={busy} className="w-full">{t.auth.sendLink}</Button>
        </form>
      )}
    </AuthCard>
  );
}
