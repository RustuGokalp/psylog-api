package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.LoginRequest;
import com.gokalp.psylog_api.dto.response.AuthResponse;
import com.gokalp.psylog_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate() {
        return ResponseEntity.ok(new ValidateResponse(true));
    }

    private record ValidateResponse(boolean isValid) {}
}
