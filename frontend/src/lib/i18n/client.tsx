"use client";

import { createContext, useContext, type ReactNode } from "react";
import { dictionaryFor, type Dictionary } from "./index";
import type { Locale } from "../types";

const I18nContext = createContext<{ locale: Locale; t: Dictionary }>({ locale: "pl", t: dictionaryFor("pl") });

export function I18nProvider({ locale, children }: { locale: Locale; children: ReactNode }) {
  return <I18nContext.Provider value={{ locale, t: dictionaryFor(locale) }}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  return useContext(I18nContext);
}
