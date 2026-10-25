"use client";

import { useRouter } from "next/navigation";
import { LOCALE_COOKIE, LOCALES } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n/client";
import { cx } from "@/components/ui";

export function LocaleSwitcher({ className }: { className?: string }) {
  const { locale, t } = useI18n();
  const router = useRouter();
  return (
    <div role="group" aria-label={t.nav.language} className={cx("flex rounded-full border border-line p-0.5 text-xs", className)}>
      {LOCALES.map((l) => (
        <button
          key={l}
          type="button"
          aria-pressed={l === locale}
          onClick={() => {
            document.cookie = `${LOCALE_COOKIE}=${l}; path=/; max-age=31536000; samesite=lax`;
            router.refresh();
          }}
          className={cx("min-h-[32px] min-w-[36px] rounded-full px-2 uppercase", l === locale ? "bg-ink text-cream" : "text-ink-soft hover:bg-sand")}
        >
          {l}
        </button>
      ))}
    </div>
  );
}
