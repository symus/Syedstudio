import { ButtonLink, cx } from "@/components/ui";
import { formatPrice } from "@/lib/format";
import { fmt, type Dictionary } from "@/lib/i18n";
import type { Locale, Plan } from "@/lib/types";

export function PlanCards({ plans, locale, t, currentCode }: { plans: Plan[]; locale: Locale; t: Dictionary; currentCode?: string }) {
  return (
    <div>
      <ul className="grid gap-5 md:grid-cols-3">
        {plans.map((p, i) => {
          const highlighted = i === 1;
          return (
            <li key={p.code} className={cx("flex flex-col rounded-[var(--radius-card)] border bg-paper p-6",
              highlighted ? "border-terracotta shadow-[var(--shadow-lift)]" : "border-line")}>
              <h3 className="text-2xl">{p.name}</h3>
              <p className="mt-3">
                <span className="font-serif text-4xl">{formatPrice(p.priceMinor, p.currency, locale)}</span>
                <span className="text-muted"> {t.plans.perMonth}</span>
              </p>
              <ul className="mt-5 flex-1 space-y-2 text-sm text-ink-soft">
                <li>✓ {fmt(t.plans.products, { n: p.maxPublishedProducts })}</li>
                <li>✓ {fmt(t.plans.images, { n: p.maxImagesPerProduct })}</li>
                <li>✓ {t.plans.basicInquiries}</li>
                {p.analytics && <li>✓ {t.plans.analytics}</li>}
                {p.reducedBranding && <li>✓ {t.plans.reducedBranding}</li>}
                {p.customDomain && <li>✓ {t.plans.customDomain}</li>}
              </ul>
              {currentCode === p.code ? (
                <p className="mt-6 rounded-full bg-sand py-2.5 text-center text-sm font-medium">{t.plans.current}</p>
              ) : currentCode === undefined ? (
                <ButtonLink href="/register?type=maker" variant={highlighted ? "sage" : "secondary"} className="mt-6">{t.plans.getStarted}</ButtonLink>
              ) : null}
            </li>
          );
        })}
      </ul>
      <p className="mt-4 text-xs text-muted">{t.plans.testNotice}</p>
    </div>
  );
}
