import type { MetadataRoute } from "next";
import { brand } from "@/lib/config";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: { userAgent: "*", allow: "/", disallow: ["/dashboard", "/admin", "/account", "/preview", "/api/"] },
    sitemap: `${brand.siteUrl}/sitemap.xml`,
  };
}
