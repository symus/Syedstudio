import { render, screen } from "@testing-library/react";
import type { StorefrontView } from "@/lib/types";
import { contrastText, liveLinks, StorefrontPage } from "./Storefront";

vi.mock("@/components/forms/InquiryForm", () => ({ InquiryForm: () => <div>inquiry-form</div> }));

const view: StorefrontView = {
  maker: { id: "1", slug: "glina", businessName: "Pracownia Glina", tagline: "Ceramika", bio: "O nas", city: "Kraków", region: null,
    countryCode: "PL", logoUrl: null, coverUrl: null, contactEmail: "a@b.pl", phone: null,
    social: { website: null, instagram: "https://instagram.com/glina", facebook: null, tiktok: null, pinterest: null },
    acceptsCustomOrders: true, primaryCategorySlug: null, seller: null, hasStorefront: true, memberSince: null },
  config: { templateKey: "studio", primaryColor: "#1F2933", accentColor: "#6B8F71", font: "SANS", heroTitle: "Witaj",
    heroSubtitle: null, heroImageKey: null, aboutTitle: null, aboutText: null, featuredHeading: null,
    sections: ["HERO", "CONTACT"], language: "pl", seoTitle: null, seoDescription: null },
  heroImageUrl: null, products: [], showPlatformBranding: true, preview: false,
};

describe("StorefrontPage", () => {
  it("renders configured sections in the website's language", () => {
    render(<StorefrontPage view={view} links={liveLinks("glina")} page="home" />);
    expect(screen.getByRole("heading", { name: "Witaj" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Skontaktuj się" })).toBeInTheDocument();
    expect(screen.getByRole("navigation", { name: "Pracownia Glina" }).querySelector("a[href=\"/glina/shop\"]")).toHaveTextContent("Sklep");
    expect(screen.queryByText("Wyróżnione prace")).not.toBeInTheDocument();
  });

  it("hides platform branding when the plan removes it", () => {
    render(<StorefrontPage view={{ ...view, showPlatformBranding: false }} links={liveLinks("glina")} page="contact" />);
    expect(screen.queryByText(/Strona stworzona w/)).not.toBeInTheDocument();
  });

  it("picks readable text colours", () => {
    expect(contrastText("#ffffff")).toBe("#1d1a18");
    expect(contrastText("#1F2933")).toBe("#ffffff");
  });
});
