package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.AboutRequest;
import com.gokalp.psylog_api.dto.response.AboutResponse;
import com.gokalp.psylog_api.service.AboutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/about")
public class AdminAboutController {

    private final AboutService aboutService;

    public AdminAboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @PostMapping
    public ResponseEntity<AboutResponse> createAbout(@Valid @RequestBody AboutRequest request) {
        return ResponseEntity.ok(aboutService.createAbout(request));
    }

    @PutMapping
    public ResponseEntity<AboutResponse> updateAbout(@Valid @RequestBody AboutRequest request) {
        return ResponseEntity.ok(aboutService.updateAbout(request));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAbout() {
        aboutService.deleteAbout();
        return ResponseEntity.ok(Map.of("success", true, "message", "About deleted"));
    }
}
