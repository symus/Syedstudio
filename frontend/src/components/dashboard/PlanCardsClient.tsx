"use client";

import { PlanCards } from "@/components/site/PlanCards";
import { useI18n } from "@/lib/i18n/client";
import type { Plan } from "@/lib/types";

export function PlanCardsClient({ plans, currentCode }: { plans: Plan[]; currentCode?: string }) {
  const { t, locale } = useI18n();
  return <PlanCards plans={plans} locale={locale} t={t} currentCode={currentCode ?? ""} />;
}
