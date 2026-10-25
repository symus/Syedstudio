"use client";

import Link from "next/link";
import { use, useState, type FormEvent } from "react";
import { InquiryStatusBadge } from "@/components/inquiry/StatusBadge";
import { Alert, Button, Card } from "@/components/ui";
import { Field, Select, Textarea } from "@/components/ui/form";
import { LoadState } from "@/components/ui/LoadState";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { formatDate, formatPrice } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import { INQUIRY_STATUSES, type MakerInquiryDetail } from "@/lib/types";

export default function InquiryDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { t, locale } = useI18n();
  const { me } = useAuth();
  const { data: i, setData, error, loading, reload } = useApi<MakerInquiryDetail>(`/api/maker/inquiries/${id}`);
  const [msg, setMsg] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>, path: string, body: (d: FormData) => object, method = "POST") {
    e.preventDefault();
    const form = e.currentTarget;
    setBusy(true); setMsg(null);
    try {
      setData(await api<MakerInquiryDetail>(`/api/maker/inquiries/${id}/${path}`, { method, body: body(new FormData(form)) }));
      if (path !== "notes") form.reset();
      setMsg({ tone: "success", text: t.common.saved });
    } catch (err) {
      setMsg({ tone: "error", text: err instanceof ApiError ? err.message : t.errors.generic });
    } finally { setBusy(false); }
  }

  return (
    <LoadState loading={loading} error={error} onRetry={reload}>
      {i && (
        <div className="space-y-6">
          <p className="text-sm"><Link href="/dashboard/inquiries" className="underline">← {t.inquiries.title}</Link></p>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h1 className="text-3xl">{i.customerName}</h1>
            <InquiryStatusBadge status={i.status} />
          </div>
          {msg && <Alert tone={msg.tone}>{msg.text}</Alert>}
          <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
            <div className="space-y-6">
              <Card className="p-6">
                <dl className="grid gap-4 text-sm sm:grid-cols-2">
                  <div><dt className="text-muted">{t.auth.email}</dt><dd><a className="underline" href={`mailto:${i.customerEmail}`}>{i.customerEmail}</a></dd></div>
                  {i.customerPhone && <div><dt className="text-muted">{t.inquiryForm.phone}</dt><dd><a className="underline" href={`tel:${i.customerPhone}`}>{i.customerPhone}</a></dd></div>}
                  <div><dt className="text-muted">{t.inquiries.product}</dt><dd>{i.productName && me?.makerSlug && i.productSlug
                    ? <Link className="underline" href={`/p/${me.makerSlug}/${i.productSlug}`} target="_blank">{i.productName}</Link> : (i.productName ?? t.inquiries.general)}</dd></div>
                  <div><dt className="text-muted">{t.inquiries.budget}</dt><dd>{formatPrice(i.budgetMinor, i.currency, locale) ?? "—"}</dd></div>
                  <div><dt className="text-muted">{t.inquiries.deadline}</dt><dd>{i.deadline ? formatDate(i.deadline, locale) : "—"}</dd></div>
                  <div><dt className="text-muted">{t.inquiries.received}</dt><dd>{formatDate(i.createdAt, locale)}</dd></div>
                </dl>
                <h2 className="mt-6 text-lg">{t.inquiries.message}</h2>
                <p className="mt-2 whitespace-pre-line text-ink-soft">{i.message}</p>
                {i.attachments.length > 0 && (
                  <>
                    <h2 className="mt-6 text-lg">{t.inquiries.attachments}</h2>
                    <ul className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3">
                      {i.attachments.map((a) => {
                        const url = `/api/maker/inquiries/${i.id}/attachments/${a.id}`;
                        return (
                          <li key={a.id}><a href={url} target="_blank" rel="noopener noreferrer">
                            <img src={url} alt={a.filename} className="aspect-square w-full rounded-xl border border-line object-cover" />
                            <span className="mt-1 block truncate text-xs text-muted">{a.filename}</span>
                          </a></li>
                        );
                      })}
                    </ul>
                  </>
                )}
                <p className="mt-6 text-xs text-muted">{t.inquiries.consentAt}: {formatDate(i.consentAt, locale)}</p>
              </Card>

              <Card className="p-6">
                <h2 className="text-lg">{t.inquiries.reply}</h2>
                <p className="text-xs text-muted">{t.inquiries.replyHint}</p>
                <form className="mt-3 space-y-3" onSubmit={(e) => submit(e, "reply", (d) => ({ message: d.get("message") }))}>
                  <Field label={t.inquiries.message}>{(f) => <Textarea name="message" required maxLength={5000} {...f} />}</Field>
                  <Button type="submit" disabled={busy}>{busy ? t.common.sending : t.common.send}</Button>
                </form>
              </Card>

              <Card className="p-6">
                <h2 className="text-lg">{t.inquiries.history}</h2>
                <ol className="mt-3 space-y-3 border-l-2 border-line pl-4 text-sm">
                  {i.history.map((h) => (
                    <li key={h.at}>
                      <p><InquiryStatusBadge status={h.to} /> <span className="text-xs text-muted">{formatDate(h.at, locale)}</span></p>
                      {h.message && <p className="mt-1 whitespace-pre-line text-ink-soft">{h.message}</p>}
                    </li>
                  ))}
                </ol>
              </Card>
            </div>

            <div className="space-y-6">
              <Card className="p-6">
                <h2 className="text-lg">{t.inquiries.changeStatus}</h2>
                <form className="mt-3 space-y-3" onSubmit={(e) => submit(e, "status", (d) => ({ status: d.get("status"), message: d.get("message") || null }))}>
                  <Field label={t.common.status}>{(f) => (
                    <Select name="status" defaultValue={i.status} {...f}>{INQUIRY_STATUSES.map((s) => <option key={s} value={s}>{t.inquiries.status[s]}</option>)}</Select>
                  )}</Field>
                  <Field label={t.inquiries.statusMessage}>{(f) => <Textarea name="message" className="min-h-[80px]" maxLength={5000} {...f} />}</Field>
                  <Button type="submit" variant="secondary" disabled={busy}>{t.inquiries.update}</Button>
                </form>
              </Card>
              <Card className="p-6">
                <h2 className="text-lg">{t.inquiries.internalNotes}</h2>
                <p className="text-xs text-muted">{t.inquiries.internalHint}</p>
                <form className="mt-3 space-y-3" onSubmit={(e) => submit(e, "notes", (d) => ({ notes: d.get("notes") }), "PUT")}>
                  <Field label={t.inquiries.internalNotes}>{(f) => <Textarea name="notes" maxLength={5000} defaultValue={i.internalNotes ?? ""} {...f} />}</Field>
                  <Button type="submit" variant="secondary" disabled={busy}>{t.common.save}</Button>
                </form>
              </Card>
            </div>
          </div>
        </div>
      )}
    </LoadState>
  );
}
