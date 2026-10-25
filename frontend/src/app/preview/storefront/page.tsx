"use client";

import { useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { previewLinks, StorefrontPage, type StorePage } from "@/components/storefront/Storefront";
import { Container } from "@/components/ui";
import { LoadState } from "@/components/ui/LoadState";
import { useApi } from "@/lib/useApi";
import type { StorefrontView } from "@/lib/types";

const PAGES: StorePage[] = ["home", "shop", "about", "custom-order", "contact"];

/** Renders the maker's DRAFT storefront (authenticated). Used directly and inside the builder's device frames. */
function Preview() {
  const params = useSearchParams();
  const requested = params.get("page") as StorePage | null;
  const page = requested && PAGES.includes(requested) ? requested : "home";
  const { data, error, loading, reload } = useApi<StorefrontView>(`/api/maker/storefront/preview?v=${params.get("v") ?? ""}`);
  if (!data) return <Container className="py-10"><LoadState loading={loading} error={error} onRetry={reload}>{null}</LoadState></Container>;
  return <StorefrontPage view={data} links={previewLinks} page={page} />;
}

export default function StorefrontPreviewPage() {
  return <Suspense><Preview /></Suspense>;
}
