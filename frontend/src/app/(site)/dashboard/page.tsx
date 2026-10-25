"use client";

import Link from "next/link";
import { Alert, Badge, ButtonLink, Card, EmptyState } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { useAuth } from "@/lib/auth";
import { formatDate } from "@/lib/format";
import { fmt } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n/client";
import { isApiError, useApi } from "@/lib/useApi";
import type { Overview } from "@/lib/types";

export default function DashboardOverview() {
  const { t, locale } = useI18n();
  const { me } = useAuth();
  const { data, error, loading, reload } = useApi<Overview>("/api/maker/overview");

  if (isApiError(error, 404)) {
    return <EmptyState title={t.dash.setupTitle} text={t.dash.setupText} action={<ButtonLink href="/dashboard/profile" variant="sage">{t.profileForm.title}</ButtonLink>} />;
  }
  const storefrontLabel = data && (data.storefrontStatus === "PUBLISHED"
    ? (data.storefrontHasUnpublishedChanges ? t.builder.unpublishedChanges : t.builder.upToDate)
    : data.storefrontStatus === "UNPUBLISHED" ? t.builder.unpublished : t.builder.neverPublished);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl">{fmt(t.dash.welcome, { name: me?.displayName ?? "" })}</h1>
        <div className="flex flex-wrap gap-2">
          <ButtonLink href="/dashboard/products/new" variant="sage">{t.dash.quick.addProduct}</ButtonLink>
          <ButtonLink href="/dashboard/storefront" variant="secondary">{t.dash.quick.editSite}</ButtonLink>
          {me?.makerSlug && data?.storefrontStatus === "PUBLISHED" && <ButtonLink href={`/${me.makerSlug}`} variant="ghost">{t.dash.quick.viewSite} ↗</ButtonLink>}
        </div>
      </div>
      <LoadState loading={loading} error={error} onRetry={reload}>
        {data && (
          <>
            {data.makerStatus === "PENDING" && <Alert tone="info">{t.dash.pending}</Alert>}
            {data.makerStatus === "SUSPENDED" && <Alert tone="error">{t.dash.suspended}</Alert>}
            <ul className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <Stat label={t.dash.storefront} value={<Badge tone={data.storefrontStatus === "PUBLISHED" ? "success" : "neutral"}>{storefrontLabel}</Badge>} href="/dashboard/storefront" />
              <Stat label={t.dash.products} value={`${data.publishedProducts} / ${data.totalProducts}`} href="/dashboard/products" />
              <Stat label={t.dash.newInquiries} value={String(data.newInquiries)} sub={`${t.dash.openInquiries}: ${data.openInquiries}`} href="/dashboard/inquiries" />
              <Stat label={t.dash.plan} value={data.planName} sub={fmt(t.dash.usage, { used: data.publishedProducts, max: data.maxPublishedProducts })} href="/dashboard/plan" />
            </ul>
            <Card className="p-6">
              <h2 className="text-xl">{t.dash.recent}</h2>
              {data.recentActivity.length ? (
                <ul className="mt-4 divide-y divide-line">
                  {data.recentActivity.map((a) => (
                    <li key={`${a.type}-${a.refId}`} className="flex items-center justify-between gap-3 py-3 text-sm">
                      <Link href={a.type === "INQUIRY" ? `/dashboard/inquiries/${a.refId}` : `/dashboard/products/${a.refId}`} className="hover:underline">
                        {fmt(t.dash.activity[a.type] ?? "{label}", { label: a.label })}
                      </Link>
                      <span className="shrink-0 text-xs text-muted">{formatDate(a.at, locale)}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="mt-3 text-sm text-muted">{t.dash.noActivity}</p>}
            </Card>
            <p className="text-xs text-muted">{t.dash.analyticsLater}</p>
          </>
        )}
      </LoadState>
    </div>
  );
}

function Stat({ label, value, sub, href }: { label: string; value: React.ReactNode; sub?: string; href: string }) {
  return (
    <li>
      <Link href={href} className="block h-full rounded-2xl border border-line bg-paper p-5 hover:border-ink/30">
        <p className="text-sm text-muted">{label}</p>
        <div className="mt-2 font-serif text-2xl">{value}</div>
        {sub && <p className="mt-1 text-xs text-muted">{sub}</p>}
      </Link>
    </li>
  );
}
