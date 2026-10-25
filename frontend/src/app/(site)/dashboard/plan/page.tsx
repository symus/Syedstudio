"use client";

import { PlanCardsClient } from "@/components/dashboard/PlanCardsClient";
import { Alert, Card } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { brand } from "@/lib/config";
import { fmt } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";
import type { Plan } from "@/lib/types";

type MySubscription = { plan: Plan; status: string; publishedProducts: number };

export default function PlanPage() {
  const { t } = useI18n();
  const sub = useApi<MySubscription>("/api/maker/subscription");
  const plans = useApi<Plan[]>("/api/maker/plans");
  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.dash.nav.plan}</h1>
      <LoadState loading={sub.loading || plans.loading} error={sub.error ?? plans.error} onRetry={() => { sub.reload(); plans.reload(); }}>
        {sub.data && (
          <Card className="p-6">
            <p className="text-sm text-muted">{t.plans.current}</p>
            <p className="font-serif text-3xl">{sub.data.plan.name}</p>
            <div className="mt-4 h-2 overflow-hidden rounded-full bg-sand" role="progressbar" aria-valuemin={0}
              aria-valuemax={sub.data.plan.maxPublishedProducts} aria-valuenow={sub.data.publishedProducts}>
              <div className="h-full bg-terracotta" style={{ width: `${Math.min(100, (sub.data.publishedProducts / sub.data.plan.maxPublishedProducts) * 100)}%` }} />
            </div>
            <p className="mt-2 text-sm text-ink-soft">{fmt(t.dash.usage, { used: sub.data.publishedProducts, max: sub.data.plan.maxPublishedProducts })}</p>
          </Card>
        )}
        {plans.data && <PlanCardsClient plans={plans.data} currentCode={sub.data?.plan.code} />}
        <Alert tone="info">{t.plans.upgradeSoon} <a className="underline" href={`mailto:${brand.supportEmail}`}>{t.plans.contact}</a></Alert>
      </LoadState>
    </div>
  );
}
