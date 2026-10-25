import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { InquiryForm } from "@/components/forms/InquiryForm";
import { Container } from "@/components/ui";
import { publicApi } from "@/lib/api";
import { getI18n } from "@/lib/i18n/server";
import type { Page, ProductCard, PublicMaker } from "@/lib/types";

export const metadata: Metadata = { robots: { index: false } };

export default async function MakerInquiryPage({ params, searchParams }: {
  params: Promise<{ slug: string }>; searchParams: Promise<{ product?: string }>;
}) {
  const { slug } = await params;
  const { product } = await searchParams;
  const { t } = await getI18n();
  const m = await publicApi<PublicMaker>(`/api/public/makers/${encodeURIComponent(slug)}`);
  if (!m) notFound();
  let productName: string | null = null;
  if (product) {
    const list = await publicApi<Page<ProductCard>>(`/api/public/products?maker=${encodeURIComponent(slug)}&size=60`, 60);
    productName = list?.items.find((p) => p.id === product)?.name ?? null;
  }
  return (
    <Container className="max-w-2xl py-10">
      <p className="text-sm"><Link href={`/makers/${m.slug}`} className="underline">← {m.businessName}</Link></p>
      <h1 className="mt-3 text-3xl">{t.inquiryForm.title}</h1>
      <div className="mt-6 rounded-2xl border border-line bg-paper p-6">
        <InquiryForm makerSlug={m.slug} makerName={m.businessName} acceptsCustomOrders={m.acceptsCustomOrders}
          productId={productName ? product : null} productName={productName} />
      </div>
    </Container>
  );
}
