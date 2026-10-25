package com.craftora.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void slugifiesPolishText() {
        assertThat(Slugs.slugify("Łódzka Świeca Zapachowa!", 60)).isEqualTo("lodzka-swieca-zapachowa");
        assertThat(Slugs.slugify("  --Kubek  ceramiczny-- ", 60)).isEqualTo("kubek-ceramiczny");
        assertThat(Slugs.slugify("abc def ghi", 6)).isEqualTo("abc-de");
    }

    @Test
    void rejectsReservedAndMalformedStoreSlugs() {
        assertThat(Slugs.isValidStoreSlug("anna-ceramics")).isTrue();
        assertThat(Slugs.isValidStoreSlug("admin")).isFalse();
        assertThat(Slugs.isValidStoreSlug("dashboard")).isFalse();
        assertThat(Slugs.isValidStoreSlug("ab")).isFalse();
        assertThat(Slugs.isValidStoreSlug("-bad")).isFalse();
        assertThat(Slugs.isValidStoreSlug("Upper")).isFalse();
    }
}
