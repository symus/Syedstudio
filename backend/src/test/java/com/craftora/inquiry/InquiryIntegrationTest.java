package com.craftora.inquiry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.craftora.support.IntegrationTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class InquiryIntegrationTest extends IntegrationTest {

    private MockMultipartFile data(Map<String, Object> overrides) {
        Map<String, Object> m = new HashMap<>(Map.of("makerSlug", "inq-maker", "name", "Kasia",
                "email", "kasia@example.com", "message", "Could you make a set of six mugs?", "budgetMinor", 50000,
                "consent", true));
        m.putAll(overrides);
        return new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, toJson(m).getBytes());
    }

    private String submit(Session as) throws Exception {
        var req = multipart("/api/public/inquiries").file(data(Map.of()))
                .file(new MockMultipartFile("files", "ref.png", "image/png", PNG));
        return body(perform(as, req).andExpect(status().isCreated())).get("id").asString();
    }

    @Test
    void customerSubmitsInquiryAndMakerManagesIt() throws Exception {
        Session maker = registerVerifiedMaker("maker@example.com", "inq-maker");
        approveMaker("inq-maker");
        String id = submit(null);

        assertThat(emailService.outbox()).anyMatch(m -> m.to().equals("maker@example.com"));
        assertThat(emailService.outbox()).anyMatch(m -> m.to().equals("kasia@example.com"));

        perform(maker, get("/api/maker/inquiries")).andExpect(jsonPath("$.items[0].status").value("NEW"));
        var detail = getJson(maker, "/api/maker/inquiries/" + id);
        assertThat(detail.get("attachments").size()).isEqualTo(1);
        String attachmentId = detail.get("attachments").get(0).get("id").asString();
        perform(maker, get("/api/maker/inquiries/" + id + "/attachments/" + attachmentId))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"));

        perform(maker, put("/api/maker/inquiries/" + id + "/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"Customer seems flexible on price\"}")).andExpect(status().isOk());
        perform(maker, post("/api/maker/inquiries/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"QUOTED\",\"message\":\"I can do it for 480 PLN.\"}"))
                .andExpect(jsonPath("$.status").value("QUOTED"))
                .andExpect(jsonPath("$.history.length()").value(2));
        assertThat(emailService.outbox().getFirst().body()).contains("480 PLN").doesNotContain("flexible");
    }

    @Test
    void attachmentsAreNeverServedByThePublicMediaEndpoint() throws Exception {
        registerVerifiedMaker("maker@example.com", "inq-maker");
        approveMaker("inq-maker");
        submit(null);
        String key = jdbc.sql("select storage_key from inquiry_attachment").query(String.class).single();
        assertThat(key).startsWith("private/");
        mvc.perform(get("/api/media/" + key)).andExpect(status().isNotFound());
    }

    @Test
    void consentIsRequired() throws Exception {
        registerVerifiedMaker("maker@example.com", "inq-maker");
        approveMaker("inq-maker");
        mvc.perform(multipart("/api/public/inquiries").file(data(Map.of("consent", false)))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.consent").exists());
    }

    @Test
    void inquiriesToUnapprovedMakersAreRejected() throws Exception {
        registerVerifiedMaker("maker@example.com", "inq-maker");
        mvc.perform(multipart("/api/public/inquiries").file(data(Map.of()))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherMakersAndCustomersCannotSeeInquiryDetails() throws Exception {
        registerVerifiedMaker("maker@example.com", "inq-maker");
        approveMaker("inq-maker");
        Session other = registerVerifiedMaker("other@example.com", "other-maker");
        String id = submit(null);
        perform(other, get("/api/maker/inquiries/" + id)).andExpect(status().isNotFound());
        perform(other, get("/api/maker/inquiries")).andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void loggedInCustomerSeesStatusButNeverInternalNotes() throws Exception {
        Session maker = registerVerifiedMaker("maker@example.com", "inq-maker");
        approveMaker("inq-maker");
        Session customer = register("kasia@example.com", "CUSTOMER");
        String id = submit(customer);
        perform(maker, put("/api/maker/inquiries/" + id + "/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"TOP SECRET NOTE\"}"));
        perform(maker, post("/api/maker/inquiries/" + id + "/reply").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Thanks, working on it\"}")).andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        String body = perform(customer, get("/api/account/inquiries")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].history[1].message").value("Thanks, working on it"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("TOP SECRET").doesNotContain("internalNotes");
    }
}
