import Link from "next/link";
import type { CSSProperties, ReactNode } from "react";
import { InquiryForm } from "@/components/forms/InquiryForm";
import { brand } from "@/lib/config";
import { formatPrice } from "@/lib/format";
import { dictionaryFor, fmt } from "@/lib/i18n";
import type { ProductCard, PublicProduct, StorefrontView } from "@/lib/types";

export type StorePage = "home" | "shop" | "about" | "custom-order" | "contact" | "product";

/** How storefront links are built: real paths for the live site, query strings for the dashboard preview. */
export type StoreLinks = { page: (p: Exclude<StorePage, "product">) => string; product: (slug: string) => string | null };

export function liveLinks(slug: string): StoreLinks {
  return {
    page: (p) => (p === "home" ? `/${slug}` : `/${slug}/${p}`),
    product: (productSlug) => `/${slug}/shop/${productSlug}`,
  };
}

export const previewLinks: StoreLinks = {
  page: (p) => `/preview/storefront?page=${p}`,
  product: () => null,
};

const FONTS = { SERIF: "var(--font-serif)", SANS: "var(--font-sans)", ROUNDED: "var(--font-rounded)" } as const;

/** Picks black or white text for readable contrast on a hex background. */
export function contrastText(hex: string): string {
  const n = Number.parseInt(hex.slice(1), 16);
  const [r, g, b] = [(n >> 16) & 255, (n >> 8) & 255, n & 255].map((c) => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
  });
  const lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
  return lum > 0.4 ? "#1d1a18" : "#ffffff";
}

export function StorefrontShell({ view, links, current, children }: { view: StorefrontView; links: StoreLinks; current: StorePage; children: ReactNode }) {
  const { config, maker } = view;
  const t = dictionaryFor(config.language);
  const template = config.templateKey;
  const style = {
    "--sf-primary": config.primaryColor,
    "--sf-accent": config.accentColor,
    "--sf-on-primary": contrastText(config.primaryColor),
    "--sf-on-accent": contrastText(config.accentColor),
    "--sf-font": FONTS[config.font],
  } as CSSProperties;
  const nav: { key: Exclude<StorePage, "product">; label: string }[] = [
    { key: "home", label: t.storefront.home }, { key: "shop", label: t.storefront.shop }, { key: "about", label: t.storefront.about },
    { key: "custom-order", label: t.storefront.customOrder }, { key: "contact", label: t.storefront.contact },
  ];
  return (
    <div style={style} lang={config.language} className={`sf sf-${template} min-h-dvh bg-[#fdfbf8] text-[#2a2522]`} data-template={template}>
      {view.preview && <div className="bg-amber-100 px-4 py-2 text-center text-sm text-amber-900">{t.storefront.previewBanner}</div>}
      <header className="border-b border-black/5 bg-white/80 backdrop-blur" style={template === "atelier" ? { background: "var(--sf-primary)", color: "var(--sf-on-primary)" } : undefined}>
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-4 sm:px-6">
          <Link href={links.page("home")} className="flex items-center gap-3" style={{ fontFamily: "var(--sf-font)" }}>
            {maker.logoUrl && <img src={maker.logoUrl} alt="" className="h-10 w-10 rounded-full object-cover" />}
            <span className="text-xl">{maker.businessName}</span>
          </Link>
          <nav aria-label={maker.businessName} className="-mx-2 flex max-w-full gap-1 overflow-x-auto text-sm">
            {nav.map((n) => (
              <Link key={n.key} href={links.page(n.key)} aria-current={current === n.key ? "page" : undefined}
                className="whitespace-nowrap rounded-full px-3 py-2 hover:bg-black/5 aria-[current=page]:font-semibold aria-[current=page]:underline aria-[current=page]:underline-offset-4">
                {n.label}
              </Link>
            ))}
          </nav>
        </div>
      </header>
      <main id="main">{children}</main>
      <footer className="mt-20 border-t border-black/5 px-4 py-8 text-center text-xs text-black/60">
        <p>© {new Date().getFullYear()} {maker.businessName}{maker.city ? ` · ${maker.city}` : ""}</p>
        {view.showPlatformBranding && (
          <p className="mt-2"><Link href="/" className="underline">{fmt(t.storefront.poweredBy, { brand: brand.name })}</Link></p>
        )}
      </footer>
    </div>
  );
}

