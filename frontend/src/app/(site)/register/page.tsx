"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useState, type FormEvent } from "react";
import { AuthCard } from "@/components/site/AuthCard";
import { Alert, Button, cx } from "@/components/ui";
import { Field, Input } from "@/components/ui/form";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";
import type { Me } from "@/lib/types";

function RegisterForm() {
  const { t, locale } = useI18n();
  const { setMe } = useAuth();
  const router = useRouter();
  const search = useSearchParams();
  const [type, setType] = useState<"MAKER" | "CUSTOMER">(search.get("type") === "customer" ? "CUSTOMER" : "MAKER");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    setBusy(true);
    setError(null);
    setErrors({});
    try {
      const me = await api<Me>("/api/auth/register", { body: {
        email: data.get("email"), password: data.get("password"), displayName: data.get("displayName"), accountType: type, locale,
      } });
      setMe(me);
      router.push(type === "MAKER" ? "/dashboard/profile" : "/account");
      router.refresh();
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setError(err.status === 429 ? t.errors.rateLimited : err.message);
      } else setError(t.errors.generic);
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      <fieldset>
        <legend className="mb-2 text-sm font-medium">{t.auth.accountType}</legend>
        <div className="grid gap-2">
          {(["MAKER", "CUSTOMER"] as const).map((v) => (
            <label key={v} className={cx("flex cursor-pointer items-center gap-3 rounded-xl border p-3 text-sm", type === v ? "border-terracotta bg-[#fbf1ec]" : "border-line")}>
              <input type="radio" name="accountType" value={v} checked={type === v} onChange={() => setType(v)} className="accent-terracotta" />
              {v === "MAKER" ? t.auth.typeMaker : t.auth.typeCustomer}
            </label>
          ))}
        </div>
      </fieldset>
      <Field label={t.auth.displayName} error={errors.displayName}>{(p) => <Input name="displayName" autoComplete="name" required maxLength={100} {...p} />}</Field>
      <Field label={t.auth.email} error={errors.email}>{(p) => <Input name="email" type="email" autoComplete="email" required maxLength={254} {...p} />}</Field>
      <Field label={t.auth.password} hint={t.auth.passwordHint} error={errors.password}>
        {(p) => <Input name="password" type="password" autoComplete="new-password" required minLength={8} maxLength={64} {...p} />}
      </Field>
      {error && <Alert tone="error">{error}</Alert>}
      <p className="text-xs text-muted">
        {t.auth.terms} <Link href="/legal/terms" className="underline">{t.footer.terms}</Link> · <Link href="/legal/privacy" className="underline">{t.footer.privacy}</Link>
      </p>
      <Button type="submit" variant="sage" disabled={busy} className="w-full">{busy ? t.common.loading : t.auth.submitRegister}</Button>
      <p className="text-center text-sm">{t.auth.haveAccount} <Link href="/login" className="underline">{t.nav.login}</Link></p>
    </form>
  );
}

export default function RegisterPage() {
  const { t } = useI18n();
  return <AuthCard title={t.auth.registerTitle}><Suspense><RegisterForm /></Suspense></AuthCard>;
}
