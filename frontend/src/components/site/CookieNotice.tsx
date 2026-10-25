"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useI18n } from "@/lib/i18n/client";

const KEY = "cookie-notice-dismissed";

/** Informational only: the platform sets strictly necessary cookies, which do not require consent under ePrivacy. */
export function CookieNotice() {
  const { t } = useI18n();
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    try {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- reading browser-only storage after mount
      setVisible(localStorage.getItem(KEY) !== "1");
    } catch {
      setVisible(false);
    }
  }, []);

  if (!visible) return null;
  return (
    <div className="fixed inset-x-3 bottom-3 z-50 mx-auto flex max-w-xl flex-col gap-3 rounded-2xl border border-line bg-paper p-4 text-sm shadow-[var(--shadow-lift)] sm:flex-row sm:items-center">
      <p className="flex-1 text-ink-soft">
        {t.legal.cookieNotice}{" "}
        <Link href="/legal/privacy" className="underline">{t.footer.privacy}</Link>
      </p>
      <button
        type="button"
        className="min-h-[40px] rounded-full bg-ink px-4 text-cream"
        onClick={() => {
          try { localStorage.setItem(KEY, "1"); } catch { /* storage unavailable */ }
          setVisible(false);
        }}
      >
        {t.legal.ok}
      </button>
    </div>
  );
}
