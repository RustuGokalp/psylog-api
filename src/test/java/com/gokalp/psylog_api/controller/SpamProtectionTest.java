package com.gokalp.psylog_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.service.CommentService;
import com.gokalp.psylog_api.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// HTTP-level tests for the spam protection on the two public endpoints that send mail.
// Configured limits (test + default profile): contact 3/hour, comment 5/hour.
// Every test uses its own client IP so the shared in-memory limiter cannot leak state.
// The IP is sent through CF-Connecting-IP — the header production actually relies on.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpamProtectionTest {

    @Autowired
    private WebApplicationContext wac;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private CommentService commentService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String contactBody(String website) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fullName", "Jane Doe");
        body.put("email", "jane@example.com");
        body.put("subject", "Randevu");
        body.put("message", "Merhaba, bilgi almak istiyorum.");
        if (website != null) {
            body.put("website", website);
        }
        return objectMapper.writeValueAsString(body);
    }

    private String commentBody(String website) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("author", "Jane Doe");
        body.put("email", "jane@example.com");
        body.put("content", "Harika bir yazı.");
        if (website != null) {
            body.put("website", website);
        }
        return objectMapper.writeValueAsString(body);
    }

    private void postContact(String ip, String website, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/contact")
                        .header("CF-Connecting-IP", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody(website)))
                .andExpect(status().is(expectedStatus));
    }

    // ─── Honeypot ───────────────────────────────────────────────────────────

    // An empty honeypot is the normal path — the message must be saved.
    @Test
    void contact_withEmptyHoneypot_isProcessedNormally() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .header("CF-Connecting-IP", "10.10.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody("")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message received"));

        verify(contactService).save(any());
    }

    // A filled honeypot must not be saved, but the bot still sees the success response.
    @Test
    void contact_withFilledHoneypot_isDroppedButLooksSuccessful() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .header("CF-Connecting-IP", "10.10.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody("http://spam.example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message received"));

        verifyNoInteractions(contactService);
    }

    @Test
    void comment_withEmptyHoneypot_isProcessedNormally() throws Exception {
        when(commentService.submitComment(anyLong(), any())).thenReturn(new CommentPublicResponse(
                1L, "Jane Doe", "jane@example.com", "Harika bir yazı.", LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(post("/api/posts/1/comments")
                        .header("CF-Connecting-IP", "10.10.0.3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(commentService).submitComment(eq(1L), any());
    }

    // Same contract shape (201 + comment body), but nothing is persisted.
    @Test
    void comment_withFilledHoneypot_isDroppedButLooksSuccessful() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .header("CF-Connecting-IP", "10.10.0.4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody("http://spam.example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Jane Doe"))
                .andExpect(jsonPath("$.content").value("Harika bir yazı."))
                .andExpect(jsonPath("$.id").doesNotExist());

        verifyNoInteractions(commentService);
    }

    // ─── Rate limiting ──────────────────────────────────────────────────────

    // Contact allows 3 requests per hour; the fourth one is rejected with 429.
    @Test
    void contact_overTheHourlyLimit_returns429WithErrorContract() throws Exception {
        String ip = "10.20.0.1";
        for (int i = 0; i < 3; i++) {
            postContact(ip, null, 200);
        }

        mockMvc.perform(post("/api/contact")
                        .header("CF-Connecting-IP", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody(null)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message")
                        .value("Çok fazla istek gönderdiniz. Lütfen bir süre sonra tekrar deneyin."))
                .andExpect(jsonPath("$.path").value("/api/contact"))
                .andExpect(jsonPath("$.timestamp").exists());

        // The blocked request never reaches the service, so no mail is triggered.
        verify(contactService, times(3)).save(any());
    }

    // The limiter keys on the forwarded client IP, not on the proxy address —
    // one blocked visitor must not block everybody else.
    @Test
    void contact_limitIsPerClientIp() throws Exception {
        String blockedIp = "10.20.0.2";
        for (int i = 0; i < 3; i++) {
            postContact(blockedIp, null, 200);
        }
        postContact(blockedIp, null, 429);

        // Same proxy, different original client — must still be allowed.
        postContact("10.20.0.3", null, 200);
    }

    // A bot rotating a fake X-Forwarded-For on every request must not get a fresh bucket:
    // the unspoofable Cloudflare header decides which bucket the request lands in.
    @Test
    void contact_spoofedForwardedForCannotBypassTheLimit() throws Exception {
        String realIp = "10.20.0.4";
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/contact")
                            .header("CF-Connecting-IP", realIp)
                            .header("X-Forwarded-For", "1.2.3." + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(contactBody(null)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/contact")
                        .header("CF-Connecting-IP", realIp)
                        .header("X-Forwarded-For", "9.9.9.9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody(null)))
                .andExpect(status().isTooManyRequests());
    }

    // Comments have their own budget (5/hour) and their own bucket.
    @Test
    void comment_overTheHourlyLimit_returns429() throws Exception {
        when(commentService.submitComment(anyLong(), any())).thenReturn(new CommentPublicResponse(
                1L, "Jane Doe", "jane@example.com", "Harika bir yazı.", LocalDateTime.of(2026, 5, 1, 10, 0)));

        String ip = "10.30.0.1";
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/posts/1/comments")
                            .header("CF-Connecting-IP", ip)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(commentBody(null)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/posts/1/comments")
                        .header("CF-Connecting-IP", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(null)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.path").value("/api/posts/1/comments"));

        verify(commentService, times(5)).submitComment(anyLong(), any());
    }
}
