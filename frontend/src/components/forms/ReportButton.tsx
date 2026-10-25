"use client";

import { useRef, useState, type FormEvent } from "react";
import { Alert, Button } from "@/components/ui";
import { Field, Input, Select, Textarea } from "@/components/ui/form";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";

const REASONS = ["PROHIBITED_ITEM", "COUNTERFEIT", "FRAUD", "OFFENSIVE", "INTELLECTUAL_PROPERTY", "SPAM", "OTHER"];

export function ReportButton({ targetType, targetId, label }: { targetType: "PRODUCT" | "MAKER"; targetId: string; label: string }) {
  const { t } = useI18n();
  const { me } = useAuth();
  const dialog = useRef<HTMLDialogElement>(null);
  const [state, setState] = useState<"idle" | "sending" | "done">("idle");
  const [error, setError] = useState<string | null>(null);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    setState("sending");
    setError(null);
    try {
      await api("/api/public/reports", { body: {
        targetType, targetId, reason: form.get("reason"), details: form.get("details") || null,
        email: me ? null : form.get("email") || null,
      } });
      setState("done");
    } catch (err) {
      setError(err instanceof ApiError && err.status === 429 ? t.errors.rateLimited : t.errors.generic);
      setState("idle");
    }
  }

  return (
    <>
      <button type="button" className="text-sm text-muted underline hover:text-ink" onClick={() => dialog.current?.showModal()}>{label}</button>
      <dialog ref={dialog} className="m-auto w-[min(92vw,28rem)] rounded-2xl border border-line bg-paper p-0 backdrop:bg-ink/40">
        <div className="p-6">
          <h2 className="text-xl">{t.report.title}</h2>
          {state === "done" ? (
            <div className="mt-4 space-y-4"><Alert tone="success">{t.report.thanks}</Alert>
              <Button variant="secondary" onClick={() => dialog.current?.close()}>{t.common.close}</Button></div>
          ) : (
            <form onSubmit={submit} className="mt-4 space-y-4">
              <Field label={t.report.reason}>{(p) => (
                <Select name="reason" required {...p}>{REASONS.map((r) => <option key={r} value={r}>{t.report.reasons[r]}</option>)}</Select>
              )}</Field>
              <Field label={t.report.details} optional={t.common.optional}>{(p) => <Textarea name="details" maxLength={2000} {...p} />}</Field>
              {!me && <Field label={t.report.email}>{(p) => <Input name="email" type="email" maxLength={254} {...p} />}</Field>}
              {error && <Alert tone="error">{error}</Alert>}
              <div className="flex justify-end gap-2">
                <Button type="button" variant="ghost" onClick={() => dialog.current?.close()}>{t.common.cancel}</Button>
                <Button type="submit" disabled={state === "sending"}>{state === "sending" ? t.common.sending : t.report.submit}</Button>
              </div>
            </form>
          )}
        </div>
      </dialog>
    </>
  );
}
