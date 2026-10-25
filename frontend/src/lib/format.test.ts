import { formatPrice, minorToInput, parsePriceToMinor, slugify } from "./format";
import { fmt } from "./i18n";
import en from "./i18n/en";
import pl from "./i18n/pl";
import { safeNext } from "./redirect";

describe("prices", () => {
  it("parses Polish and English decimal input to minor units", () => {
    expect(parsePriceToMinor("49,99")).toBe(4999);
    expect(parsePriceToMinor("1 200.5")).toBe(120050);
    expect(parsePriceToMinor("")).toBeNull();
    expect(parsePriceToMinor("12.345")).toBeNaN();
    expect(parsePriceToMinor("-3")).toBeNaN();
  });
  it("round-trips and formats", () => {
    expect(minorToInput(4999)).toBe("49.99");
    expect(formatPrice(null, "PLN", "pl")).toBeNull();
    expect(formatPrice(8900, "PLN", "pl")).toMatch(/89,00/);
  });
});

describe("slugify", () => {
  it("handles Polish diacritics", () => {
    expect(slugify("Łódzka Pracownia Świec!")).toBe("lodzka-pracownia-swiec");
  });
});

describe("safeNext", () => {
  it("only allows same-site relative paths", () => {
    expect(safeNext("/dashboard", "/")).toBe("/dashboard");
    expect(safeNext("//evil.com", "/")).toBe("/");
    expect(safeNext("https://evil.com", "/")).toBe("/");
    expect(safeNext("/\\evil.com", "/")).toBe("/");
    expect(safeNext(null, "/account")).toBe("/account");
  });
});

describe("i18n", () => {
  it("fills placeholders", () => {
    expect(fmt("Hi {name}, {n} new", { name: "Ania", n: 3 })).toBe("Hi Ania, 3 new");
  });
  it("Polish dictionary has every English key", () => {
    const keys = (o: object, prefix = ""): string[] =>
      Object.entries(o).flatMap(([k, v]) => (v && typeof v === "object" && !Array.isArray(v) ? keys(v, `${prefix}${k}.`) : [`${prefix}${k}`]));
    expect(keys(pl).sort()).toEqual(keys(en).sort());
  });
});
