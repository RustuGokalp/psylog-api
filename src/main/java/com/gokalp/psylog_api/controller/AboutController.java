package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.AboutResponse;
import com.gokalp.psylog_api.service.AboutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about")
public class AboutController {

    private final AboutService aboutService;

    public AboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @GetMapping
    public ResponseEntity<AboutResponse> getAbout() {
        return ResponseEntity.ok(aboutService.getAbout());
    }
}
