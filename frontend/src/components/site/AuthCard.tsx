import type { ReactNode } from "react";
import { Container } from "@/components/ui";

export function AuthCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Container className="flex justify-center py-14">
      <div className="w-full max-w-md rounded-[var(--radius-card)] border border-line bg-paper p-6 shadow-[var(--shadow-soft)] sm:p-8">
        <h1 className="text-3xl">{title}</h1>
        <div className="mt-6">{children}</div>
      </div>
    </Container>
  );
}