function Section({ title, children, id }: { title?: string; children: ReactNode; id?: string }) {
  return (
    <section id={id} className="mx-auto max-w-6xl px-4 pt-16 sm:px-6">
      {title && <h2 className="mb-6 text-3xl" style={{ fontFamily: "var(--sf-font)" }}>{title}</h2>}
      {children}
    </section>
  );
}

function CtaLink({ href, children }: { href: string; children: ReactNode }) {
  return (
    <Link href={href} className="inline-flex min-h-[44px] items-center rounded-full px-6 text-sm font-medium shadow-sm transition hover:opacity-90"
      style={{ background: "var(--sf-accent)", color: "var(--sf-on-accent)" }}>{children}</Link>
  );
}

function Hero({ view, links }: { view: StorefrontView; links: StoreLinks }) {
  const { config, heroImageUrl, maker } = view;
  const t = dictionaryFor(config.language);
  const title = config.heroTitle || maker.businessName;
  const subtitle = config.heroSubtitle || maker.tagline;
  const image = heroImageUrl ?? maker.coverUrl;
  if (config.templateKey === "studio") {
    return (
      <section className="mx-auto grid max-w-6xl items-center gap-8 px-4 pt-12 sm:px-6 md:grid-cols-2">
        <div>
          <h1 className="text-4xl leading-tight sm:text-5xl" style={{ fontFamily: "var(--sf-font)", color: "var(--sf-primary)" }}>{title}</h1>
          {subtitle && <p className="mt-4 text-lg text-black/70">{subtitle}</p>}
          <div className="mt-6"><CtaLink href={links.page("shop")}>{t.storefront.shop}</CtaLink></div>
        </div>
        <div className="aspect-[4/3] overflow-hidden rounded-2xl bg-black/5">{image && <img src={image} alt="" className="h-full w-full object-cover" />}</div>
      </section>
    );
  }
  if (config.templateKey === "market") {
    return (
      <section className="mx-auto max-w-6xl px-4 pt-8 sm:px-6">
        <div className="overflow-hidden rounded-[2rem] p-8 sm:p-14" style={{ background: "var(--sf-accent)", color: "var(--sf-on-accent)" }}>
          <div className="grid items-center gap-8 md:grid-cols-[1.2fr_1fr]">
            <div>
              <h1 className="text-4xl leading-tight sm:text-5xl" style={{ fontFamily: "var(--sf-font)" }}>{title}</h1>
              {subtitle && <p className="mt-4 text-lg opacity-90">{subtitle}</p>}
              <Link href={links.page("shop")} className="mt-6 inline-flex min-h-[44px] items-center rounded-full px-6 text-sm font-medium"
                style={{ background: "var(--sf-primary)", color: "var(--sf-on-primary)" }}>{t.storefront.shop}</Link>
            </div>
            {image && <img src={image} alt="" className="aspect-square w-full rounded-[1.5rem] object-cover" />}
          </div>
        </div>
      </section>
    );
  }
  return (
    <section className="relative isolate overflow-hidden" style={{ background: "var(--sf-primary)", color: "var(--sf-on-primary)" }}>
      {image && <img src={image} alt="" className="absolute inset-0 -z-10 h-full w-full object-cover opacity-45" />}
      <div className="mx-auto max-w-6xl px-4 py-24 sm:px-6 sm:py-32">
        <h1 className="max-w-2xl text-4xl leading-tight sm:text-6xl" style={{ fontFamily: "var(--sf-font)" }}>{title}</h1>
        {subtitle && <p className="mt-5 max-w-xl text-lg opacity-90">{subtitle}</p>}
        <div className="mt-8"><CtaLink href={links.page("shop")}>{t.storefront.shop}</CtaLink></div>
      </div>
    </section>
  );
}

