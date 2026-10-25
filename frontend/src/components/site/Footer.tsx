import Link from "next/link";
import { brand } from "@/lib/config";
import { fmt } from "@/lib/i18n";
import type { Dictionary } from "@/lib/i18n";
import { Container } from "@/components/ui";

export function Footer({ t }: { t: Dictionary }) {
  const year = new Date().getFullYear();
  return (
    <footer className="mt-24 border-t border-line bg-sand/60">
      <Container className="grid gap-10 py-12 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <p className="font-serif text-xl">{brand.name}</p>
          <p className="mt-2 text-sm text-muted">{t.footer.tagline}</p>
        </div>
        <FooterColumn title={t.footer.explore} links={[
          { href: "/marketplace", label: t.nav.marketplace },
          { href: "/makers", label: t.nav.makers },
        ]} />
        <FooterColumn title={t.footer.forMakers} links={[
          { href: "/register", label: t.footer.createWebsite },
          { href: "/pricing", label: t.nav.pricing },
        ]} />
        <FooterColumn title={t.footer.legal} links={[
          { href: "/legal/privacy", label: t.footer.privacy },
          { href: "/legal/terms", label: t.footer.terms },
          { href: `mailto:${brand.supportEmail}`, label: t.footer.contact },
          ...(brand.social.instagram ? [{ href: brand.social.instagram, label: "Instagram" }] : []),
          ...(brand.social.facebook ? [{ href: brand.social.facebook, label: "Facebook" }] : []),
        ]} />
      </Container>
      <Container className="flex flex-col gap-1 border-t border-line py-6 text-xs text-muted sm:flex-row sm:justify-between">
        <p>© {year} {brand.name}. {t.footer.rights}</p>
        <p>{fmt(t.footer.placeholderNotice, { brand: brand.name })}</p>
      </Container>
    </footer>
  );
}

function FooterColumn({ title, links }: { title: string; links: { href: string; label: string }[] }) {
  return (
    <div>
      <p className="text-sm font-semibold">{title}</p>
      <ul className="mt-3 space-y-2 text-sm text-ink-soft">
        {links.map((l) => (
          <li key={l.href}><Link href={l.href} className="hover:text-ink hover:underline">{l.label}</Link></li>
        ))}
      </ul>
    </div>
  );
}
