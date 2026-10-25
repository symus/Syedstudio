"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRef, useState, type FormEvent } from "react";
import { Alert, Badge, Button, Card } from "@/components/ui";
import { Checkbox, Field, Input, Select, Textarea } from "@/components/ui/form";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/client";
import { minorToInput, parsePriceToMinor } from "@/lib/format";
import { fmt } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import { OCCASIONS, type Category, type MakerProduct, type Occasion, type ProductStatus } from "@/lib/types";

type OptionDraft = { key: number; name: string; type: "TEXT" | "SELECT"; choices: string; required: boolean };

export function ProductEditor({ initial }: { initial: MakerProduct | null }) {
  const { t, locale } = useI18n();
  const { me } = useAuth();
  const router = useRouter();
  const categories = useApi<Category[]>(`/api/public/categories?locale=${locale}`);
  const [product, setProduct] = useState<MakerProduct | null>(initial);
  const [options, setOptions] = useState<OptionDraft[]>(
    (initial?.options ?? []).map((o, i) => ({ key: i, name: o.name, type: o.type, choices: o.choices.join(", "), required: o.required })));
  const nextKey = useRef(options.length);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [msg, setMsg] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  const fail = (err: unknown) => {
    if (err instanceof ApiError) { setErrors(err.fieldErrors); setMsg({ tone: "error", text: err.message }); }
    else setMsg({ tone: "error", text: t.errors.generic });
  };

  async function save(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const d = new FormData(e.currentTarget);
    const price = parsePriceToMinor(String(d.get("price") ?? ""));
    if (Number.isNaN(price)) { setErrors({ priceMinor: t.productForm.invalidPrice }); return; }
    const days = String(d.get("productionDays") ?? "").trim();
    const body = {
      name: d.get("name"), description: d.get("description") || null, categoryId: d.get("categoryId") || null,
      priceMinor: price, currency: d.get("currency"),
      tags: String(d.get("tags") ?? "").split(",").map((s) => s.trim()).filter(Boolean),
      occasions: d.getAll("occasions") as Occasion[], availabilityType: d.get("availabilityType"),
      productionDays: days ? Number.parseInt(days, 10) : null, available: d.get("available") === "on",
      customizable: d.get("customizable") === "on", materials: d.get("materials") || null, dimensions: d.get("dimensions") || null,
      shippingInfo: d.get("shippingInfo") || null, showOnStorefront: d.get("showOnStorefront") === "on",
      options: options.filter((o) => o.name.trim()).map((o) => ({
        name: o.name.trim(), type: o.type, required: o.required,
        choices: o.type === "SELECT" ? o.choices.split(",").map((c) => c.trim()).filter(Boolean) : [],
      })),
    };
    setBusy(true); setMsg(null); setErrors({});
    try {
      if (product) {
        setProduct(await api<MakerProduct>(`/api/maker/products/${product.id}`, { method: "PUT", body }));
        setMsg({ tone: "success", text: t.common.saved });
      } else {
        const created = await api<MakerProduct>("/api/maker/products", { body });
        router.replace(`/dashboard/products/${created.id}?created=1`);
      }
    } catch (err) { fail(err); } finally { setBusy(false); }
  }

  async function act(fn: () => Promise<MakerProduct | void>) {
    setBusy(true); setMsg(null);
    try { const r = await fn(); if (r) setProduct(r); } catch (err) { fail(err); } finally { setBusy(false); }
  }
  const setStatus = (status: ProductStatus) => act(() => api<MakerProduct>(`/api/maker/products/${product!.id}/status`, { body: { status } }));

  async function upload(files: FileList) {
    for (const file of Array.from(files)) {
      const body = new FormData();
      body.append("file", file);
      await act(() => api<MakerProduct>(`/api/maker/products/${product!.id}/images`, { body }));
    }
  }

  function move(index: number, delta: number) {
    if (!product) return;
    const ids = product.images.map((i) => i.id);
    const target = index + delta;
    if (target < 0 || target >= ids.length) return;
    [ids[index], ids[target]] = [ids[target], ids[index]];
    void act(() => api<MakerProduct>(`/api/maker/products/${product.id}/images/order`, { method: "PUT", body: { imageIds: ids } }));
  }

  const p = product;
  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm"><Link href="/dashboard/products" className="underline">← {t.products.title}</Link></p>
          <h1 className="mt-1 text-3xl">{p ? p.name : t.productForm.newTitle}</h1>
        </div>
        {p && (
          <div className="flex flex-wrap items-center gap-2">
            <Badge tone={p.status === "PUBLISHED" ? "success" : "neutral"}>{t.products.status[p.status]}</Badge>
            {p.status !== "PUBLISHED" && <Button variant="sage" disabled={busy} onClick={() => setStatus("PUBLISHED")}>{t.productForm.publish}</Button>}
            {p.status === "PUBLISHED" && <Button variant="secondary" disabled={busy} onClick={() => setStatus("DRAFT")}>{t.productForm.unpublish}</Button>}
            {p.status !== "ARCHIVED" && <Button variant="ghost" disabled={busy} onClick={() => setStatus("ARCHIVED")}>{t.productForm.archive}</Button>}
            {p.status === "PUBLISHED" && me?.makerSlug && <Link className="text-sm underline" href={`/p/${me.makerSlug}/${p.slug}`} target="_blank">{t.productForm.preview} ↗</Link>}
          </div>
        )}
      </div>
      {p?.moderationStatus === "HIDDEN" && <Alert tone="error">{fmt(t.productForm.hidden, { reason: p.moderationReason ?? "—" })}</Alert>}
      {msg && <Alert tone={msg.tone}>{msg.text}</Alert>}

      <Card className="p-6">
        <h2 className="text-xl">{t.productForm.images}</h2>
        <p className="mt-1 text-xs text-muted">{t.productForm.imagesHint}</p>
        {p ? (
          <>
            <ul className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
              {p.images.map((img, i) => (
                <li key={img.id} className="overflow-hidden rounded-xl border border-line">
                  <img src={img.url} alt={img.altText} className="aspect-square w-full object-cover" />
                  <div className="flex justify-between p-1 text-sm">
                    <button type="button" className="h-9 w-9 rounded-lg hover:bg-sand disabled:opacity-30" aria-label={t.productForm.moveLeft} disabled={i === 0 || busy} onClick={() => move(i, -1)}>←</button>
                    <button type="button" className="h-9 rounded-lg px-2 text-danger hover:bg-red-50" aria-label={t.productForm.removeImage} disabled={busy}
                      onClick={() => act(() => api<MakerProduct>(`/api/maker/products/${p.id}/images/${img.id}`, { method: "DELETE" }))}>✕</button>
                    <button type="button" className="h-9 w-9 rounded-lg hover:bg-sand disabled:opacity-30" aria-label={t.productForm.moveRight} disabled={i === p.images.length - 1 || busy} onClick={() => move(i, 1)}>→</button>
                  </div>
                </li>
              ))}
            </ul>
            <label className="mt-4 inline-flex min-h-[44px] cursor-pointer items-center rounded-full border border-line px-5 text-sm hover:border-ink/40">
              {busy ? t.common.uploading : `+ ${t.common.upload}`}
              <input type="file" accept="image/jpeg,image/png,image/webp" multiple className="sr-only" disabled={busy}
                onChange={(e) => { if (e.target.files?.length) void upload(e.target.files); e.target.value = ""; }} />
            </label>
          </>
        ) : <p className="mt-3 text-sm text-muted">{t.productForm.saveFirst}</p>}
      </Card>

      <form onSubmit={save} className="space-y-6" key={p?.id ?? "new"}>
        <Card className="grid gap-4 p-6 sm:grid-cols-2">
          <div className="sm:col-span-2"><Field label={t.productForm.name} error={errors.name}>{(f) => <Input name="name" required maxLength={140} defaultValue={p?.name} {...f} />}</Field></div>
          <div className="sm:col-span-2"><Field label={t.productForm.description} optional={t.common.optional} error={errors.description}>{(f) => <Textarea name="description" maxLength={10000} defaultValue={p?.description ?? ""} {...f} />}</Field></div>
          <Field label={t.productForm.category} error={errors.categoryId}>{(f) => (
            <Select name="categoryId" defaultValue={p?.categoryId ?? ""} {...f}>
              <option value="">{t.productForm.chooseCategory}</option>
              {(categories.data ?? []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </Select>
          )}</Field>
          <div className="grid grid-cols-[1fr_100px] gap-2">
            <Field label={t.productForm.price} hint={t.productForm.priceHint} error={errors.priceMinor}>{(f) => <Input name="price" inputMode="decimal" defaultValue={minorToInput(p?.priceMinor)} {...f} />}</Field>
            <Field label={t.productForm.currency}>{(f) => <Select name="currency" defaultValue={p?.currency ?? "PLN"} {...f}><option>PLN</option><option>EUR</option></Select>}</Field>
          </div>
          <Field label={t.productForm.availabilityType}>{(f) => (
            <Select name="availabilityType" defaultValue={p?.availabilityType ?? "READY_MADE"} {...f}>
              <option value="READY_MADE">{t.productForm.readyMade}</option><option value="MADE_TO_ORDER">{t.productForm.madeToOrder}</option>
            </Select>
          )}</Field>
          <Field label={t.productForm.productionDays} optional={t.common.optional} error={errors.productionDays}>{(f) => <Input name="productionDays" type="number" min={0} max={365} defaultValue={p?.productionDays ?? ""} {...f} />}</Field>
          <Field label={t.productForm.materials} optional={t.common.optional}>{(f) => <Input name="materials" maxLength={255} defaultValue={p?.materials ?? ""} {...f} />}</Field>
          <Field label={t.productForm.dimensions} optional={t.common.optional}>{(f) => <Input name="dimensions" maxLength={255} defaultValue={p?.dimensions ?? ""} {...f} />}</Field>
          <div className="sm:col-span-2"><Field label={t.productForm.shippingInfo} optional={t.common.optional}>{(f) => <Textarea name="shippingInfo" className="min-h-[80px]" maxLength={2000} defaultValue={p?.shippingInfo ?? ""} {...f} />}</Field></div>
          <div className="sm:col-span-2"><Field label={t.productForm.tags} hint={t.productForm.tagsHint} optional={t.common.optional}>{(f) => <Input name="tags" defaultValue={p?.tags.join(", ")} {...f} />}</Field></div>
          <fieldset className="sm:col-span-2">
            <legend className="mb-2 text-sm font-medium">{t.productForm.occasions}</legend>
            <div className="flex flex-wrap gap-2">
              {OCCASIONS.map((o) => (
                <label key={o} className="flex min-h-[40px] cursor-pointer items-center gap-2 rounded-full border border-line px-3 text-sm has-[:checked]:border-terracotta has-[:checked]:bg-[#fbf1ec]">
                  <input type="checkbox" name="occasions" value={o} defaultChecked={p?.occasions.includes(o)} className="accent-terracotta" />{t.occasions[o]}
                </label>
              ))}
            </div>
          </fieldset>
          <div className="space-y-3 sm:col-span-2">
            <Checkbox name="available" defaultChecked={p?.available ?? true} label={t.productForm.available} />
            <Checkbox name="customizable" defaultChecked={p?.customizable ?? false} label={t.productForm.customizable} />
            <Checkbox name="showOnStorefront" defaultChecked={p?.showOnStorefront ?? true} label={t.productForm.showOnStorefront} />
          </div>
        </Card>

        <Card className="space-y-4 p-6">
          <h2 className="text-xl">{t.productForm.options}</h2>
          {options.map((o, i) => (
            <div key={o.key} className="grid gap-3 rounded-xl border border-line p-4 sm:grid-cols-[1fr_140px_1.4fr_auto]">
              <Field label={t.productForm.optionName}>{(f) => <Input value={o.name} maxLength={80} onChange={(e) => setOptions(options.map((x, j) => j === i ? { ...x, name: e.target.value } : x))} {...f} />}</Field>
              <Field label={t.productForm.optionType}>{(f) => (
                <Select value={o.type} onChange={(e) => setOptions(options.map((x, j) => j === i ? { ...x, type: e.target.value as OptionDraft["type"] } : x))} {...f}>
                  <option value="TEXT">{t.productForm.optionText}</option><option value="SELECT">{t.productForm.optionSelect}</option>
                </Select>
              )}</Field>
              <Field label={t.productForm.optionChoices}>{(f) => <Input value={o.choices} disabled={o.type !== "SELECT"} onChange={(e) => setOptions(options.map((x, j) => j === i ? { ...x, choices: e.target.value } : x))} {...f} />}</Field>
              <div className="flex items-end gap-3 pb-1">
                <Checkbox checked={o.required} onChange={(e) => setOptions(options.map((x, j) => j === i ? { ...x, required: e.target.checked } : x))} label={t.productForm.optionRequired} />
                <button type="button" className="h-10 rounded-lg px-2 text-danger hover:bg-red-50" aria-label={t.common.delete} onClick={() => setOptions(options.filter((_, j) => j !== i))}>✕</button>
              </div>
            </div>
          ))}
          {options.length < 10 && (
            <Button type="button" variant="secondary" onClick={() => setOptions([...options, { key: nextKey.current++, name: "", type: "TEXT", choices: "", required: false }])}>
              + {t.productForm.addOption}
            </Button>
          )}
        </Card>

        <div className="flex flex-wrap justify-between gap-3">
          <Button type="submit" variant="sage" disabled={busy}>{busy ? t.common.saving : t.common.save}</Button>
          {p && (
            <Button type="button" variant="danger" disabled={busy} onClick={() => {
              if (!window.confirm(t.productForm.deleteConfirm)) return;
              void act(async () => { await api(`/api/maker/products/${p.id}`, { method: "DELETE" }); router.push("/dashboard/products"); });
            }}>{t.common.delete}</Button>
          )}
        </div>
      </form>
    </div>
  );
}
