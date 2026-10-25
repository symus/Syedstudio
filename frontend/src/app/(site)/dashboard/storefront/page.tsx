"use client";

import Link from "next/link";
import { useState } from "react";
import { ImageUpload } from "@/components/dashboard/ImageUpload";
import { Alert, Badge, Button, Card, cx } from "@/components/ui";
import { Field, Input, Select, Textarea } from "@/components/ui/form";
import { LoadState } from "@/components/ui/LoadState";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";
import { isApiError, useApi } from "@/lib/useApi";
import { STOREFRONT_SECTIONS, type StorefrontConfig, type StorefrontSection, type StorefrontState } from "@/lib/types";

const DEVICES = { desktop: "100%", tablet: "768px", mobile: "390px" } as const;
type Device = keyof typeof DEVICES;

export default function StorefrontBuilderPage() {
  const { t } = useI18n();
  const state = useApi<StorefrontState>("/api/maker/storefront");
  if (isApiError(state.error, 404)) {
    return <Alert tone="info">{t.dash.setupText} <Link className="underline" href="/dashboard/profile">{t.profileForm.title}</Link></Alert>;
  }
  return (
    <LoadState loading={state.loading} error={state.error} onRetry={state.reload}>
      {state.data && <Builder initial={state.data} />}
    </LoadState>
  );
}

