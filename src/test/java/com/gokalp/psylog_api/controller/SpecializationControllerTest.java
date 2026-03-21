package com.gokalp.psylog_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokalp.psylog_api.dto.request.SpecializationRequest;
import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.security.JwtUtil;
import com.gokalp.psylog_api.service.SpecializationService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TC-01 through TC-13: Full-stack HTTP integration tests for Specialization endpoints
// Uses H2 in-memory DB (src/test/resources/application.properties), full Spring Security stack
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpecializationControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private SpecializationService specializationService;

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

    private SpecializationResponse sampleResponse(Long id, String title, Integer displayOrder) {
        return new SpecializationResponse(
                id, title, "Açıklama", "https://img.com/" + id + ".jpg",
                displayOrder, LocalDateTime.of(2026, 3, 22, 10, 0), LocalDateTime.of(2026, 3, 22, 10, 0));
    }

    private SpecializationRequest validRequest() {
        SpecializationRequest req = new SpecializationRequest();
        req.setTitle("Anksiyete Bozuklukları");
        req.setDescription("Kapsamlı bir açıklama metni");
        req.setImage("https://img.com/1.jpg");
        req.setDisplayOrder(1);
        return req;
    }

    // ─── GET /api/specializations (public) ──────────────────────────────────

    // TC-01: public GET → no auth required → 200
    @Test
    void getAll_publicEndpoint_returns200WithoutToken() throws Exception {
        when(specializationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/specializations"))
                .andExpect(status().isOk());
    }

    // TC-02: public GET → returns list from service
    @Test
    void getAll_returnsListFromService() throws Exception {
        List<SpecializationResponse> list = List.of(
                sampleResponse(1L, "Anksiyete", 1),
                sampleResponse(2L, "Depresyon", 2)
        );
        when(specializationService.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/specializations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Anksiyete"))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // ─── GET /api/admin/specializations ────────────────────────────────────

    // TC-03: admin GET with valid token → 200
    @Test
    void adminGetAll_withValidToken_returns200() throws Exception {
        when(specializationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/specializations")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isOk());
    }

    // TC-04: admin GET without token → 401
    @Test
    void adminGetAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/specializations"))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /api/admin/specializations ────────────────────────────────────

    // TC-05: valid request with token → 200, response body contains all fields
    @Test
    void adminCreate_withValidRequest_returns200() throws Exception {
        SpecializationRequest req = validRequest();
        SpecializationResponse res = sampleResponse(1L, req.getTitle(), req.getDisplayOrder());
        when(specializationService.create(any())).thenReturn(res);

        mockMvc.perform(post("/api/admin/specializations")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Anksiyete Bozuklukları"))
                .andExpect(jsonPath("$.displayOrder").value(1));
    }

    // TC-06: POST without token → 401
    @Test
    void adminCreate_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/specializations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    // TC-07: POST with blank title → @NotBlank fails → 400
    @Test
    void adminCreate_withBlankTitle_returns400() throws Exception {
        SpecializationRequest req = validRequest();
        req.setTitle("   ");

        mockMvc.perform(post("/api/admin/specializations")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // TC-08: POST with missing description → @NotBlank fails → 400
    @Test
    void adminCreate_withMissingDescription_returns400() throws Exception {
        SpecializationRequest req = validRequest();
        req.setDescription(null);

        mockMvc.perform(post("/api/admin/specializations")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // TC-09: POST with invalid image URL → @URL fails → 400
    @Test
    void adminCreate_withInvalidImageUrl_returns400() throws Exception {
        SpecializationRequest req = validRequest();
        req.setImage("not-a-url");

        mockMvc.perform(post("/api/admin/specializations")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /api/admin/specializations/{id} ────────────────────────────────

    // TC-10: valid request with token → 200
    @Test
    void adminUpdate_withValidRequest_returns200() throws Exception {
        SpecializationResponse res = sampleResponse(1L, "Updated", 2);
        when(specializationService.update(eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/admin/specializations/1")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // TC-11: PUT with non-existent id → service throws ResourceNotFoundException → 404
    @Test
    void adminUpdate_nonExistentId_returns404() throws Exception {
        when(specializationService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Specialization not found with id: 99"));

        mockMvc.perform(put("/api/admin/specializations/99")
                        .cookie(new Cookie("token", adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    // TC-12: PUT without token → 401
    @Test
    void adminUpdate_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/admin/specializations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE /api/admin/specializations/{id} ─────────────────────────────

    // TC-13: valid token → 200, success response body
    @Test
    void adminDelete_withValidToken_returns200() throws Exception {
        mockMvc.perform(delete("/api/admin/specializations/1")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Specialization deleted"));
    }

    // TC-14: DELETE non-existent id → service throws ResourceNotFoundException → 404
    @Test
    void adminDelete_nonExistentId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Specialization not found with id: 99"))
                .when(specializationService).delete(99L);

        mockMvc.perform(delete("/api/admin/specializations/99")
                        .cookie(new Cookie("token", adminToken())))
                .andExpect(status().isNotFound());
    }

    // TC-15: DELETE without token → 401
    @Test
    void adminDelete_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/admin/specializations/1"))
                .andExpect(status().isUnauthorized());
    }
}