export function StoreProductGrid({ products, view, links }: { products: ProductCard[]; view: StorefrontView; links: StoreLinks }) {
  const t = dictionaryFor(view.config.language);
  if (!products.length) return <p className="text-black/60">{t.storefront.noProducts}</p>;
  const rounded = view.config.templateKey === "market" ? "rounded-[1.5rem]" : view.config.templateKey === "studio" ? "rounded-md" : "rounded-xl";
  return (
    <ul className="grid grid-cols-2 gap-4 sm:gap-6 md:grid-cols-3 lg:grid-cols-4">
      {products.map((p) => {
        const href = links.product(p.slug);
        const body = (
          <>
            <div className={`aspect-square overflow-hidden bg-black/5 ${rounded}`}>
              {p.imageUrl && <img src={p.imageUrl} alt={p.imageAlt} loading="lazy" className="h-full w-full object-cover transition duration-500 group-hover:scale-[1.03]" />}
            </div>
            <p className="mt-3 text-sm font-medium">{p.name}</p>
            <p className="text-sm text-black/60">{formatPrice(p.priceMinor, p.currency, view.config.language) ?? t.common.priceOnRequest}</p>
          </>
        );
        return <li key={p.id} className="group">{href ? <Link href={href}>{body}</Link> : body}</li>;
      })}
    </ul>
  );
}

function AboutBlock({ view }: { view: StorefrontView }) {
  const t = dictionaryFor(view.config.language);
  const text = view.config.aboutText || view.maker.bio;
  if (!text) return null;
  return (
    <Section title={view.config.aboutTitle || t.storefront.about}>
      <p className="max-w-3xl whitespace-pre-line text-lg leading-relaxed text-black/75">{text}</p>
    </Section>
  );
}

function CustomOrderBlock({ view, links }: { view: StorefrontView; links: StoreLinks }) {
  const t = dictionaryFor(view.config.language);
  return (
    <Section>
      <div className="flex flex-col items-start justify-between gap-6 rounded-[1.5rem] p-8 sm:flex-row sm:items-center"
        style={{ background: "color-mix(in srgb, var(--sf-accent) 14%, white)" }}>
        <div>
          <h2 className="text-2xl" style={{ fontFamily: "var(--sf-font)" }}>{t.storefront.customOrderTitle}</h2>
          <p className="mt-2 text-black/70">{t.storefront.customOrderText}</p>
        </div>
        <CtaLink href={links.page("custom-order")}>{t.storefront.customOrderCta}</CtaLink>
      </div>
    </Section>
  );
}

function ContactBlock({ view }: { view: StorefrontView }) {
  const t = dictionaryFor(view.config.language);
  const m = view.maker;
  const social = Object.entries(m.social).filter(([, url]) => Boolean(url)) as [string, string][];
  return (
    <Section title={t.storefront.contactTitle}>
      <dl className="grid gap-6 text-sm sm:grid-cols-3">
        {m.city && <div><dt className="text-black/50">{t.storefront.location}</dt><dd className="mt-1">{[m.city, m.region].filter(Boolean).join(", ")}</dd></div>}
        {m.contactEmail && <div><dt className="text-black/50">{t.storefront.email}</dt><dd className="mt-1"><a className="underline" href={`mailto:${m.contactEmail}`}>{m.contactEmail}</a></dd></div>}
        {m.phone && <div><dt className="text-black/50">{t.storefront.phone}</dt><dd className="mt-1"><a className="underline" href={`tel:${m.phone}`}>{m.phone}</a></dd></div>}
      </dl>
      {social.length > 0 && (
        <ul className="mt-6 flex flex-wrap gap-2 text-sm">
          {social.map(([name, url]) => <li key={name}><a href={url} target="_blank" rel="noopener noreferrer nofollow" className="rounded-full border border-black/10 px-4 py-2 capitalize hover:bg-black/5">{name}</a></li>)}
        </ul>
      )}
      {m.seller && (
        <div className="mt-6 text-xs text-black/60">
          {[m.seller.legalName, m.seller.taxId && `NIP: ${m.seller.taxId}`, m.seller.businessAddress].filter(Boolean).map((l) => <p key={l as string}>{l}</p>)}
        </div>
      )}
    </Section>
  );
}

export function StorefrontHome({ view, links }: { view: StorefrontView; links: StoreLinks }) {
  const t = dictionaryFor(view.config.language);
  return (
    <>
      {view.config.sections.map((s) => {
        switch (s) {
          case "HERO": return <Hero key={s} view={view} links={links} />;
          // Avoid showing the same few items twice when the full gallery is also on the page.
          case "FEATURED_PRODUCTS": return view.products.length && !(view.config.sections.includes("GALLERY") && view.products.length <= 4) ? (
            <Section key={s} title={view.config.featuredHeading || t.storefront.featured}>
              <StoreProductGrid products={view.products.slice(0, 4)} view={view} links={links} />
            </Section>) : null;
          case "ABOUT": return <AboutBlock key={s} view={view} />;
          case "GALLERY": return (
            <Section key={s} title={t.storefront.gallery}><StoreProductGrid products={view.products} view={view} links={links} /></Section>
          );
          case "CUSTOM_ORDER": return <CustomOrderBlock key={s} view={view} links={links} />;
          case "CONTACT": return <ContactBlock key={s} view={view} />;
          default: return null;
        }
      })}
    </>
  );
}