function Builder({ initial }: { initial: StorefrontState }) {
  const { t } = useI18n();
  const [server, setServer] = useState(initial);
  const [config, setConfig] = useState<StorefrontConfig>(initial.draft);
  const [dirty, setDirty] = useState(false);
  const [device, setDevice] = useState<Device>("desktop");
  const [previewVersion, setPreviewVersion] = useState(() => Date.now());
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const update = <K extends keyof StorefrontConfig>(key: K, value: StorefrontConfig[K]) => {
    setConfig((c) => ({ ...c, [key]: value }));
    setDirty(true);
  };

  async function call(fn: () => Promise<StorefrontState>, success?: string) {
    setBusy(true); setMsg(null); setErrors({});
    try {
      const next = await fn();
      setServer(next);
      setConfig(next.draft);
      setDirty(false);
      setPreviewVersion(Date.now());
      if (success) setMsg({ tone: "success", text: success });
    } catch (e) {
      if (e instanceof ApiError) { setErrors(e.fieldErrors); setMsg({ tone: "error", text: e.message }); }
      else setMsg({ tone: "error", text: t.errors.generic });
    } finally { setBusy(false); }
  }
  const saveDraft = () => call(() => api<StorefrontState>("/api/maker/storefront", { method: "PUT", body: config }), t.common.saved);
  const publish = () => call(async () => {
    if (dirty) await api<StorefrontState>("/api/maker/storefront", { method: "PUT", body: config });
    return api<StorefrontState>("/api/maker/storefront/publish", { method: "POST" });
  }, t.builder.upToDate);

  const toggleSection = (s: StorefrontSection) =>
    update("sections", config.sections.includes(s) ? config.sections.filter((x) => x !== s) : [...config.sections, s]);
  const moveSection = (index: number, delta: number) => {
    const next = [...config.sections];
    const target = index + delta;
    if (target < 0 || target >= next.length) return;
    [next[index], next[target]] = [next[target], next[index]];
    update("sections", next);
  };

  const statusText = server.status === "PUBLISHED"
    ? (server.hasUnpublishedChanges || dirty ? t.builder.unpublishedChanges : t.builder.upToDate)
    : server.status === "UNPUBLISHED" ? t.builder.unpublished : t.builder.neverPublished;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl">{t.builder.title}</h1>
          <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-muted">
            <Badge tone={server.status === "PUBLISHED" ? "success" : "neutral"}>{statusText}</Badge>
            {server.status === "PUBLISHED" && <>{t.builder.liveAt} <Link href={server.path} target="_blank" className="underline">{server.path}</Link></>}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" disabled={busy || !dirty} onClick={saveDraft}>{busy ? t.common.saving : t.builder.saveDraft}</Button>
          <Button variant="sage" disabled={busy || !server.emailVerified || (server.status === "PUBLISHED" && !server.hasUnpublishedChanges && !dirty)} onClick={publish}>
            {server.status === "PUBLISHED" ? t.builder.publishChanges : t.builder.publish}
          </Button>
          {server.status === "PUBLISHED" && (
            <Button variant="ghost" disabled={busy} onClick={() => call(() => api<StorefrontState>("/api/maker/storefront/unpublish", { method: "POST" }))}>{t.builder.unpublish}</Button>
          )}
        </div>
      </div>
      {!server.emailVerified && <Alert tone="warning">{t.builder.verifyFirst}</Alert>}
      {!server.makerApproved && <Alert tone="info">{t.builder.approvalNote}</Alert>}
      {msg && <Alert tone={msg.tone}>{msg.text}</Alert>}

      <div className="grid gap-6 2xl:grid-cols-[minmax(0,420px)_1fr]">
        <div className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl">{t.builder.template}</h2>
            <div className="mt-4 grid gap-3 sm:grid-cols-3 2xl:grid-cols-1">
              {server.templates.map((tpl) => (
                <button key={tpl.key} type="button" aria-pressed={config.templateKey === tpl.key}
                  onClick={() => { setConfig((c) => ({ ...c, templateKey: tpl.key, primaryColor: tpl.defaultPrimaryColor, accentColor: tpl.defaultAccentColor, font: tpl.defaultFont })); setDirty(true); }}
                  className={cx("rounded-xl border p-3 text-left", config.templateKey === tpl.key ? "border-terracotta ring-2 ring-terracotta/30" : "border-line hover:border-ink/30")}>
                  <span className="flex gap-1"><span className="h-5 w-5 rounded-full" style={{ background: tpl.defaultPrimaryColor }} /><span className="h-5 w-5 rounded-full" style={{ background: tpl.defaultAccentColor }} /></span>
                  <span className="mt-2 block font-medium">{tpl.name}</span>
                  <span className="block text-xs text-muted">{tpl.description}</span>
                </button>
              ))}
            </div>
          </Card>

          <Card className="grid gap-4 p-6 sm:grid-cols-3 2xl:grid-cols-1">
            <h2 className="text-xl sm:col-span-3 2xl:col-span-1">{t.builder.colors}</h2>
            <ColorField label={t.builder.primaryColor} value={config.primaryColor} onChange={(v) => update("primaryColor", v)} error={errors.primaryColor} />
            <ColorField label={t.builder.accentColor} value={config.accentColor} onChange={(v) => update("accentColor", v)} error={errors.accentColor} />
            <Field label={t.builder.font}>{(f) => (
              <Select value={config.font} onChange={(e) => update("font", e.target.value as StorefrontConfig["font"])} {...f}>
                {(["SERIF", "SANS", "ROUNDED"] as const).map((x) => <option key={x} value={x}>{t.builder.fonts[x]}</option>)}
              </Select>
            )}</Field>
          </Card>

          <Card className="space-y-4 p-6">
            <h2 className="text-xl">{t.builder.hero}</h2>
            <Field label={t.builder.heroTitle} error={errors.heroTitle}>{(f) => <Input maxLength={120} value={config.heroTitle ?? ""} onChange={(e) => update("heroTitle", e.target.value)} {...f} />}</Field>
            <Field label={t.builder.heroSubtitle} error={errors.heroSubtitle}>{(f) => <Textarea className="min-h-[80px]" maxLength={240} value={config.heroSubtitle ?? ""} onChange={(e) => update("heroSubtitle", e.target.value)} {...f} />}</Field>
            <ImageUpload<StorefrontState> label={t.builder.heroImage} endpoint="/api/maker/storefront/hero-image" currentUrl={server.heroImageUrl}
              onUploaded={(next) => { setServer(next); setConfig((c) => ({ ...c, heroImageKey: next.draft.heroImageKey })); setPreviewVersion(Date.now()); }} />
          </Card>

          <Card className="space-y-4 p-6">
            <h2 className="text-xl">{t.builder.about}</h2>
            <Field label={t.builder.aboutTitle}>{(f) => <Input maxLength={80} value={config.aboutTitle ?? ""} onChange={(e) => update("aboutTitle", e.target.value)} {...f} />}</Field>
            <Field label={t.builder.aboutText} error={errors.aboutText}>{(f) => <Textarea maxLength={5000} value={config.aboutText ?? ""} onChange={(e) => update("aboutText", e.target.value)} {...f} />}</Field>
            <Field label={t.builder.featuredHeading}>{(f) => <Input maxLength={80} value={config.featuredHeading ?? ""} onChange={(e) => update("featuredHeading", e.target.value)} {...f} />}</Field>
          </Card>

          <Card className="p-6">
            <h2 className="text-xl">{t.builder.sections}</h2>
            <ul className="mt-4 space-y-2">
              {config.sections.map((s, i) => (
                <li key={s} className="flex items-center justify-between gap-2 rounded-xl border border-line px-3 py-2">
                  <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked onChange={() => toggleSection(s)} className="h-5 w-5 accent-terracotta" />{t.builder.sectionNames[s]}</label>
                  <span className="flex gap-1">
                    <button type="button" aria-label={t.builder.moveUp} disabled={i === 0} onClick={() => moveSection(i, -1)} className="h-9 w-9 rounded-lg hover:bg-sand disabled:opacity-30">↑</button>
                    <button type="button" aria-label={t.builder.moveDown} disabled={i === config.sections.length - 1} onClick={() => moveSection(i, 1)} className="h-9 w-9 rounded-lg hover:bg-sand disabled:opacity-30">↓</button>
                  </span>
                </li>
              ))}
              {STOREFRONT_SECTIONS.filter((s) => !config.sections.includes(s)).map((s) => (
                <li key={s} className="rounded-xl border border-dashed border-line px-3 py-2">
                  <label className="flex items-center gap-2 text-sm text-muted"><input type="checkbox" checked={false} onChange={() => toggleSection(s)} className="h-5 w-5 accent-terracotta" />{t.builder.sectionNames[s]}</label>
                </li>
              ))}
            </ul>
            <p className="mt-3 text-xs text-muted">{t.builder.productsNote}</p>
          </Card>

          <Card className="space-y-4 p-6">
            <h2 className="text-xl">{t.builder.seo}</h2>
            <Field label={t.builder.language}>{(f) => (
              <Select value={config.language} onChange={(e) => update("language", e.target.value as StorefrontConfig["language"])} {...f}>
                <option value="pl">Polski</option><option value="en">English</option>
              </Select>
            )}</Field>
            <Field label={t.builder.seoTitle} hint={`${(config.seoTitle ?? "").length}/70`}>{(f) => <Input maxLength={70} value={config.seoTitle ?? ""} onChange={(e) => update("seoTitle", e.target.value)} {...f} />}</Field>
            <Field label={t.builder.seoDescription} hint={`${(config.seoDescription ?? "").length}/160`}>{(f) => <Textarea className="min-h-[80px]" maxLength={160} value={config.seoDescription ?? ""} onChange={(e) => update("seoDescription", e.target.value)} {...f} />}</Field>
          </Card>
        </div>

        <div className="2xl:sticky 2xl:top-20 2xl:self-start">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h2 className="text-xl">{t.builder.preview}</h2>
            <div role="group" aria-label={t.builder.preview} className="flex rounded-full border border-line bg-paper p-0.5 text-sm">
              {(Object.keys(DEVICES) as Device[]).map((d) => (
                <button key={d} type="button" aria-pressed={device === d} onClick={() => setDevice(d)}
                  className={cx("min-h-[36px] rounded-full px-3", device === d ? "bg-ink text-cream" : "")}>{t.builder.devices[d]}</button>
              ))}
            </div>
          </div>
          {dirty && <p className="mb-2 text-xs text-warning">{t.builder.unpublishedChanges} — {t.builder.saveDraft}</p>}
          <div className="overflow-hidden rounded-2xl border border-line bg-sand p-3">
            <iframe key={previewVersion} title={t.builder.preview} src={`/preview/storefront?v=${previewVersion}`}
              className="mx-auto block h-[720px] rounded-xl border border-line bg-white transition-[width]" style={{ width: DEVICES[device], maxWidth: "100%" }} />
          </div>
          <a href="/preview/storefront" target="_blank" className="mt-2 inline-block text-sm underline">{t.builder.openPreview} ↗</a>
        </div>
      </div>
    </div>
  );
}

function ColorField({ label, value, onChange, error }: { label: string; value: string; onChange: (v: string) => void; error?: string }) {
  return (
    <Field label={label} error={error}>{(f) => (
      <div className="flex items-center gap-2">
        <input type="color" value={value} onChange={(e) => onChange(e.target.value)} className="h-11 w-14 cursor-pointer rounded-lg border border-line bg-paper" aria-label={label} />
        <Input value={value} onChange={(e) => onChange(e.target.value)} pattern="#[0-9a-fA-F]{6}" maxLength={7} {...f} />
      </div>
    )}</Field>
  );
}
