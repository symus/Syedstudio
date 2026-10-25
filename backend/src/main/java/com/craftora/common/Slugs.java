package com.craftora.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** URL slug helpers. Handles Polish diacritics (including ł, which NFD does not decompose). */
public final class Slugs {

    public static final Pattern VALID = Pattern.compile("^[a-z0-9](?:[a-z0-9-]{1,58}[a-z0-9])$");

    /** Top-level paths owned by the platform; maker storefront slugs live at the root and must not collide. */
    public static final Set<String> RESERVED = Set.of(
            "about", "account", "admin", "api", "auth", "blog", "categories", "category", "contact", "dashboard",
            "docs", "faq", "forgot-password", "help", "home", "legal", "login", "logout", "makers", "marketplace",
            "media", "p", "preview", "pricing", "privacy", "products", "register", "reset-password", "robots",
            "search", "settings", "shop", "sitemap", "static", "support", "terms", "verify-email", "www",
            "craftora", "_next");

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    private Slugs() {}

    public static String slugify(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        String s = input.replace('ł', 'l').replace('Ł', 'L');
        s = MARKS.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
        s = NON_ALNUM.matcher(s.toLowerCase(Locale.ROOT)).replaceAll("-");
        s = s.replaceAll("^-+|-+$", "");
        if (s.length() > maxLength) {
            s = s.substring(0, maxLength).replaceAll("-+$", "");
        }
        return s;
    }

    public static boolean isValidStoreSlug(String slug) {
        return slug != null && VALID.matcher(slug).matches() && !RESERVED.contains(slug);
    }
}
