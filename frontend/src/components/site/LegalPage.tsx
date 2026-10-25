import { Alert, Container } from "@/components/ui";

export function LegalPage({ title, notice, sections }: { title: string; notice: string; sections: { h: string; p: string }[] }) {
  return (
    <Container className="max-w-3xl py-14">
      <h1 className="text-4xl">{title}</h1>
      <div className="mt-6"><Alert tone="warning">{notice}</Alert></div>
      <div className="mt-8 space-y-6 leading-relaxed text-ink-soft">
        {sections.map((s) => (
          <section key={s.h}><h2 className="text-xl text-ink">{s.h}</h2><p className="mt-2">{s.p}</p></section>
        ))}
      </div>
    </Container>
  );
}
