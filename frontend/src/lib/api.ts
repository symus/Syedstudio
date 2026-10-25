import "server-only";
import { backendUrl } from "./config";

/**
 * Server-side fetch for public data (used by server components for SSR/SEO).
 * Returns null on 404 so pages can call notFound(); throws on other errors so error boundaries show retry.
 */
export async function publicApi<T>(path: string, revalidateSeconds = 60): Promise<T | null> {
  const res = await fetch(`${backendUrl}${path}`, {
    next: { revalidate: revalidateSeconds },
    headers: { Accept: "application/json" },
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`API ${path} failed with ${res.status}`);
  return (await res.json()) as T;
}
