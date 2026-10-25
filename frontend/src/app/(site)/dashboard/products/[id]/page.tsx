"use client";

import { use } from "react";
import { ProductEditor } from "@/components/dashboard/ProductEditor";
import { LoadState } from "@/components/ui/LoadState";
import { useApi } from "@/lib/useApi";
import type { MakerProduct } from "@/lib/types";

export default function EditProductPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data, error, loading, reload } = useApi<MakerProduct>(`/api/maker/products/${id}`);
  return (
    <LoadState loading={loading} error={error} onRetry={reload}>
      {data && <ProductEditor key={data.id} initial={data} />}
    </LoadState>
  );
}
