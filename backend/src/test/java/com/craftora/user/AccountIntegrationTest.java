package com.craftora.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AccountIntegrationTest extends IntegrationTest {

    @Test
    void exportContainsOwnDataOnly() throws Exception {
        Session s = registerVerifiedMaker("me@example.com", "export-maker");
        createProduct(s, "My product", 100);
        registerVerifiedMaker("someone@example.com", "someone-else");
        String body = perform(s, get("/api/me/export")).andExpect(status().isOk())
                .andExpect(jsonPath("$.account.email").value("me@example.com"))
                .andExpect(jsonPath("$.products.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("someone@example.com").doesNotContain("password");
    }

    @Test
    void deletingAccountRemovesOwnedData() throws Exception {
        Session s = registerVerifiedMaker("bye@example.com", "bye-maker");
        createPublishedProduct(s, "Last product", 100);
        perform(s, delete("/api/me").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
        perform(s, delete("/api/me").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"password123\"}"))
                .andExpect(status().isNoContent());
        assertThat(jdbc.sql("select count(*) from app_user").query(Long.class).single()).isZero();
        assertThat(jdbc.sql("select count(*) from product").query(Long.class).single()).isZero();
        assertThat(jdbc.sql("select count(*) from maker_profile").query(Long.class).single()).isZero();
    }

    @Test
    void customerCanBecomeMaker() throws Exception {
        Session s = register("cust@example.com", "CUSTOMER");
        perform(s, get("/api/maker/profile")).andExpect(status().isForbidden());
        perform(s, post("/api/me/become-maker")).andExpect(jsonPath("$.roles.length()").value(2));
        perform(s, get("/api/maker/profile")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("maker_profile_missing"));
    }
}
