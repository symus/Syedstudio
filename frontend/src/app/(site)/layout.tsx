import type { ReactNode } from "react";
import { Footer } from "@/components/site/Footer";
import { Header } from "@/components/site/Header";
import { getI18n } from "@/lib/i18n/server";

export default async function SiteLayout({ children }: { children: ReactNode }) {
  const { t } = await getI18n();
  return (
    <>
      <Header />
      <main id="main">{children}</main>
      <Footer t={t} />
    </>
  );
}
