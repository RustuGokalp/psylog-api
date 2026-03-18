package com.gokalp.psylog_api.security;

import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

// TC-01 through TC-07: Unit tests for JwtAuthFilter — verifies filter chain behavior per token state
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha-256";

    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setup() throws Exception {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3_600_000L);
        Method init = JwtUtil.class.getDeclaredMethod("initSigningKey");
        init.setAccessible(true);
        init.invoke(jwtUtil);

        filter = new JwtAuthFilter(jwtUtil);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // TC-01: no Authorization header → chain continues, no authentication set
    @Test
    void noAuthorizationHeader_continuesChainUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // TC-02: non-Bearer Authorization header → chain continues, no authentication set
    @Test
    void nonBearerAuthorizationHeader_continuesChainUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // TC-03: valid token → authentication set in SecurityContext with correct username and role
    @Test
    void validToken_setsAuthenticationWithCorrectUsernameAndRole() throws Exception {
        User user = new User("admin@test.com", "pass", Role.ADMIN);
        String token = jwtUtil.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin@test.com", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // TC-04: expired token → JwtException caught by filter, chain continues, no authentication set
    @Test
    void expiredToken_continuesChainUnauthenticated() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("admin@test.com")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 20_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // TC-05: malformed token → JwtException caught, chain continues, no authentication set
    @Test
    void malformedToken_continuesChainUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer this.is.not.valid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // TC-06: tampered signature → JwtException caught, chain continues, no authentication set
    @Test
    void tamperedToken_continuesChainUnauthenticated() throws Exception {
        User user = new User("admin@test.com", "pass", Role.ADMIN);
        String valid = jwtUtil.generateToken(user);
        String tampered = valid.substring(0, valid.length() - 6) + "XXXXXX";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tampered);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // TC-07: token without role claim → null-guard in filter prevents authentication from being set
    @Test
    void tokenWithoutRoleClaim_continuesChainUnauthenticated() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenNoRole = Jwts.builder()
                .subject("admin@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenNoRole);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
