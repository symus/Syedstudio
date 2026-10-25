import { ButtonLink, Container } from "@/components/ui";
import { getI18n } from "@/lib/i18n/server";

export default async function NotFound() {
  const { t } = await getI18n();
  return (
    <Container className="flex min-h-[60vh] flex-col items-center justify-center py-20 text-center">
      <p className="font-serif text-7xl text-terracotta">404</p>
      <h1 className="mt-4 text-3xl">{t.errors.notFoundTitle}</h1>
      <p className="mt-2 max-w-md text-muted">{t.errors.notFoundText}</p>
      <ButtonLink href="/" className="mt-8">{t.errors.goHome}</ButtonLink>
    </Container>
  );
}
