package com.craftora.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class ProductIntegrationTest extends IntegrationTest {

    @Test
    void publishingRequiresImageAndCategory() throws Exception {
        Session s = registerVerifiedMaker("m1@example.com", "studio-one");
        String id = createProduct(s, "Blue mug", 8900).get("id").asString();
        perform(s, post("/api/maker/products/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("image_required"));
        uploadImage(s, id).andExpect(status().isOk()).andExpect(jsonPath("$.images.length()").value(1));
        perform(s, post("/api/maker/products/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void publishedProductsAppearInMarketplaceOnlyOnceMakerIsApproved() throws Exception {
        Session s = registerVerifiedMaker("m2@example.com", "studio-two");
        createPublishedProduct(s, "Speckled stoneware bowl", 12000);
        mvc.perform(get("/api/public/products?q=stoneware")).andExpect(jsonPath("$.totalItems").value(0));

        approveMaker("studio-two");
        mvc.perform(get("/api/public/products?q=stoneware"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].makerSlug").value("studio-two"))
                .andExpect(jsonPath("$.items[0].imageUrl").exists());
        // prefix, tag, maker-name and filter matching
        mvc.perform(get("/api/public/products?q=speck")).andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/public/products?q=mug")).andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/public/products").param("category", "ceramics-pottery").param("city", "kraków")
                        .param("maxPrice", "15000").param("sort", "price_asc"))
                .andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/public/products?maxPrice=5000")).andExpect(jsonPath("$.totalItems").value(0));
        mvc.perform(get("/api/public/products/studio-two/speckled-stoneware-bowl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maker.slug").value("studio-two"))
                .andExpect(jsonPath("$.tags.length()").value(2));
    }

    @Test
    void draftProductsAreNeverPublic() throws Exception {
        Session s = registerVerifiedMaker("m3@example.com", "studio-three");
        approveMaker("studio-three");
        createProduct(s, "Secret draft", 1000);
        mvc.perform(get("/api/public/products")).andExpect(jsonPath("$.totalItems").value(0));
        mvc.perform(get("/api/public/products/studio-three/secret-draft")).andExpect(status().isNotFound());
    }

    @Test
    void freePlanLimitsPublishedProducts() throws Exception {
        Session s = registerVerifiedMaker("m4@example.com", "studio-four");
        jdbc.sql("update subscription_plan set max_published_products = 2 where code = 'FREE'").update();
        try {
            createPublishedProduct(s, "One", 100);
            createPublishedProduct(s, "Two", 100);
            String id = createProduct(s, "Three", 100).get("id").asString();
            uploadImage(s, id).andExpect(status().isOk());
            perform(s, post("/api/maker/products/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"PUBLISHED\"}"))
                    .andExpect(status().isPaymentRequired())
                    .andExpect(jsonPath("$.code").value("plan_limit_products"));
        } finally {
            jdbc.sql("update subscription_plan set max_published_products = 10 where code = 'FREE'").update();
        }
    }

    @Test
    void uploadsAreValidatedBySignatureNotExtension() throws Exception {
        Session s = registerVerifiedMaker("m5@example.com", "studio-five");
        String id = createProduct(s, "Vase", 100).get("id").asString();
        perform(s, multipart("/api/maker/products/" + id + "/images").file(new MockMultipartFile("file", "evil.png",
                        "image/png", "<script>alert(1)</script>".getBytes())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("unsupported_file_type"));
    }

    @Test
    void imagesCanBeReorderedAndServedPublicly() throws Exception {
        Session s = registerVerifiedMaker("m6@example.com", "studio-six");
        String id = createProduct(s, "Plate", 100).get("id").asString();
        uploadImage(s, id);
        var product = body(uploadImage(s, id));
        String first = product.get("images").get(0).get("id").asString();
        String second = product.get("images").get(1).get("id").asString();
        perform(s, put("/api/maker/products/" + id + "/images/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageIds\":[\"" + second + "\",\"" + first + "\"]}"))
                .andExpect(jsonPath("$.images[0].id").value(second));
        String url = product.get("images").get(0).get("url").asString();
        mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType("image/png"));
    }

    @Test
    void makersCannotTouchEachOthersProducts() throws Exception {
        Session a = registerVerifiedMaker("a@example.com", "studio-a");
        Session b = registerVerifiedMaker("b@example.com", "studio-b");
        String id = createProduct(a, "A's mug", 100).get("id").asString();

        perform(b, get("/api/maker/products/" + id)).andExpect(status().isNotFound());
        perform(b, put("/api/maker/products/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"hijack\",\"availabilityType\":\"READY_MADE\"}")).andExpect(status().isNotFound());
        perform(b, post("/api/maker/products/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ARCHIVED\"}")).andExpect(status().isNotFound());
        uploadImage(b, id).andExpect(status().isNotFound());
        perform(b, delete("/api/maker/products/" + id)).andExpect(status().isNotFound());
        perform(b, get("/api/maker/products")).andExpect(jsonPath("$.totalItems").value(0));
        perform(a, get("/api/maker/products/" + id)).andExpect(jsonPath("$.name").value("A's mug"));
    }

    @Test
    void adminModerationHidesProductFromMarketplace() throws Exception {
        Session s = registerVerifiedMaker("m7@example.com", "studio-seven");
        approveMaker("studio-seven");
        String id = createPublishedProduct(s, "Questionable item", 100).get("id").asString();
        Session admin = createAdmin("admin@example.com");
        perform(admin, post("/api/admin/products/" + id + "/moderation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"HIDDEN\",\"reason\":\"Prohibited\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/public/products")).andExpect(jsonPath("$.totalItems").value(0));
        perform(s, get("/api/maker/products/" + id)).andExpect(jsonPath("$.moderationReason").value("Prohibited"));
        mvc.perform(get("/api/public/products")).andExpect(status().isOk());
    }
}
