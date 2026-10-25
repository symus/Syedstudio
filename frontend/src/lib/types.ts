// Mirrors the backend DTOs (see backend/src/main/java/com/craftora/**/..Dtos.java).

export type Locale = "pl" | "en";
export type Role = "CUSTOMER" | "MAKER" | "ADMIN";
export type MakerStatus = "PENDING" | "APPROVED" | "SUSPENDED";
export type ProductStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
export type AvailabilityType = "READY_MADE" | "MADE_TO_ORDER";
export type InquiryStatus = "NEW" | "IN_PROGRESS" | "QUOTED" | "ACCEPTED" | "COMPLETED" | "DECLINED";
export type Occasion =
  | "BIRTHDAY" | "WEDDING" | "ANNIVERSARY" | "CHRISTMAS" | "VALENTINES" | "EASTER" | "BABY"
  | "HOUSEWARMING" | "MOTHERS_DAY" | "FATHERS_DAY" | "GRADUATION" | "EVERYDAY";

export const OCCASIONS: Occasion[] = [
  "BIRTHDAY", "WEDDING", "ANNIVERSARY", "CHRISTMAS", "VALENTINES", "EASTER", "BABY", "HOUSEWARMING",
  "MOTHERS_DAY", "FATHERS_DAY", "GRADUATION", "EVERYDAY",
];
export const INQUIRY_STATUSES: InquiryStatus[] = ["NEW", "IN_PROGRESS", "QUOTED", "ACCEPTED", "COMPLETED", "DECLINED"];

export interface Page<T> { items: T[]; page: number; size: number; totalItems: number; totalPages: number }

export interface Me {
  id: string; email: string; displayName: string; preferredLocale: Locale; emailVerified: boolean;
  roles: Role[]; makerSlug: string | null;
}

export interface Category { id: string; slug: string; name: string; productCount: number }
export interface AdminCategory { id: string; slug: string; names: Record<string, string>; position: number; active: boolean }

export interface ProductCard {
  id: string; slug: string; name: string; priceMinor: number | null; currency: string; imageUrl: string | null;
  imageAlt: string; makerSlug: string; makerName: string; makerCity: string | null; available: boolean;
  availabilityType: AvailabilityType; customizable: boolean;
}

export interface ImageRef { id: string; url: string; altText: string; position: number }
export interface ProductOption { id: string; name: string; type: "TEXT" | "SELECT"; choices: string[]; required: boolean }
export interface SellerInfo { legalName: string | null; taxId: string | null; businessAddress: string | null }

export interface PublicProduct {
  id: string; slug: string; name: string; description: string | null; categorySlug: string | null;
  categoryName: string | null; priceMinor: number | null; currency: string; tags: string[]; occasions: Occasion[];
  availabilityType: AvailabilityType; productionDays: number | null; available: boolean; customizable: boolean;
  materials: string | null; dimensions: string | null; shippingInfo: string | null; images: ImageRef[];
  options: ProductOption[];
  maker: { slug: string; businessName: string; logoUrl: string | null; city: string | null;
    acceptsCustomOrders: boolean; seller: SellerInfo | null; hasStorefront: boolean };
  moreFromMaker: ProductCard[]; related: ProductCard[]; publishedAt: string;
}

export interface MakerProduct {
  id: string; slug: string; name: string; description: string | null; categoryId: string | null;
  priceMinor: number | null; currency: string; tags: string[]; occasions: Occasion[];
  availabilityType: AvailabilityType; productionDays: number | null; available: boolean; customizable: boolean;
  materials: string | null; dimensions: string | null; shippingInfo: string | null; showOnStorefront: boolean;
  status: ProductStatus; moderationStatus: "VISIBLE" | "HIDDEN"; moderationReason: string | null;
  images: ImageRef[]; options: ProductOption[]; publishedAt: string | null; updatedAt: string;
}

export interface MakerCard {
  id: string; slug: string; businessName: string; tagline: string | null; city: string | null;
  logoUrl: string | null; coverUrl: string | null; productCount: number;
}

export interface SocialLinks { website: string | null; instagram: string | null; facebook: string | null; tiktok: string | null; pinterest: string | null }

