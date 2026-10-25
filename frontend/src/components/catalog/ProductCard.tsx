import Link from "next/link";
import { formatPrice } from "@/lib/format";
import type { Dictionary } from "@/lib/i18n";
import type { Locale, ProductCard as Card } from "@/lib/types";

export function ProductCard({ product, locale, t, href }: { product: Card; locale: Locale; t: Dictionary; href?: string }) {
  const price = formatPrice(product.priceMinor, product.currency, locale);
  return (
    <Link href={href ?? `/p/${product.makerSlug}/${product.slug}`}
      className="group block overflow-hidden rounded-[var(--radius-card)] border border-line bg-paper shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5 hover:shadow-[var(--shadow-lift)]">
      <div className="aspect-[4/5] overflow-hidden bg-sand">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.imageAlt} loading="lazy" decoding="async" width={480} height={600}
            className="h-full w-full object-cover transition duration-500 group-hover:scale-[1.03]" />
        ) : (
          <div className="grid h-full place-items-center text-4xl text-line" aria-hidden>✿</div>
        )}
      </div>
      <div className="space-y-1 p-4">
        <h3 className="line-clamp-2 font-sans text-sm font-medium leading-snug">{product.name}</h3>
        <p className="truncate text-xs text-muted">
          {product.makerName}{product.makerCity ? ` · ${product.makerCity}` : ""}
        </p>
        <p className="pt-1 text-sm font-semibold">{price ?? <span className="font-normal text-muted">{t.common.priceOnRequest}</span>}</p>
        {!product.available && <p className="text-xs text-warning">{t.product.unavailable}</p>}
      </div>
    </Link>
  );
}

export function ProductGrid({ products, locale, t, hrefFor }: {
  products: Card[]; locale: Locale; t: Dictionary; hrefFor?: (p: Card) => string;
}) {
  return (
    <ul className="grid grid-cols-2 gap-3 sm:gap-5 md:grid-cols-3 lg:grid-cols-4">
      {products.map((p) => (
        <li key={p.id}><ProductCard product={p} locale={locale} t={t} href={hrefFor?.(p)} /></li>
      ))}
    </ul>
  );
}
