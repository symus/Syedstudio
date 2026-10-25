"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import { Alert, Button } from "@/components/ui";
import { Checkbox, Field, Input, Textarea } from "@/components/ui/form";
import { useAuth } from "@/lib/auth";
import { api, ApiError, jsonBlob } from "@/lib/client";
import { brand } from "@/lib/config";
import { parsePriceToMinor } from "@/lib/format";
import { fmt } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n/client";

const MAX_FILES = 3;
const MAX_BYTES = 5 * 1024 * 1024;
const TYPES = ["image/jpeg", "image/png", "image/webp"];

export function InquiryForm({ makerSlug, makerName, acceptsCustomOrders, productId, productName }: {
  makerSlug: string; makerName: string; acceptsCustomOrders: boolean; productId?: string | null; productName?: string | null;
}) {
  const { t, locale } = useI18n();
  const { me } = useAuth();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);
  const [done, setDone] = useState(false);
  const [tomorrow] = useState(() => new Date(Date.now() + 86_400_000).toISOString().slice(0, 10));

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.currentTarget;
    const data = new FormData(form);
    const files = (data.getAll("files") as File[]).filter((f) => f.size > 0);
    const nextErrors: Record<string, string> = {};
    const budget = parsePriceToMinor(String(data.get("budget") ?? ""));
    if (Number.isNaN(budget)) nextErrors.budgetMinor = t.productForm.invalidPrice;
    if (files.length > MAX_FILES || files.some((f) => f.size > MAX_BYTES || !TYPES.includes(f.type))) nextErrors.files = t.inquiryForm.imagesHint;
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;

    const payload = new FormData();
    payload.append("data", jsonBlob({
      makerSlug, productId: productId ?? null, name: data.get("name"), email: data.get("email"),
      phone: data.get("phone") || null, message: data.get("message"), budgetMinor: budget, currency: "PLN",
      deadline: data.get("deadline") || null, consent: data.get("consent") === "on",
    }));
    files.forEach((f) => payload.append("files", f));
    setSending(true);
    setFormError(null);
    try {
      await api(`/api/public/inquiries?locale=${locale}`, { body: payload });
      setDone(true);
      form.reset();
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setFormError(err.status === 429 ? t.errors.rateLimited : err.message);
      } else setFormError(t.errors.generic);
    } finally {
      setSending(false);
    }
  }

  if (done) {
    return (
      <div className="rounded-2xl border border-line bg-paper p-6 text-center" role="status">
        <p className="font-serif text-2xl">{t.inquiryForm.successTitle}</p>
        <p className="mt-2 text-ink-soft">{fmt(t.inquiryForm.success, { maker: makerName })}</p>
        <Button variant="secondary" className="mt-5" onClick={() => setDone(false)}>{t.inquiryForm.another}</Button>
      </div>
    );
  }

  return (
    <form onSubmit={submit} noValidate={false} className="space-y-4">
      {!acceptsCustomOrders && <Alert tone="info">{t.storefront.notAccepting}</Alert>}
      {productName && <p className="text-sm text-muted">{t.inquiryForm.about}: <strong className="text-ink">{productName}</strong></p>}
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label={t.inquiryForm.name} error={errors.name}>{(p) => <Input name="name" required maxLength={100} defaultValue={me?.displayName} autoComplete="name" {...p} />}</Field>
        <Field label={t.inquiryForm.email} error={errors.email}>{(p) => <Input name="email" type="email" required maxLength={254} defaultValue={me?.email} autoComplete="email" {...p} />}</Field>
      </div>
      <Field label={t.inquiryForm.message} hint={t.inquiryForm.messageHint} error={errors.message}>
        {(p) => <Textarea name="message" required minLength={10} maxLength={5000} {...p} />}
      </Field>
      <div className="grid gap-4 sm:grid-cols-3">
        <Field label={t.inquiryForm.phone} optional={t.common.optional} error={errors.phone}>{(p) => <Input name="phone" type="tel" maxLength={40} autoComplete="tel" {...p} />}</Field>
        <Field label={t.inquiryForm.budget} optional={t.common.optional} error={errors.budgetMinor}>{(p) => <Input name="budget" inputMode="decimal" {...p} />}</Field>
        <Field label={t.inquiryForm.deadline} optional={t.common.optional} error={errors.deadline}>{(p) => <Input name="deadline" type="date" min={tomorrow} {...p} />}</Field>
      </div>
      <Field label={t.inquiryForm.images} optional={t.common.optional} hint={t.inquiryForm.imagesHint} error={errors.files}>
        {(p) => <Input name="files" type="file" accept={TYPES.join(",")} multiple className="file:mr-3 file:rounded-full file:border-0 file:bg-sand file:px-3 file:py-1.5" {...p} />}
      </Field>
      <Checkbox name="consent" required label={
        <>{fmt(t.inquiryForm.consent, { maker: makerName, brand: brand.name })} <Link href="/legal/privacy" className="underline" target="_blank">{t.footer.privacy}</Link></>
      } />
      {errors.consent && <p className="text-xs text-danger">{errors.consent}</p>}
      {formError && <Alert tone="error">{formError}</Alert>}
      <Button type="submit" variant="sage" disabled={sending} className="w-full sm:w-auto">{sending ? t.common.sending : t.inquiryForm.submit}</Button>
    </form>
  );
}