export interface PublicMaker {
  id: string; slug: string; businessName: string; tagline: string | null; bio: string | null; city: string | null;
  region: string | null; countryCode: string; logoUrl: string | null; coverUrl: string | null;
  contactEmail: string | null; phone: string | null; social: SocialLinks; acceptsCustomOrders: boolean;
  primaryCategorySlug: string | null; seller: SellerInfo | null; hasStorefront: boolean; memberSince: string | null;
}

export interface MakerProfile {
  id: string; slug: string; businessName: string; tagline: string | null; bio: string | null; city: string | null;
  region: string | null; countryCode: string; showLocation: boolean; logoUrl: string | null; coverUrl: string | null;
  contactEmail: string | null; phone: string | null; websiteUrl: string | null; instagramUrl: string | null;
  facebookUrl: string | null; tiktokUrl: string | null; pinterestUrl: string | null; acceptsCustomOrders: boolean;
  primaryCategoryId: string | null; legalName: string | null; taxId: string | null; businessAddress: string | null;
  status: MakerStatus; createdAt: string;
}

export type StorefrontSection = "HERO" | "FEATURED_PRODUCTS" | "ABOUT" | "GALLERY" | "CUSTOM_ORDER" | "CONTACT";
export const STOREFRONT_SECTIONS: StorefrontSection[] = ["HERO", "FEATURED_PRODUCTS", "ABOUT", "GALLERY", "CUSTOM_ORDER", "CONTACT"];
export type StorefrontFont = "SERIF" | "SANS" | "ROUNDED";

export interface StorefrontConfig {
  templateKey: string; primaryColor: string; accentColor: string; font: StorefrontFont; heroTitle: string | null;
  heroSubtitle: string | null; heroImageKey: string | null; aboutTitle: string | null; aboutText: string | null;
  featuredHeading: string | null; sections: StorefrontSection[]; language: Locale; seoTitle: string | null;
  seoDescription: string | null;
}

export interface StorefrontTemplate { key: string; name: string; description: string; defaultPrimaryColor: string; defaultAccentColor: string; defaultFont: StorefrontFont }

export interface StorefrontState {
  status: "DRAFT" | "PUBLISHED" | "UNPUBLISHED"; hasUnpublishedChanges: boolean; publishedAt: string | null;
  draft: StorefrontConfig; heroImageUrl: string | null; path: string; makerApproved: boolean; emailVerified: boolean;
  templates: StorefrontTemplate[];
}

export interface StorefrontView {
  maker: PublicMaker; config: StorefrontConfig; heroImageUrl: string | null; products: ProductCard[];
  showPlatformBranding: boolean; preview: boolean;
}

export interface Overview {
  storefrontStatus: string; storefrontHasUnpublishedChanges: boolean; makerStatus: MakerStatus; emailVerified: boolean;
  totalProducts: number; publishedProducts: number; newInquiries: number; openInquiries: number; planCode: string;
  planName: string; maxPublishedProducts: number; maxImagesPerProduct: number;
  recentActivity: { type: string; label: string; refId: string; at: string }[];
}

export interface Plan {
  code: string; name: string; priceMinor: number; currency: string; billingInterval: string;
  maxPublishedProducts: number; maxImagesPerProduct: number; customDomain: boolean; analytics: boolean; reducedBranding: boolean;
}

export interface HistoryEntry { from: InquiryStatus | null; to: InquiryStatus; message: string | null; at: string }

export interface MakerInquirySummary {
  id: string; customerName: string; productName: string | null; status: InquiryStatus; budgetMinor: number | null;
  currency: string; deadline: string | null; createdAt: string;
}

export interface MakerInquiryDetail extends Omit<MakerInquirySummary, "productName"> {
  customerEmail: string; customerPhone: string | null; message: string; internalNotes: string | null;
  productId: string | null; productName: string | null; productSlug: string | null;
  attachments: { id: string; filename: string; contentType: string; sizeBytes: number }[];
  history: HistoryEntry[]; consentAt: string;
}

export interface CustomerInquiry {
  id: string; makerName: string; makerSlug: string; productName: string | null; message: string;
  budgetMinor: number | null; currency: string; deadline: string | null; status: InquiryStatus;
  history: HistoryEntry[]; createdAt: string;
}

export interface Home {
  featuredMakers: MakerCard[]; trendingProducts: ProductCard[]; recentProducts: ProductCard[]; popularCategories: Category[];
}

export interface ApiProblem { status: number; title?: string; detail?: string; code?: string; errors?: Record<string, string> }
