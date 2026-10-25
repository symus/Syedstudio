package com.craftora.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.common.RateLimiter;
import com.craftora.mail.EmailMessage;
import com.craftora.mail.EmailService;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Full-stack tests against a real PostgreSQL database (schema from Flyway). Each test starts from empty
 * user-generated tables; reference data (categories, plans, templates) is kept.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    /** Smallest valid PNG (1x1). */
    public static final byte[] PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper json;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected EmailService emailService;

    @Autowired
    private RateLimiter rateLimiter;

    @BeforeEach
    void cleanDatabase() {
        jdbc.sql("""
                truncate audit_log, report, inquiry_status_history, inquiry_attachment, custom_order_inquiry,
                         product_occasion, product_tag, product_option, product_image, product, tag, storefront,
                         subscription, maker_profile, user_token, user_session, user_role, app_user cascade
                """).update();
        rateLimiter.reset();
    }

    // ------------------------------------------------------------------ helpers

    public record Session(Cookie cookie, String email) {}

    protected Session register(String email, String accountType) throws Exception {
        var res = mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email, "password", "password123", "displayName", "Test User",
                                "accountType", accountType, "locale", "en"))))
                .andExpect(status().isCreated()).andReturn();
        return new Session(res.getResponse().getCookie("craftora_session"), email);
    }

    protected Session registerVerifiedMaker(String email, String slug) throws Exception {
        Session s = register(email, "MAKER");
        verifyEmail(email);
        perform(s, put("/api/maker/profile").contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of(
                "slug", slug, "businessName", "Studio " + slug, "city", "Kraków", "showLocation", true,
                "acceptsCustomOrders", true)))).andExpect(status().isOk());
        return s;
    }

    protected Session createAdmin(String email) throws Exception {
        Session s = register(email, "CUSTOMER");
        jdbc.sql("insert into user_role (user_id, role) select id, 'ADMIN' from app_user where email = :e")
                .param("e", email).update();
        return s;
    }

    protected void approveMaker(String slug) {
        jdbc.sql("update maker_profile set status = 'APPROVED', approved_at = now() where slug = :s")
                .param("s", slug).update();
    }

    protected void verifyEmail(String email) throws Exception {
        String token = tokenFromLatestEmail(email, "verify-email");
        mvc.perform(post("/api/auth/verify-email").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("token", token)))).andExpect(status().isNoContent());
    }

    protected String tokenFromLatestEmail(String to, String path) {
        EmailMessage msg = emailService.outbox().stream().filter(m -> m.to().equals(to)).findFirst().orElseThrow();
        Matcher m = Pattern.compile(path + "\\?token=([A-Za-z0-9_-]+)").matcher(msg.body());
        if (!m.find()) {
            throw new AssertionError("No token in email: " + msg.body());
        }
        return m.group(1);
    }

    protected ResultActions perform(Session s,
                                    org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder<?> req)
            throws Exception {
        if (s != null) {
            req.cookie(s.cookie());
        }
        return mvc.perform(req.with(csrf()));
    }

    /** Creates a product with category + image and publishes it; returns its JSON. */
    protected JsonNode createPublishedProduct(Session s, String name, long price) throws Exception {
        JsonNode product = createProduct(s, name, price);
        String id = product.get("id").asString();
        uploadImage(s, id).andExpect(status().isOk());
        return body(perform(s, post("/api/maker/products/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PUBLISHED\"}")).andExpect(status().isOk()));
    }

    protected JsonNode createProduct(Session s, String name, long price) throws Exception {
        String categoryId = jdbc.sql("select id from category where slug = 'ceramics-pottery'")
                .query(String.class).single();
        return body(perform(s, post("/api/maker/products").contentType(MediaType.APPLICATION_JSON).content(toJson(
                Map.of("name", name, "description", "Hand-thrown stoneware", "categoryId", categoryId,
                        "priceMinor", price, "currency", "PLN", "availabilityType", "READY_MADE",
                        "available", true, "customizable", true, "showOnStorefront", true,
                        "tags", java.util.List.of("mug", "stoneware")))))
                .andExpect(status().isCreated()));
    }

    protected ResultActions uploadImage(Session s, String productId) throws Exception {
        return perform(s, multipart("/api/maker/products/" + productId + "/images")
                .file(new MockMultipartFile("file", "photo.png", "image/png", PNG)));
    }

    protected JsonNode getJson(Session s, String url) throws Exception {
        return body(perform(s, get(url)).andExpect(status().isOk()));
    }

    protected JsonNode body(ResultActions actions) throws Exception {
        return json.readTree(actions.andReturn().getResponse().getContentAsString());
    }

    protected String toJson(Object o) {
        return json.writeValueAsString(o);
    }
}