export function StorefrontPage({ view, links, page, product, productId }: {
  view: StorefrontView; links: StoreLinks; page: StorePage; product?: PublicProduct; productId?: string;
}) {
  const t = dictionaryFor(view.config.language);
  const selected = productId ? view.products.find((p) => p.id === productId) : undefined;
  let body: ReactNode;
  switch (page) {
    case "shop":
      body = <Section title={t.storefront.shop}><StoreProductGrid products={view.products} view={view} links={links} /></Section>;
      break;
    case "about":
      body = <><AboutBlock view={view} />{!view.config.aboutText && !view.maker.bio && <Section title={t.storefront.about}><p>{view.maker.tagline}</p></Section>}</>;
      break;
    case "contact":
      body = <ContactBlock view={view} />;
      break;
    case "custom-order":
      body = (
        <Section title={t.inquiryForm.title}>
          <div className="max-w-2xl rounded-2xl border border-black/10 bg-white p-6">
            <InquiryForm makerSlug={view.maker.slug} makerName={view.maker.businessName} acceptsCustomOrders={view.maker.acceptsCustomOrders}
              productId={selected?.id} productName={selected?.name} />
          </div>
        </Section>
      );
      break;
    case "product":
      body = product ? <StoreProduct view={view} links={links} product={product} /> : null;
      break;
    default:
      body = <StorefrontHome view={view} links={links} />;
  }
  return <StorefrontShell view={view} links={links} current={page}>{body}</StorefrontShell>;
}

function StoreProduct({ view, product: p, links }: { view: StorefrontView; product: PublicProduct; links: StoreLinks }) {
  const t = dictionaryFor(view.config.language);
  return (
    <Section>
      <div className="grid gap-10 md:grid-cols-2">
        <div className="space-y-3">
          {p.images.map((img, i) => (
            <img key={img.id} src={img.url} alt={img.altText} loading={i === 0 ? "eager" : "lazy"} className="w-full rounded-2xl object-cover" />
          ))}
        </div>
        <div>
          <h1 className="text-4xl" style={{ fontFamily: "var(--sf-font)" }}>{p.name}</h1>
          <p className="mt-4 text-2xl">{formatPrice(p.priceMinor, p.currency, view.config.language) ?? t.common.priceOnRequest}</p>
          <p className="mt-2 text-sm text-black/60">
            {p.availabilityType === "READY_MADE" ? t.product.readyMade : t.product.madeToOrder}
            {p.productionDays !== null && ` · ${t.product.productionTime}: ${fmt(t.product.days, { n: p.productionDays })}`}
            {!p.available && ` · ${t.product.unavailable}`}
          </p>
          {p.description && <p className="mt-6 whitespace-pre-line leading-relaxed text-black/75">{p.description}</p>}
          <dl className="mt-6 space-y-2 text-sm">
            {p.materials && <div><dt className="inline text-black/50">{t.product.materials}: </dt><dd className="inline">{p.materials}</dd></div>}
            {p.dimensions && <div><dt className="inline text-black/50">{t.product.dimensions}: </dt><dd className="inline">{p.dimensions}</dd></div>}
            <div><dt className="inline text-black/50">{t.product.shipping}: </dt><dd className="inline">{p.shippingInfo ?? t.product.noShipping}</dd></div>
          </dl>
          {p.options.length > 0 && (
            <ul className="mt-4 text-sm text-black/70">{p.options.map((o) => <li key={o.id}>• {o.name}{o.choices.length ? `: ${o.choices.join(" / ")}` : ""}</li>)}</ul>
          )}
          <div className="mt-8"><CtaLink href={`${links.page("custom-order")}?product=${p.id}`}>{t.product.askMaker}</CtaLink></div>
        </div>
      </div>
    </Section>
  );
}
