package com.gokalp.psylog_api.security;

import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// TC-01 through TC-09: Unit tests for JwtUtil — no Spring context, no DB
class JwtUtilTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha-256";
    private static final long TEST_EXPIRATION = 3_600_000L;

    private JwtUtil jwtUtil;
    private User adminUser;

    @BeforeEach
    void setup() throws Exception {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
        Method init = JwtUtil.class.getDeclaredMethod("initSigningKey");
        init.setAccessible(true);
        init.invoke(jwtUtil);

        adminUser = new User("admin@test.com", "pass", Role.ADMIN);
    }

    // TC-01: subject claim must be the user's email
    @Test
    void generateToken_subjectIsEmail() {
        String token = jwtUtil.generateToken(adminUser);
        assertEquals("admin@test.com", jwtUtil.extractUsername(token));
    }

    // TC-02: role claim must be present and equal to "ROLE_ADMIN"
    @Test
    void generateToken_containsRoleAdminClaim() {
        String token = jwtUtil.generateToken(adminUser);
        assertEquals("ROLE_ADMIN", jwtUtil.extractRole(token));
    }

    // TC-03: extractAllClaims must return both subject and role in a single parse
    @Test
    void extractAllClaims_returnsSubjectAndRole() {
        String token = jwtUtil.generateToken(adminUser);
        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("admin@test.com", claims.getSubject());
        assertEquals("ROLE_ADMIN", claims.get("role", String.class));
    }

    // TC-04: getExpiration must reflect the configured value
    @Test
    void getExpiration_returnsConfiguredValue() {
        assertEquals(TEST_EXPIRATION, jwtUtil.getExpiration());
    }

    // TC-05: expired token must throw JwtException (caught by JwtAuthFilter)
    @Test
    void extractAllClaims_expiredToken_throwsJwtException() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("admin@test.com")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 20_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims(expiredToken));
    }

    // TC-06: malformed (non-JWT) token must throw JwtException
    @Test
    void extractAllClaims_malformedToken_throwsJwtException() {
        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims("not.a.valid.jwt"));
    }

    // TC-07: tampered signature must throw JwtException
    @Test
    void extractAllClaims_tamperedSignature_throwsJwtException() {
        String validToken = jwtUtil.generateToken(adminUser);
        String tampered = validToken.substring(0, validToken.length() - 6) + "XXXXXX";
        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims(tampered));
    }

    // TC-08: token signed with a different key must throw JwtException
    @Test
    void extractAllClaims_differentSecret_throwsJwtException() {
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-secret-key-that-is-long-enough-for-hmac".getBytes(StandardCharsets.UTF_8));
        String tokenWithDifferentKey = Jwts.builder()
                .subject("admin@test.com")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(differentKey)
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims(tokenWithDifferentKey));
    }

    // TC-09: token without role claim → extractRole must return null (filter null-guard handles this)
    @Test
    void extractRole_tokenWithoutRoleClaim_returnsNull() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenNoRole = Jwts.builder()
                .subject("admin@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(key)
                .compact();

        assertNull(jwtUtil.extractRole(tokenNoRole));
    }
}
