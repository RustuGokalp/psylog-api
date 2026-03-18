package com.gokalp.psylog_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gokalp.psylog_api.dto.request.LoginRequest;
import com.gokalp.psylog_api.dto.response.AuthResponse;
import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import com.gokalp.psylog_api.security.JwtUtil;
import com.gokalp.psylog_api.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TC-01 through TC-09: Full-stack HTTP integration tests
// Uses H2 in-memory DB (src/test/resources/application.properties), full Spring Security stack
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthControllerTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha-256";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Replace real AuthService — we test the controller + security layer, not the service
    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ─── Login ──────────────────────────────────────────────────────────────

    // TC-01: valid credentials → 200 with correct response shape
    @Test
    void login_withValidCredentials_returns200WithCorrectShape() throws Exception {
        User user = new User("admin@test.com", "pass", Role.ADMIN);
        String token = jwtUtil.generateToken(user);
        AuthResponse mockResponse = new AuthResponse(
                token, 3_600_000L,
                new AuthResponse.UserInfo(1L, "admin@test.com", "ADMIN"));
        when(authService.login(any())).thenReturn(mockResponse);

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3_600_000))
                .andExpect(jsonPath("$.user.email").value("admin@test.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    // TC-02: invalid credentials → service throws BadCredentialsException → GlobalExceptionHandler returns 401
    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // TC-03: missing email → @Valid fails → 400
    @Test
    void login_withMissingEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // TC-04: missing password → @Valid fails → 400
    @Test
    void login_withMissingPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─── Validate ───────────────────────────────────────────────────────────

    // TC-05: valid token → JwtAuthFilter sets auth → 200 with { isValid: true }
    @Test
    void validate_withValidToken_returns200WithIsValidTrue() throws Exception {
        User user = new User("admin@test.com", "pass", Role.ADMIN);
        String token = jwtUtil.generateToken(user);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    // TC-06: no Authorization header → no authentication set → Spring Security returns 401
    @Test
    void validate_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    // TC-07: expired token → JwtAuthFilter catches JwtException → no auth set → 401
    @Test
    void validate_withExpiredToken_returns401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("admin@test.com")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 20_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // TC-08: malformed token string → JwtAuthFilter catches JwtException → 401
    @Test
    void validate_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    // TC-09: tampered signature → JwtAuthFilter catches JwtException → 401
    @Test
    void validate_withTamperedToken_returns401() throws Exception {
        User user = new User("admin@test.com", "pass", Role.ADMIN);
        String valid = jwtUtil.generateToken(user);
        String tampered = valid.substring(0, valid.length() - 6) + "XXXXXX";

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }
}
