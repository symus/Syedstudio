"use client";

import { Card } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { useI18n } from "@/lib/i18n/client";
import { useApi } from "@/lib/useApi";

type Stats = { users: number; makersByStatus: Record<string, number>; publishedProducts: number; openReports: number;
  inquiriesLast30Days: number; subscriptionsByPlan: Record<string, number> };

export default function AdminOverview() {
  const { t } = useI18n();
  const { data, error, loading, reload } = useApi<Stats>("/api/admin/stats");
  return (
    <div className="space-y-6">
      <h1 className="text-3xl">{t.admin.title}</h1>
      <LoadState loading={loading} error={error} onRetry={reload}>
        {data && (
          <>
            <ul className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              {[[t.admin.users, data.users], [t.admin.published, data.publishedProducts], [t.admin.openReports, data.openReports],
                [t.admin.inquiries30, data.inquiriesLast30Days]].map(([label, value]) => (
                <li key={label as string}><Card className="p-5"><p className="text-sm text-muted">{label}</p><p className="mt-2 font-serif text-3xl">{value}</p></Card></li>
              ))}
            </ul>
            <div className="grid gap-4 md:grid-cols-2">
              <Card className="p-5"><h2 className="text-lg">{t.admin.makers}</h2>
                <ul className="mt-3 space-y-1 text-sm">{Object.entries(data.makersByStatus).map(([k, v]) => <li key={k} className="flex justify-between"><span>{t.admin.makerStatus[k] ?? k}</span><span>{v}</span></li>)}</ul></Card>
              <Card className="p-5"><h2 className="text-lg">{t.admin.byPlan}</h2>
                <ul className="mt-3 space-y-1 text-sm">{Object.entries(data.subscriptionsByPlan).map(([k, v]) => <li key={k} className="flex justify-between"><span>{k}</span><span>{v}</span></li>)}</ul></Card>
            </div>
          </>
        )}
      </LoadState>
    </div>
  );
}
