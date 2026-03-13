package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.ContactRequest;
import com.gokalp.psylog_api.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContact(@Valid @RequestBody ContactRequest request) {
        contactService.save(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Message received"));
    }
}
