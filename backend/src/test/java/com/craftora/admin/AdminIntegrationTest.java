package com.craftora.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AdminIntegrationTest extends IntegrationTest {

    @Test
    void approvingMakerPublishesThemAndIsAudited() throws Exception {
        Session maker = registerVerifiedMaker("m@example.com", "pending-maker");
        Session admin = createAdmin("admin@example.com");
        String makerId = getJson(maker, "/api/maker/profile").get("id").asString();

        perform(admin, get("/api/admin/makers?status=PENDING")).andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/public/makers/pending-maker")).andExpect(status().isNotFound());
        perform(admin, post("/api/admin/makers/" + makerId + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/public/makers/pending-maker")).andExpect(status().isOk());

        perform(admin, post("/api/admin/makers/" + makerId + "/featured").contentType(MediaType.APPLICATION_JSON)
                .content("{\"featured\":true}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/public/home")).andExpect(jsonPath("$.featuredMakers[0].slug").value("pending-maker"));

        perform(admin, get("/api/admin/audit"))
                .andExpect(jsonPath("$.items[0].action").value("MAKER_FEATURED"))
                .andExpect(jsonPath("$.items[1].action").value("MAKER_APPROVED"))
                .andExpect(jsonPath("$.items[0].actorEmail").value("admin@example.com"));
    }

    @Test
    void adminCanChangePlanWhichLiftsLimits() throws Exception {
        Session maker = registerVerifiedMaker("m@example.com", "plan-maker");
        Session admin = createAdmin("admin@example.com");
        String makerId = getJson(maker, "/api/maker/profile").get("id").asString();
        perform(admin, put("/api/admin/makers/" + makerId + "/plan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"planCode\":\"PRO\"}")).andExpect(status().isNoContent());
        perform(maker, get("/api/maker/subscription")).andExpect(jsonPath("$.plan.code").value("PRO"))
                .andExpect(jsonPath("$.plan.maxPublishedProducts").value(500));
    }

    @Test
    void reportsCanBeFiledAndResolved() throws Exception {
        Session maker = registerVerifiedMaker("m@example.com", "reported-maker");
        approveMaker("reported-maker");
        String productId = createPublishedProduct(maker, "Fake brand bag", 100).get("id").asString();
        mvc.perform(post("/api/public/reports")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"PRODUCT\",\"targetId\":\"" + productId + "\",\"reason\":\"COUNTERFEIT\"}"))
                .andExpect(status().isCreated());
        Session admin = createAdmin("admin@example.com");
        var reports = getJson(admin, "/api/admin/reports");
        String reportId = reports.get("items").get(0).get("id").asString();
        perform(admin, get("/api/admin/reports")).andExpect(jsonPath("$.items[0].targetPath")
                .value("/p/reported-maker/fake-brand-bag"));
        perform(admin, post("/api/admin/reports/" + reportId + "/resolve").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RESOLVED\",\"note\":\"Listing hidden\"}")).andExpect(status().isNoContent());
        perform(admin, get("/api/admin/stats")).andExpect(jsonPath("$.openReports").value(0));
    }

    @Test
    void suspendingAUserRevokesTheirSessions() throws Exception {
        Session customer = register("c@example.com", "CUSTOMER");
        Session admin = createAdmin("admin@example.com");
        String userId = getJson(customer, "/api/me").get("id").asString();
        perform(admin, post("/api/admin/users/" + userId + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isNoContent());
        perform(customer, get("/api/me")).andExpect(status().isUnauthorized());
    }
}
