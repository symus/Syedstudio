/** Only same-site relative paths are accepted as post-login redirects (prevents open redirects). */
export function safeNext(next: string | null | undefined, fallback: string): string {
  return next && next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/\\") ? next : fallback;
}
