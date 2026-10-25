"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/lib/auth";
import { brand } from "@/lib/config";
import { useI18n } from "@/lib/i18n/client";
import { ButtonLink, Container, cx } from "@/components/ui";
import { LocaleSwitcher } from "./LocaleSwitcher";

export function Logo() {
  return (
    <Link href="/" className="flex items-center gap-2 font-serif text-xl tracking-tight text-ink">
      <span aria-hidden className="grid h-8 w-8 place-items-center rounded-full bg-terracotta text-sm text-white">✿</span>
      {brand.name}
    </Link>
  );
}

export function Header() {
  const { t } = useI18n();
  const { me, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [open, setOpen] = useState(false);

  const links = [
    { href: "/marketplace", label: t.nav.marketplace },
    { href: "/makers", label: t.nav.makers },
    { href: "/pricing", label: t.nav.pricing },
  ];
  const accountLinks = me
    ? [
        ...(me.roles.includes("MAKER") ? [{ href: "/dashboard", label: t.nav.dashboard }] : []),
        ...(me.roles.includes("ADMIN") ? [{ href: "/admin", label: t.nav.admin }] : []),
        { href: "/account", label: t.nav.account },
      ]
    : [];

  async function handleLogout() {
    await logout();
    setOpen(false);
    router.push("/");
    router.refresh();
  }

  return (
    <header className="sticky top-0 z-40 border-b border-line/70 bg-cream/90 backdrop-blur">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:left-3 focus:top-3 focus:rounded focus:bg-paper focus:px-3 focus:py-2">
        {t.nav.skip}
      </a>
      <Container className="flex h-16 items-center justify-between gap-4">
        <Logo />
        <nav aria-label="Main" className="hidden items-center gap-1 md:flex">
          {links.map((l) => (
            <Link key={l.href} href={l.href}
              className={cx("rounded-full px-3 py-2 text-sm hover:bg-sand", pathname.startsWith(l.href) && "bg-sand font-medium")}>
              {l.label}
            </Link>
          ))}
        </nav>
        <div className="hidden items-center gap-2 md:flex">
          <LocaleSwitcher />
          {me ? (
            <>
              {accountLinks.map((l) => (
                <Link key={l.href} href={l.href} className="rounded-full px-3 py-2 text-sm hover:bg-sand">{l.label}</Link>
              ))}
              <button type="button" onClick={handleLogout} className="rounded-full px-3 py-2 text-sm text-muted hover:bg-sand">
                {t.nav.logout}
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="rounded-full px-3 py-2 text-sm hover:bg-sand">{t.nav.login}</Link>
              <ButtonLink href="/register" className="py-2">{t.nav.register}</ButtonLink>
            </>
          )}
        </div>
        <button type="button" className="grid h-11 w-11 place-items-center rounded-full hover:bg-sand md:hidden"
          aria-expanded={open} aria-controls="mobile-nav" aria-label={t.nav.menu} onClick={() => setOpen((o) => !o)}>
          <span aria-hidden className="text-xl">{open ? "✕" : "☰"}</span>
        </button>
      </Container>
      {open && (
        <nav id="mobile-nav" aria-label="Mobile" className="border-t border-line bg-cream md:hidden">
          <Container className="flex flex-col py-3">
            {[...links, ...accountLinks].map((l) => (
              <Link key={l.href} href={l.href} onClick={() => setOpen(false)} className="rounded-lg px-2 py-3 hover:bg-sand">{l.label}</Link>
            ))}
            {me ? (
              <button type="button" onClick={handleLogout} className="rounded-lg px-2 py-3 text-left text-muted hover:bg-sand">{t.nav.logout}</button>
            ) : (
              <div className="mt-2 flex gap-2">
                <ButtonLink href="/login" variant="secondary" className="flex-1" onClick={() => setOpen(false)}>{t.nav.login}</ButtonLink>
                <ButtonLink href="/register" className="flex-1" onClick={() => setOpen(false)}>{t.nav.register}</ButtonLink>
              </div>
            )}
            <LocaleSwitcher className="mt-3 self-start" />
          </Container>
        </nav>
      )}
    </header>
  );
}
