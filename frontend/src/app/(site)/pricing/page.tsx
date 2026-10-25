import type { Metadata } from "next";
import { PlanCards } from "@/components/site/PlanCards";
import { Container } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { getI18n } from "@/lib/i18n/server";
import type { Plan } from "@/lib/types";

export async function generateMetadata(): Promise<Metadata> {
  const { t } = await getI18n();
  return { title: t.plans.title, description: t.plans.subtitle, alternates: { canonical: "/pricing" } };
}

export default async function PricingPage() {
  const { locale, t } = await getI18n();
  const plans = (await publicApi<Plan[]>("/api/public/plans", 300)) ?? [];
  return (
    <Container className="py-14">
      <h1 className="text-4xl">{t.plans.title}</h1>
      <p className="mb-10 mt-2 text-muted">{t.plans.subtitle}</p>
      <PlanCards plans={plans} locale={locale} t={t} />
    </Container>
  );
}
