import Link from "next/link";
import type { ComponentProps, ReactNode } from "react";

export function cx(...classes: (string | false | null | undefined)[]) {
  return classes.filter(Boolean).join(" ");
}

type Variant = "primary" | "secondary" | "ghost" | "danger" | "sage";
const variants: Record<Variant, string> = {
  primary: "bg-ink text-cream hover:bg-ink/90",
  secondary: "bg-paper text-ink border border-line hover:border-ink/40",
  ghost: "text-ink hover:bg-sand",
  danger: "bg-danger text-white hover:bg-danger/90",
  sage: "bg-terracotta text-white hover:bg-terracotta-dark",
};
const base =
  "inline-flex items-center justify-center gap-2 rounded-full px-5 py-2.5 text-sm font-medium transition-colors disabled:opacity-50 disabled:pointer-events-none min-h-[44px]";

export function Button({ variant = "primary", className, ...props }: ComponentProps<"button"> & { variant?: Variant }) {
  return <button className={cx(base, variants[variant], className)} {...props} />;
}

export function ButtonLink({ variant = "primary", className, ...props }: ComponentProps<typeof Link> & { variant?: Variant }) {
  return <Link className={cx(base, variants[variant], className)} {...props} />;
}

export function Card({ className, ...props }: ComponentProps<"div">) {
  return <div className={cx("rounded-[var(--radius-card)] border border-line bg-paper shadow-[var(--shadow-soft)]", className)} {...props} />;
}

export function Badge({ tone = "neutral", children }: { tone?: "neutral" | "success" | "warning" | "danger" | "accent"; children: ReactNode }) {
  const tones = {
    neutral: "bg-sand text-ink-soft",
    success: "bg-sage-soft text-success",
    warning: "bg-amber-50 text-warning",
    danger: "bg-red-50 text-danger",
    accent: "bg-[#f6e6dd] text-terracotta-dark",
  };
  return <span className={cx("inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium", tones[tone])}>{children}</span>;
}

export function Container({ className, ...props }: ComponentProps<"div">) {
  return <div className={cx("mx-auto w-full max-w-6xl px-4 sm:px-6", className)} {...props} />;
}

export function SectionTitle({ title, action, subtitle }: { title: string; subtitle?: string; action?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h2 className="text-2xl sm:text-3xl">{title}</h2>
        {subtitle && <p className="mt-1 text-muted">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

export function EmptyState({ title, text, action }: { title: string; text?: string; action?: ReactNode }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-dashed border-line bg-paper/60 px-6 py-12 text-center">
      <p className="font-serif text-xl">{title}</p>
      {text && <p className="mx-auto mt-2 max-w-md text-muted">{text}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

export function Alert({ tone = "info", children }: { tone?: "info" | "error" | "success" | "warning"; children: ReactNode }) {
  const tones = {
    info: "border-line bg-sand text-ink",
    error: "border-red-200 bg-red-50 text-danger",
    success: "border-green-200 bg-sage-soft text-success",
    warning: "border-amber-200 bg-amber-50 text-warning",
  };
  return (
    <div role={tone === "error" ? "alert" : "status"} className={cx("rounded-xl border px-4 py-3 text-sm", tones[tone])}>
      {children}
    </div>
  );
}

export function Spinner({ label }: { label: string }) {
  return (
    <div role="status" className="flex items-center gap-3 py-10 text-muted">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-line border-t-terracotta" aria-hidden />
      {label}
    </div>
  );
}
