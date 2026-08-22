package com.gokalp.psylog_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokalp.psylog_api.dto.request.PageHeroRequest;
import com.gokalp.psylog_api.dto.response.PageHeroResponse;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.security.JwtUtil;
import com.gokalp.psylog_api.service.PageHeroService;
import jakarta.servlet.http.Cookie;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Full-stack HTTP integration tests for PageHero endpoints
// Uses H2 in-memory DB (src/test/resources/application.properties), full Spring Security stack
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PageHeroControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private PageHeroService pageHeroService;

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

    private String adminToken() {
        User admin = new User("admin@test.com", "pass", Role.ADMIN);
        return jwtUtil.generateToken(admin);
    }

    private PageHeroResponse sampleResponse(Long id, PageKey pageKey) {
        return new PageHeroResponse(
                id, pageKey, "Hakkımda", "Klinik Psikolog Tuğçe Tekin", "Kısa bir açıklama.",
                LocalDateTime.of(2026, 8, 23, 10, 0),
                LocalDateTime.of(2026, 8, 23, 10, 0));
    }

    private PageHeroRequest validRequest() {
        PageHeroRequest req = new PageHeroRequest();
        req.setSubtitle("Hakkımda");
        req.setTitle("Klinik Psikolog Tuğçe Tekin");
        req.setDescription("Kısa bir açıklama.");
        return req;
    }

    // ─── GET /api/page-heroes (public) ──────────────────────────────────────

    @Test
    void getAll_publicEndpoint_returns200WithoutToken() throws Exception {
        when(pageHeroService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/page-heroes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_returnsListFromService() throws Exception {
        when(pageHeroService.getAll()).thenReturn(List.of(
                sampleResponse(1L, PageKey.ABOUT),
                sampleResponse(2L, PageKey.POSTS)));

        mockMvc.perform(get("/api/page-heroes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pageKey").value("ABOUT"))
                .andExpect(jsonPath("$[0].subtitle").value("Hakkımda"))
                .andExpect(jsonPath("$[1].pageKey").value("POSTS"));
    }

    // ─── GET /api/page-heroes/{pageKey} (public) ────────────────────────────

    @Test
    void getByPageKey_existingKey_returns200() throws Exception {
        when(pageHeroService.getByPageKey(PageKey.ABOUT)).thenReturn(sampleResponse(1L, PageKey.ABOUT));

        mockMvc.perform(get("/api/page-heroes/ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pageKey").value("ABOUT"))
                .andExpect(jsonPath("$.title").value("Klinik Psikolog Tuğçe Tekin"))
                .andExpect(jsonPath("$.description").value("Kısa bir açıklama."));
    }

    @Test
    void getByPageKey_missingRecord_returns404() throws Exception {
        when(pageHeroService.getByPageKey(PageKey.CONTACT))
                .thenThrow(new ResourceNotFoundException("Page hero not found: CONTACT"));

        mockMvc.perform(get("/api/page-heroes/CONTACT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPageKey_unknownKey_returns400() throws Exception {
        mockMvc.perform(get("/api/page-heroes/UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── GET /api/admin/page-heroes ─────────────────────────────────────────

    @Test
    void adminGetAll_withValidToken_returns200() throws Exception {
        when(pageHeroService.getAll()).thenReturn(List.of(sampleResponse(1L, PageKey.ABOUT)));

        mockMvc.perform(get("/api/admin/page-heroes")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void adminGetAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/page-heroes"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/admin/page-heroes/{pageKey} ───────────────────────────────

    @Test
    void adminGetByPageKey_withValidToken_returns200() throws Exception {
        when(pageHeroService.getByPageKey(PageKey.SPECIALIZATIONS))
                .thenReturn(sampleResponse(4L, PageKey.SPECIALIZATIONS));

        mockMvc.perform(get("/api/admin/page-heroes/SPECIALIZATIONS")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageKey").value("SPECIALIZATIONS"));
    }

    @Test
    void adminGetByPageKey_missingRecord_returns404() throws Exception {
        when(pageHeroService.getByPageKey(PageKey.POSTS))
                .thenThrow(new ResourceNotFoundException("Page hero not found: POSTS"));

        mockMvc.perform(get("/api/admin/page-heroes/POSTS")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminGetByPageKey_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/page-heroes/ABOUT"))
                .andExpect(status().isUnauthorized());
    }

    // ─── PUT /api/admin/page-heroes/{pageKey} ───────────────────────────────

    @Test
    void adminUpdate_withValidRequest_returns200() throws Exception {
        when(pageHeroService.update(eq(PageKey.ABOUT), any())).thenReturn(sampleResponse(1L, PageKey.ABOUT));

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageKey").value("ABOUT"))
                .andExpect(jsonPath("$.title").value("Klinik Psikolog Tuğçe Tekin"));
    }

    @Test
    void adminUpdate_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUpdate_missingRecord_returns404() throws Exception {
        when(pageHeroService.update(eq(PageKey.CONTACT), any()))
                .thenThrow(new ResourceNotFoundException("Page hero not found: CONTACT"));

        mockMvc.perform(put("/api/admin/page-heroes/CONTACT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminUpdate_unknownPageKey_returns400() throws Exception {
        mockMvc.perform(put("/api/admin/page-heroes/UNKNOWN")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withBlankSubtitle_returns400() throws Exception {
        PageHeroRequest req = validRequest();
        req.setSubtitle("   ");

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withMissingTitle_returns400() throws Exception {
        PageHeroRequest req = validRequest();
        req.setTitle(null);

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withTooLongSubtitle_returns400() throws Exception {
        PageHeroRequest req = validRequest();
        req.setSubtitle("a".repeat(61));

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withTooLongTitle_returns400() throws Exception {
        PageHeroRequest req = validRequest();
        req.setTitle("a".repeat(121));

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withTooLongDescription_returns400() throws Exception {
        PageHeroRequest req = validRequest();
        req.setDescription("a".repeat(401));

        mockMvc.perform(put("/api/admin/page-heroes/ABOUT")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdate_withNullDescription_returns200() throws Exception {
        PageHeroRequest req = validRequest();
        req.setDescription(null);
        when(pageHeroService.update(eq(PageKey.POSTS), any())).thenReturn(sampleResponse(2L, PageKey.POSTS));

        mockMvc.perform(put("/api/admin/page-heroes/POSTS")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
