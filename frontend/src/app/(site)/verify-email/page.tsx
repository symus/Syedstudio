"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useRef, useState } from "react";
import { AuthCard } from "@/components/site/AuthCard";
import { Alert, Spinner } from "@/components/ui";
import { useAuth } from "@/lib/auth";
import { api } from "@/lib/client";
import { useI18n } from "@/lib/i18n/client";

function Verify() {
  const { t } = useI18n();
  const { refresh } = useAuth();
  const token = useSearchParams().get("token");
  const [state, setState] = useState<"pending" | "ok" | "failed">(token ? "pending" : "failed");
  const started = useRef(false);

  useEffect(() => {
    if (!token || started.current) return;
    started.current = true;
    api("/api/auth/verify-email", { body: { token } })
      .then(() => { setState("ok"); void refresh(); })
      .catch(() => setState("failed"));
  }, [token, refresh]);

  if (state === "pending") return <Spinner label={t.auth.verifying} />;
  return (
    <div className="space-y-4">
      <Alert tone={state === "ok" ? "success" : "error"}>{state === "ok" ? t.auth.verified : t.auth.verifyFailed}</Alert>
      <Link href="/dashboard" className="underline">{t.nav.dashboard}</Link>
    </div>
  );
}

export default function VerifyEmailPage() {
  const { t } = useI18n();
  return <AuthCard title={t.auth.verifyTitle}><Suspense><Verify /></Suspense></AuthCard>;
}
