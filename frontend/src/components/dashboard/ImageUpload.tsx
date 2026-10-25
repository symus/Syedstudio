"use client";

import { useRef, useState } from "react";
import { Alert } from "@/components/ui";
import { api, ApiError } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";

const TYPES = "image/jpeg,image/png,image/webp";

/** Single-image uploader that POSTs multipart "file" to `endpoint` and hands back the JSON response. */
export function ImageUpload<T>({ label, endpoint, currentUrl, onUploaded, shape = "rect" }: {
  label: string; endpoint: string; currentUrl: string | null; onUploaded: (result: T) => void; shape?: "rect" | "circle";
}) {
  const { t } = useI18n();
  const input = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function upload(file: File) {
    if (file.size > 5 * 1024 * 1024) { setError(t.productForm.imagesHint); return; }
    const body = new FormData();
    body.append("file", file);
    setBusy(true);
    setError(null);
    try {
      onUploaded(await api<T>(endpoint, { body }));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t.errors.generic);
    } finally {
      setBusy(false);
      if (input.current) input.current.value = "";
    }
  }

  return (
    <div className="space-y-2">
      <p className="text-sm font-medium">{label}</p>
      <div className="flex items-center gap-4">
        <div className={shape === "circle" ? "h-20 w-20 overflow-hidden rounded-full bg-sand" : "h-20 w-36 overflow-hidden rounded-xl bg-sand"}>
          {currentUrl && <img src={currentUrl} alt="" className="h-full w-full object-cover" />}
        </div>
        <label className="inline-flex min-h-[44px] cursor-pointer items-center rounded-full border border-line px-4 text-sm hover:border-ink/40">
          {busy ? t.common.uploading : t.common.upload}
          <input ref={input} type="file" accept={TYPES} className="sr-only" disabled={busy}
            onChange={(e) => { const f = e.target.files?.[0]; if (f) void upload(f); }} />
        </label>
      </div>
      {error && <Alert tone="error">{error}</Alert>}
    </div>
  );
}
