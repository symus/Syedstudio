package com.craftora.storefront;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.node.ObjectNode;

class StorefrontIntegrationTest extends IntegrationTest {

    @Test
    void profileCreationProvisionsDraftStorefront() throws Exception {
        Session s = registerVerifiedMaker("s1@example.com", "atelier-one");
        perform(s, get("/api/maker/storefront"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.draft.templateKey").value("atelier"))
                .andExpect(jsonPath("$.path").value("/atelier-one"))
                .andExpect(jsonPath("$.templates.length()").value(3));
        mvc.perform(get("/api/public/storefronts/atelier-one")).andExpect(status().isNotFound());
    }

    @Test
    void publishSnapshotsDraftAndLaterEditsStayPrivateUntilRepublished() throws Exception {
        Session s = registerVerifiedMaker("s2@example.com", "atelier-two");
        approveMaker("atelier-two");
        ObjectNode draft = (ObjectNode) getJson(s, "/api/maker/storefront").get("draft");
        draft.put("heroTitle", "Version one");
        perform(s, put("/api/maker/storefront").contentType(MediaType.APPLICATION_JSON).content(toJson(draft)))
                .andExpect(jsonPath("$.hasUnpublishedChanges").value(true));
        perform(s, post("/api/maker/storefront/publish")).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/api/public/storefronts/atelier-two"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.heroTitle").value("Version one"))
                .andExpect(jsonPath("$.showPlatformBranding").value(true));

        draft.put("heroTitle", "Version two");
        perform(s, put("/api/maker/storefront").contentType(MediaType.APPLICATION_JSON).content(toJson(draft)))
                .andExpect(jsonPath("$.hasUnpublishedChanges").value(true));
        mvc.perform(get("/api/public/storefronts/atelier-two")).andExpect(jsonPath("$.config.heroTitle").value("Version one"));
        perform(s, get("/api/maker/storefront/preview")).andExpect(jsonPath("$.config.heroTitle").value("Version two"))
                .andExpect(jsonPath("$.preview").value(true));

        perform(s, post("/api/maker/storefront/unpublish")).andExpect(jsonPath("$.status").value("UNPUBLISHED"));
        mvc.perform(get("/api/public/storefronts/atelier-two")).andExpect(status().isNotFound());
    }

    @Test
    void publishingRequiresVerifiedEmail() throws Exception {
        Session s = register("s3@example.com", "MAKER");
        perform(s, put("/api/maker/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"slug\":\"atelier-three\",\"businessName\":\"Three\",\"showLocation\":true}"))
                .andExpect(status().isOk());
        perform(s, post("/api/maker/storefront/publish"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("email_not_verified"));
    }

    @Test
    void storefrontShowsOnlySelectedPublishedProducts() throws Exception {
        Session s = registerVerifiedMaker("s4@example.com", "atelier-four");
        approveMaker("atelier-four");
        createPublishedProduct(s, "Shown", 100);
        createProduct(s, "Draft only", 100);
        perform(s, post("/api/maker/storefront/publish")).andExpect(status().isOk());
        mvc.perform(get("/api/public/storefronts/atelier-four"))
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Shown"));
    }

    @Test
    void clientCannotInjectArbitraryHeroImageKeyOrTemplate() throws Exception {
        Session s = registerVerifiedMaker("s5@example.com", "atelier-five");
        ObjectNode draft = (ObjectNode) getJson(s, "/api/maker/storefront").get("draft");
        draft.put("heroImageKey", "private/inquiries/x/y.png");
        perform(s, put("/api/maker/storefront").contentType(MediaType.APPLICATION_JSON).content(toJson(draft)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_hero_image"));
        draft.putNull("heroImageKey");
        draft.put("templateKey", "nope");
        perform(s, put("/api/maker/storefront").contentType(MediaType.APPLICATION_JSON).content(toJson(draft)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("unknown_template"));
        draft.put("templateKey", "studio");
        draft.put("primaryColor", "red; background:url(x)");
        perform(s, put("/api/maker/storefront").contentType(MediaType.APPLICATION_JSON).content(toJson(draft)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.primaryColor").exists());
    }

    @Test
    void reservedAndDuplicateSlugsAreRejected() throws Exception {
        registerVerifiedMaker("s6@example.com", "taken-slug");
        Session other = register("s7@example.com", "MAKER");
        perform(other, put("/api/maker/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"slug\":\"admin\",\"businessName\":\"X\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("slug_reserved"));
        perform(other, put("/api/maker/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"slug\":\"taken-slug\",\"businessName\":\"X\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("slug_taken"));
        perform(other, get("/api/maker/slug-available?slug=taken-slug")).andExpect(jsonPath("$.available").value(false));
        perform(other, get("/api/maker/slug-available?slug=fresh-slug")).andExpect(jsonPath("$.available").value(true));
    }
}
