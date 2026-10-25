package com.craftora.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    void registerLogsInAndNormalisesEmail() throws Exception {
        Session s = register("Maker@Example.COM", "MAKER");
        perform(s, get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maker@example.com"))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.emailVerified").value(false));
        String hash = jdbc.sql("select password_hash from app_user").query(String.class).single();
        assertThat(hash).startsWith("{bcrypt}").doesNotContain("password123");
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        register("a@example.com", "CUSTOMER");
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "A@example.com", "password", "password123",
                                "displayName", "X", "accountType", "CUSTOMER"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email_taken"));
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\",\"displayName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginWithWrongPasswordFailsWithGenericMessage() throws Exception {
        register("b@example.com", "CUSTOMER");
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"b@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void logoutRevokesTheSession() throws Exception {
        Session s = register("c@example.com", "CUSTOMER");
        perform(s, post("/api/auth/logout")).andExpect(status().isNoContent());
        perform(s, get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
        Session s = register("d@example.com", "CUSTOMER");
        mvc.perform(post("/api/auth/logout").cookie(s.cookie())).andExpect(status().isForbidden());
    }

    @Test
    void emailVerificationTokenIsSingleUse() throws Exception {
        register("e@example.com", "CUSTOMER");
        String token = tokenFromLatestEmail("e@example.com", "verify-email");
        mvc.perform(post("/api/auth/verify-email").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("token", token)))).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/verify-email").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("token", token))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_token"));
        assertThat(jdbc.sql("select email_verified_at is not null from app_user").query(Boolean.class).single()).isTrue();
    }

    @Test
    void passwordResetChangesPasswordAndRevokesSessions() throws Exception {
        Session s = register("f@example.com", "CUSTOMER");
        mvc.perform(post("/api/auth/forgot-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"f@example.com\"}")).andExpect(status().isAccepted());
        // unknown e-mails get the same response (no account enumeration)
        mvc.perform(post("/api/auth/forgot-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@example.com\"}")).andExpect(status().isAccepted());
        String token = tokenFromLatestEmail("f@example.com", "reset-password");
        mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("token", token, "password", "brand-new-pass")))).andExpect(status().isNoContent());

        perform(s, get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"f@example.com\",\"password\":\"brand-new-pass\"}")).andExpect(status().isOk());
    }

    @Test
    void rolesAreEnforcedServerSide() throws Exception {
        Session customer = register("g@example.com", "CUSTOMER");
        Session maker = register("h@example.com", "MAKER");
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        perform(customer, get("/api/maker/profile")).andExpect(status().isForbidden());
        perform(maker, get("/api/admin/stats")).andExpect(status().isForbidden());
        Session admin = createAdmin("admin@example.com");
        perform(admin, get("/api/admin/stats")).andExpect(status().isOk());
    }

    @Test
    void suspendedUsersCannotUseExistingSessions() throws Exception {
        Session s = register("i@example.com", "CUSTOMER");
        jdbc.sql("update app_user set status = 'SUSPENDED'").update();
        perform(s, get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"i@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("account_suspended"));
    }
}
