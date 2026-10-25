"use client";

import { Button, Container } from "@/components/ui";
import { useI18n } from "@/lib/i18n/client";

export default function SiteError({ reset }: { error: Error; reset: () => void }) {
  const { t } = useI18n();
  return (
    <Container className="py-20 text-center">
      <h1 className="text-3xl">{t.errors.generic}</h1>
      <Button className="mt-6" onClick={reset}>{t.common.retry}</Button>
    </Container>
  );
}
