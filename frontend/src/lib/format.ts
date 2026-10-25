import type { Locale } from "./types";

const intlLocale = (l: Locale) => (l === "pl" ? "pl-PL" : "en-GB");

/** Prices are stored in minor units (grosze/cents). */
export function formatPrice(minor: number | null | undefined, currency: string, locale: Locale): string | null {
  if (minor === null || minor === undefined) return null;
  return new Intl.NumberFormat(intlLocale(locale), { style: "currency", currency }).format(minor / 100);
}

export function formatDate(iso: string | null | undefined, locale: Locale): string {
  if (!iso) return "";
  return new Intl.DateTimeFormat(intlLocale(locale), { day: "numeric", month: "short", year: "numeric" }).format(new Date(iso));
}

/** "12.50" / "12,50" -> 1250; empty -> null; invalid -> NaN. */
export function parsePriceToMinor(input: string): number | null {
  const trimmed = input.trim().replace(/\s/g, "").replace(",", ".");
  if (!trimmed) return null;
  if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) return Number.NaN;
  return Math.round(Number.parseFloat(trimmed) * 100);
}

export function minorToInput(minor: number | null | undefined): string {
  return minor === null || minor === undefined ? "" : (minor / 100).toFixed(2);
}

export function slugify(input: string): string {
  return input
    .replace(/ł/g, "l").replace(/Ł/g, "L")
    .normalize("NFD").replace(/\p{M}+/gu, "")
    .toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").slice(0, 60);
}
