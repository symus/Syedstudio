import Link from "next/link";
import { fmt, type Dictionary } from "@/lib/i18n";
import type { MakerCard as Card } from "@/lib/types";

export function MakerCard({ maker, t }: { maker: Card; t: Dictionary }) {
  return (
    <Link href={`/makers/${maker.slug}`}
      className="group block overflow-hidden rounded-[var(--radius-card)] border border-line bg-paper shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5 hover:shadow-[var(--shadow-lift)]">
      <div className="h-28 bg-gradient-to-br from-sand to-[#ead9cc]">
        {maker.coverUrl && <img src={maker.coverUrl} alt="" loading="lazy" className="h-full w-full object-cover" />}
      </div>
      <div className="-mt-8 px-4 pb-4">
        <div className="h-16 w-16 overflow-hidden rounded-full border-4 border-paper bg-sand">
          {maker.logoUrl ? <img src={maker.logoUrl} alt="" loading="lazy" className="h-full w-full object-cover" />
            : <span className="grid h-full place-items-center font-serif text-xl text-terracotta">{maker.businessName.charAt(0)}</span>}
        </div>
        <h3 className="mt-2 font-serif text-lg leading-tight">{maker.businessName}</h3>
        {maker.tagline && <p className="mt-1 line-clamp-2 text-sm text-muted">{maker.tagline}</p>}
        <p className="mt-2 text-xs text-ink-soft">
          {maker.city ? `${maker.city} · ` : ""}{fmt(t.makers.products, { n: maker.productCount })}
        </p>
      </div>
    </Link>
  );
}
