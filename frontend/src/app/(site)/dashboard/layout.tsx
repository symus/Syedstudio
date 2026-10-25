"use client";

import type { ReactNode } from "react";
import { DashboardShell } from "@/components/dashboard/Shell";
import { useI18n } from "@/lib/i18n/client";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const n = t.dash.nav;
  return (
    <DashboardShell role="MAKER" nav={[
      { href: "/dashboard", label: n.overview }, { href: "/dashboard/products", label: n.products },
      { href: "/dashboard/storefront", label: n.storefront }, { href: "/dashboard/inquiries", label: n.inquiries },
      { href: "/dashboard/profile", label: n.profile }, { href: "/dashboard/plan", label: n.plan },
    ]}>
      {children}
    </DashboardShell>
  );
}
