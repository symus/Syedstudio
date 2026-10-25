import type { Metadata } from "next";
import { LegalPage } from "@/components/site/LegalPage";
import { brand } from "@/lib/config";
import { getI18n } from "@/lib/i18n/server";

export const metadata: Metadata = { title: "Terms of service", alternates: { canonical: "/legal/terms" } };

// PLACEHOLDER — must be replaced with terms reviewed by a lawyer (incl. P2B and DSA obligations) before launch.
export default async function TermsPage() {
  const { t, locale } = await getI18n();
  const sections = locale === "pl" ? [
    { h: "Usługa", p: `${brand.name} udostępnia twórcom narzędzia do tworzenia stron oraz wspólny katalog produktów. Platforma nie jest stroną transakcji między klientem a twórcą.` },
    { h: "Konta twórców", p: "Twórca odpowiada za prawdziwość danych, legalność oferowanych produktów i realizację zamówień. Konta mogą wymagać zatwierdzenia." },
    { h: "Moderacja", p: "Możemy ukryć ogłoszenia lub zawiesić konta naruszające regulamin. Treści można zgłaszać przyciskiem „Zgłoś”." },
    { h: "Plany i płatności", p: "Ceny planów płatnych są cenami testowymi na czas pilotażu." },
  ] : [
    { h: "The service", p: `${brand.name} gives makers tools to build websites and a shared product catalogue. The platform is not a party to transactions between customers and makers.` },
    { h: "Maker accounts", p: "Makers are responsible for accurate information, the legality of their products, and fulfilling orders. Accounts may require approval." },
    { h: "Moderation", p: "We may hide listings or suspend accounts that break these terms. Content can be reported with the “Report” button." },
    { h: "Plans and payments", p: "Paid plan prices are test prices during the pilot." },
  ];
  return <LegalPage title={t.footer.terms} notice={t.legal.reviewNotice} sections={sections} />;
}
