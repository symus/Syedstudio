import { useId, type ComponentProps, type ReactNode } from "react";
import { cx } from "./index";

const control =
  "w-full rounded-xl border border-line bg-paper px-3.5 py-2.5 text-base text-ink placeholder:text-muted/70 focus:border-ink/50 focus:outline-none focus:ring-2 focus:ring-terracotta/20 aria-[invalid=true]:border-danger sm:text-sm";

interface FieldProps {
  label: string;
  error?: string;
  hint?: string;
  optional?: string;
  children: (props: { id: string; "aria-invalid": boolean; "aria-describedby"?: string }) => ReactNode;
}

/** Label + control + hint/error, wired with ids for screen readers. */
export function Field({ label, error, hint, optional, children }: FieldProps) {
  const id = useId();
  const describedBy = [hint && `${id}-hint`, error && `${id}-error`].filter(Boolean).join(" ") || undefined;
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-ink">
        {label} {optional && <span className="font-normal text-muted">({optional})</span>}
      </label>
      {children({ id, "aria-invalid": Boolean(error), "aria-describedby": describedBy })}
      {hint && <p id={`${id}-hint`} className="text-xs text-muted">{hint}</p>}
      {error && <p id={`${id}-error`} className="text-xs text-danger">{error}</p>}
    </div>
  );
}

export function Input({ className, ...props }: ComponentProps<"input">) {
  return <input className={cx(control, className)} {...props} />;
}

export function Textarea({ className, ...props }: ComponentProps<"textarea">) {
  return <textarea className={cx(control, "min-h-[120px]", className)} {...props} />;
}

export function Select({ className, ...props }: ComponentProps<"select">) {
  return <select className={cx(control, "pr-8", className)} {...props} />;
}

export function Checkbox({ label, className, ...props }: ComponentProps<"input"> & { label: ReactNode }) {
  return (
    <label className={cx("flex cursor-pointer items-start gap-3 text-sm", className)}>
      <input type="checkbox" className="mt-0.5 h-5 w-5 shrink-0 rounded border-line accent-terracotta" {...props} />
      <span>{label}</span>
    </label>
  );
}
