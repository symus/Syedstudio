import { cx } from "@/components/ui";

/** Plain GET form so search works without JavaScript and produces shareable URLs. */
export function SearchBar({ placeholder, label, defaultValue, action = "/marketplace", large, hidden }: {
  placeholder: string; label: string; defaultValue?: string; action?: string; large?: boolean;
  hidden?: Record<string, string | undefined>;
}) {
  return (
    <form role="search" action={action} method="get" className="relative w-full">
      {hidden && Object.entries(hidden).map(([k, v]) => (v ? <input key={k} type="hidden" name={k} value={v} /> : null))}
      <label htmlFor={`search-${action}`} className="sr-only">{label}</label>
      <input id={`search-${action}`} name="q" type="search" defaultValue={defaultValue} placeholder={placeholder} maxLength={100}
        className={cx("w-full rounded-full border border-line bg-paper pl-5 pr-28 text-base shadow-[var(--shadow-soft)] focus:border-ink/40 focus:outline-none focus:ring-2 focus:ring-terracotta/20",
          large ? "h-14" : "h-12")} />
      <button type="submit" className={cx("absolute right-1.5 top-1.5 rounded-full bg-ink px-5 text-sm text-cream hover:bg-ink/90", large ? "h-11" : "h-9")}>
        {label}
      </button>
    </form>
  );
}
