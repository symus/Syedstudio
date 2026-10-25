import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { CookieNotice } from "@/components/site/CookieNotice";
import { AuthProvider } from "@/lib/auth";
import { brand } from "@/lib/config";
import { I18nProvider } from "@/lib/i18n/client";
import { getI18n } from "@/lib/i18n/server";
import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const { t } = await getI18n();
  return {
    metadataBase: new URL(brand.siteUrl),
    title: { default: `${brand.name} — ${t.footer.tagline}`, template: `%s · ${brand.name}` },
    description: t.home.heroText,
    openGraph: { siteName: brand.name, type: "website" },
  };
}

export const viewport: Viewport = { themeColor: "#faf7f2", width: "device-width", initialScale: 1 };

export default async function RootLayout({ children }: { children: ReactNode }) {
  const { locale } = await getI18n();
  return (
    <html lang={locale}>
      <body className="min-h-dvh">
        <I18nProvider locale={locale}>
          <AuthProvider>
            {children}
            <CookieNotice />
          </AuthProvider>
        </I18nProvider>
      </body>
    </html>
  );
}
