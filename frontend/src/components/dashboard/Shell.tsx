"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { Alert, Container, Spinner, cx } from "@/components/ui";
import { useAuth } from "@/lib/auth";
import { api } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";
import type { Role } from "@/lib/types";

/** Client-side role gate + side navigation. Authorisation is still enforced by every backend endpoint. */
export function DashboardShell({ role, nav, children }: { role: Role; nav: { href: string; label: string }[]; children: ReactNode }) {
  const { t } = useI18n();
  const { me, loading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [resent, setResent] = useState(false);

  useEffect(() => {
    if (!loading && !me) router.replace(`/login?next=${encodeURIComponent(pathname)}`);
  }, [loading, me, pathname, router]);

  if (loading || !me) return <Container className="py-10"><Spinner label={t.common.loading} /></Container>;
  if (!me.roles.includes(role)) return <Container className="py-10"><Alert tone="error">{t.errors.forbidden}</Alert></Container>;

  const root = nav[0]?.href;
  return (
    <Container className="py-8">
      {!me.emailVerified && (
        <div className="mb-6">
          <Alert tone="warning">
            {t.auth.verifyBanner}{" "}
            <button type="button" className="underline" disabled={resent}
              onClick={() => api("/api/auth/resend-verification", { method: "POST" }).then(() => setResent(true)).catch(() => setResent(false))}>
              {resent ? t.auth.resent : t.auth.resend}
            </button>
          </Alert>
        </div>
      )}
      <div className="grid gap-8 lg:grid-cols-[220px_1fr]">
        <nav aria-label="Dashboard" className="-mx-4 overflow-x-auto px-4 lg:mx-0 lg:px-0">
          <ul className="flex gap-1 lg:flex-col">
            {nav.map((n) => {
              const active = n.href === root ? pathname === n.href : pathname.startsWith(n.href);
              return (
                <li key={n.href}>
                  <Link href={n.href} aria-current={active ? "page" : undefined}
                    className={cx("block whitespace-nowrap rounded-xl px-4 py-2.5 text-sm", active ? "bg-ink text-cream" : "hover:bg-sand")}>
                    {n.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
        <div className="min-w-0">{children}</div>
      </div>
    </Container>
  );
}
