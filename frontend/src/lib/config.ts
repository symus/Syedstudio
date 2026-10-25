/** Brand and deployment settings. The brand name is a placeholder until trademark/domain checks are done. */
export const brand = {
  name: process.env.NEXT_PUBLIC_BRAND_NAME ?? "Craftora",
  siteUrl: (process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000").replace(/\/$/, ""),
  supportEmail: process.env.NEXT_PUBLIC_SUPPORT_EMAIL ?? "hello@craftora.example",
  social: {
    instagram: process.env.NEXT_PUBLIC_SOCIAL_INSTAGRAM ?? "",
    facebook: process.env.NEXT_PUBLIC_SOCIAL_FACEBOOK ?? "",
  },
};

/** Server-side base URL of the API (not exposed to the browser, which uses the /api rewrite). */
export const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
