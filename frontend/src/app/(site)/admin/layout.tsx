"use client";

import type { ReactNode } from "react";
import { DashboardShell } from "@/components/dashboard/Shell";
import { useI18n } from "@/lib/i18n/client";

export default function AdminLayout({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const n = t.admin.nav;
  return (
    <DashboardShell role="ADMIN" nav={[
      { href: "/admin", label: n.overview }, { href: "/admin/makers", label: n.makers }, { href: "/admin/products", label: n.products },
      { href: "/admin/reports", label: n.reports }, { href: "/admin/categories", label: n.categories }, { href: "/admin/users", label: n.users },
      { href: "/admin/audit", label: n.audit },
    ]}>
      {children}
    </DashboardShell>
  );
}
