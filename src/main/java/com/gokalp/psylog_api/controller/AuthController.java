package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.LoginRequest;
import com.gokalp.psylog_api.dto.response.AuthResponse;
import com.gokalp.psylog_api.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Local: secure=false, sameSite=Lax, domain empty
    // Production: secure=true, sameSite=None, domain=.psktugcetekin.com
    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Value("${cookie.same-site}")
    private String cookieSameSite;

    @Value("${cookie.domain}")
    private String cookieDomain;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse.UserInfo> login(@Valid @RequestBody LoginRequest request,
                                                       HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie cookie = tokenCookie(result.token(), Duration.ofMillis(result.expiresIn()));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(result.userInfo());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Same attributes as login — otherwise the browser will not remove the cookie
        ResponseCookie cookie = tokenCookie("", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    private ResponseCookie tokenCookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("token", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate() {
        return ResponseEntity.ok(new ValidateResponse(true));
    }

    private record ValidateResponse(boolean isValid) {}
}
