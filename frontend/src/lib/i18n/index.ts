import en, { type Dictionary } from "./en";
import pl from "./pl";
import type { Locale } from "../types";

export type { Dictionary };
export const LOCALES: Locale[] = ["pl", "en"];
export const DEFAULT_LOCALE: Locale = "pl";
export const LOCALE_COOKIE = "locale";

const dictionaries: Record<Locale, Dictionary> = { pl, en };

export function dictionaryFor(locale: Locale): Dictionary {
  return dictionaries[locale] ?? dictionaries[DEFAULT_LOCALE];
}

export function isLocale(value: string | undefined | null): value is Locale {
  return value === "pl" || value === "en";
}

/** Replaces {placeholders} in a dictionary string. */
export function fmt(template: string, values: Record<string, string | number>): string {
  return template.replace(/\{(\w+)\}/g, (_, key: string) => String(values[key] ?? `{${key}}`));
}
