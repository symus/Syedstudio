"use client";

import type { ReactNode } from "react";
import { useI18n } from "@/lib/i18n/client";
import { Alert, Button, Spinner } from "./index";

/** Renders loading / error-with-retry states, otherwise the children. */
export function LoadState({ loading, error, onRetry, children }: {
  loading: boolean; error: Error | null; onRetry: () => void; children: ReactNode;
}) {
  const { t } = useI18n();
  if (loading) return <Spinner label={t.common.loading} />;
  if (error) {
    return (
      <div className="space-y-3">
        <Alert tone="error">{error.message || t.errors.generic}</Alert>
        <Button variant="secondary" onClick={onRetry}>{t.common.retry}</Button>
      </div>
    );
  }
  return <>{children}</>;
}
