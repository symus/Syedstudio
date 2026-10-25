import type { Metadata } from "next";
import { LegalPage } from "@/components/site/LegalPage";
import { brand } from "@/lib/config";
import { getI18n } from "@/lib/i18n/server";

export const metadata: Metadata = { title: "Privacy policy", alternates: { canonical: "/legal/privacy" } };

// PLACEHOLDER — must be replaced with text reviewed by a lawyer (GDPR Art. 13/14) before launch.
export default async function PrivacyPage() {
  const { t, locale } = await getI18n();
  const sections = locale === "pl" ? [
    { h: "Administrator danych", p: `Administratorem danych jest operator platformy ${brand.name} (dane rejestrowe do uzupełnienia). Kontakt: ${brand.supportEmail}.` },
    { h: "Jakie dane przetwarzamy", p: "Dane konta (e-mail, imię, hasło w postaci skrótu), dane profilu twórcy, treści produktów oraz treść zapytań o zamówienia wraz z załączonymi zdjęciami." },
    { h: "Cele i podstawy", p: "Świadczenie usługi (art. 6 ust. 1 lit. b RODO), obsługa zapytań (zgoda, art. 6 ust. 1 lit. a), bezpieczeństwo i zapobieganie nadużyciom (prawnie uzasadniony interes, art. 6 ust. 1 lit. f)." },
    { h: "Odbiorcy", p: "Twórca, do którego kierujesz zapytanie, dostawcy hostingu, przechowywania plików i poczty e-mail działający na nasze zlecenie." },
    { h: "Twoje prawa", p: "Dostęp, sprostowanie, usunięcie, ograniczenie, przenoszenie danych i sprzeciw. Eksport i usunięcie konta są dostępne w zakładce „Moje konto”. Przysługuje Ci skarga do Prezesa UODO." },
    { h: "Pliki cookie", p: "Używamy wyłącznie niezbędnych plików cookie: sesji logowania, ochrony CSRF i zapamiętania języka." },
  ] : [
    { h: "Data controller", p: `The controller is the operator of ${brand.name} (company details to be completed). Contact: ${brand.supportEmail}.` },
    { h: "What we process", p: "Account data (email, name, hashed password), maker profile data, product content, and custom-order inquiries including attached images." },
    { h: "Purposes and legal bases", p: "Providing the service (GDPR Art. 6(1)(b)), handling inquiries (consent, Art. 6(1)(a)), security and abuse prevention (legitimate interest, Art. 6(1)(f))." },
    { h: "Recipients", p: "The maker you contact, and hosting, file-storage and email providers acting on our behalf." },
    { h: "Your rights", p: "Access, rectification, erasure, restriction, portability and objection. Data export and account deletion are available under “My account”. You may complain to your data protection authority." },
    { h: "Cookies", p: "We only use strictly necessary cookies: login session, CSRF protection and language preference." },
  ];
  return <LegalPage title={t.footer.privacy} notice={t.legal.reviewNotice} sections={sections} />;
}
