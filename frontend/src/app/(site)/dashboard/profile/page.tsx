"use client";

import { useEffect, useState, type FormEvent } from "react";
import { ImageUpload } from "@/components/dashboard/ImageUpload";
import { Alert, Button, Card } from "@/components/ui";
import { Checkbox, Field, Input, Select, Textarea } from "@/components/ui/form";
import { LoadState } from "@/components/ui/LoadState";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { slugify } from "@/lib/format";
import { useI18n } from "@/lib/i18n/client";
import { isApiError, useApi } from "@/lib/useApi";
import type { Category, MakerProfile } from "@/lib/types";

export default function ProfilePage() {
  const { t, locale } = useI18n();
  const { refresh } = useAuth();
  const profile = useApi<MakerProfile>("/api/maker/profile");
  const categories = useApi<Category[]>(`/api/public/categories?locale=${locale}`);
  const isNew = isApiError(profile.error, 404);
  const [slug, setSlug] = useState("");
  const [checked, setChecked] = useState<{ slug: string; available: boolean } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [msg, setMsg] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- seed the controlled slug once the profile loads
    if (profile.data) setSlug(profile.data.slug);
  }, [profile.data]);

  const unchanged = !slug || slug === profile.data?.slug;
  const slugState = unchanged || checked?.slug !== slug ? "idle" : checked.available ? "ok" : "taken";

  useEffect(() => {
    if (unchanged) return;
    const id = setTimeout(() => {
      api<{ available: boolean }>(`/api/maker/slug-available?slug=${encodeURIComponent(slug)}`)
        .then((r) => setChecked({ slug, available: r.available })).catch(() => setChecked(null));
    }, 400);
    return () => clearTimeout(id);
  }, [slug, unchanged]);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    const s = (k: string) => (d.get(k) as string | null)?.trim() || null;
    setBusy(true); setMsg(null); setErrors({});
    try {
      const saved = await api<MakerProfile>("/api/maker/profile", { method: "PUT", body: {
        slug, businessName: s("businessName"), tagline: s("tagline"), bio: s("bio"), city: s("city"), region: s("region"),
        countryCode: s("countryCode") ?? "PL", showLocation: d.get("showLocation") === "on", contactEmail: s("contactEmail"),
        phone: s("phone"), websiteUrl: s("websiteUrl"), instagramUrl: s("instagramUrl"), facebookUrl: s("facebookUrl"),
        tiktokUrl: s("tiktokUrl"), pinterestUrl: s("pinterestUrl"), acceptsCustomOrders: d.get("acceptsCustomOrders") === "on",
        primaryCategoryId: s("primaryCategoryId"), legalName: s("legalName"), taxId: s("taxId"), businessAddress: s("businessAddress"),
      } });
      profile.setData(saved);
      await refresh();
      setMsg({ tone: "success", text: isNew ? t.profileForm.created : t.common.saved });
      if (isNew) void profile.reload();
    } catch (err) {
      if (err instanceof ApiError) { setErrors(err.fieldErrors); setMsg({ tone: "error", text: err.message }); }
      else setMsg({ tone: "error", text: t.errors.generic });
    } finally {
      setBusy(false);
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  }

  const p = profile.data;
  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.profileForm.title}</h1>
      {msg && <Alert tone={msg.tone}>{msg.text}</Alert>}
      <LoadState loading={profile.loading} error={isNew ? null : profile.error} onRetry={profile.reload}>
        <form onSubmit={submit} className="space-y-6" key={p?.id ?? "new"}>
          <Card className="grid gap-4 p-6 sm:grid-cols-2">
            <Field label={t.profileForm.businessName} error={errors.businessName}>
              {(f) => <Input name="businessName" required maxLength={120} defaultValue={p?.businessName}
                onChange={(e) => { if (isNew && !slug) setSlug(slugify(e.target.value)); }} onBlur={(e) => { if (isNew && !slug) setSlug(slugify(e.target.value)); }} {...f} />}
            </Field>
            <Field label={t.profileForm.slug} error={errors.slug ?? (slugState === "taken" ? t.profileForm.slugTaken : undefined)}
              hint={`${typeof window === "undefined" ? "" : window.location.host}/${slug || "…"} — ${slugState === "ok" ? t.profileForm.slugOk : t.profileForm.slugHint}`}>
              {(f) => <Input name="slug" required pattern="[a-z0-9][a-z0-9-]{1,58}[a-z0-9]" value={slug}
                onChange={(e) => setSlug(e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ""))} {...f} />}
            </Field>
            <div className="sm:col-span-2">
              <Field label={t.profileForm.tagline} optional={t.common.optional} error={errors.tagline}>{(f) => <Input name="tagline" maxLength={160} defaultValue={p?.tagline ?? ""} {...f} />}</Field>
            </div>
            <div className="sm:col-span-2">
              <Field label={t.profileForm.bio} optional={t.common.optional} error={errors.bio}>{(f) => <Textarea name="bio" maxLength={5000} defaultValue={p?.bio ?? ""} {...f} />}</Field>
            </div>
            <Field label={t.profileForm.primaryCategory} optional={t.common.optional}>{(f) => (
              <Select name="primaryCategoryId" defaultValue={p?.primaryCategoryId ?? ""} {...f}>
                <option value="">—</option>
                {(categories.data ?? []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </Select>
            )}</Field>
            <div className="flex items-end"><Checkbox name="acceptsCustomOrders" defaultChecked={p?.acceptsCustomOrders ?? true} label={t.profileForm.acceptsCustomOrders} /></div>
          </Card>

          <Card className="grid gap-4 p-6 sm:grid-cols-3">
            <Field label={t.profileForm.city} optional={t.common.optional}>{(f) => <Input name="city" maxLength={80} defaultValue={p?.city ?? ""} autoComplete="address-level2" {...f} />}</Field>
            <Field label={t.profileForm.region} optional={t.common.optional}>{(f) => <Input name="region" maxLength={80} defaultValue={p?.region ?? ""} {...f} />}</Field>
            <Field label={t.profileForm.country}>{(f) => <Input name="countryCode" maxLength={2} pattern="[A-Z]{2}" defaultValue={p?.countryCode ?? "PL"} {...f} />}</Field>
            <div className="sm:col-span-3"><Checkbox name="showLocation" defaultChecked={p?.showLocation ?? true} label={t.profileForm.showLocation} /></div>
            <Field label={t.profileForm.contactEmail} optional={t.common.optional} error={errors.contactEmail}>{(f) => <Input name="contactEmail" type="email" defaultValue={p?.contactEmail ?? ""} {...f} />}</Field>
            <Field label={t.profileForm.phone} optional={t.common.optional}>{(f) => <Input name="phone" type="tel" maxLength={40} defaultValue={p?.phone ?? ""} {...f} />}</Field>
          </Card>

          <Card className="grid gap-4 p-6 sm:grid-cols-2">
            <p className="font-medium sm:col-span-2">{t.profileForm.social}</p>
            {([["websiteUrl", t.profileForm.website], ["instagramUrl", "Instagram"], ["facebookUrl", "Facebook"], ["tiktokUrl", "TikTok"], ["pinterestUrl", "Pinterest"]] as const).map(([name, label]) => (
              <Field key={name} label={label} optional={t.common.optional} error={errors[name]}>
                {(f) => <Input name={name} type="url" placeholder="https://" defaultValue={(p?.[name] as string | null) ?? ""} {...f} />}
              </Field>
            ))}
          </Card>

          <Card className="grid gap-4 p-6 sm:grid-cols-3">
            <div className="sm:col-span-3"><p className="font-medium">{t.profileForm.sellerInfo}</p><p className="mt-1 text-xs text-muted">{t.profileForm.sellerHint}</p></div>
            <Field label={t.profileForm.legalName} optional={t.common.optional}>{(f) => <Input name="legalName" maxLength={160} defaultValue={p?.legalName ?? ""} {...f} />}</Field>
            <Field label={t.profileForm.taxId} optional={t.common.optional}>{(f) => <Input name="taxId" maxLength={40} defaultValue={p?.taxId ?? ""} {...f} />}</Field>
            <Field label={t.profileForm.businessAddress} optional={t.common.optional}>{(f) => <Input name="businessAddress" maxLength={255} defaultValue={p?.businessAddress ?? ""} {...f} />}</Field>
          </Card>

          <Button type="submit" variant="sage" disabled={busy || slugState === "taken"}>{busy ? t.common.saving : t.common.save}</Button>
        </form>

        <Card className="mt-6 grid gap-6 p-6 sm:grid-cols-2">
          {p ? (
            <>
              <ImageUpload<MakerProfile> label={t.profileForm.logo} shape="circle" endpoint="/api/maker/profile/logo" currentUrl={p.logoUrl} onUploaded={profile.setData} />
              <ImageUpload<MakerProfile> label={t.profileForm.cover} endpoint="/api/maker/profile/cover" currentUrl={p.coverUrl} onUploaded={profile.setData} />
            </>
          ) : <p className="text-sm text-muted">{t.profileForm.createFirst}</p>}
        </Card>
      </LoadState>
    </div>
  );
}
