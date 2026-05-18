package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.ContactInfoRequest;
import com.gokalp.psylog_api.dto.response.ContactInfoResponse;
import com.gokalp.psylog_api.service.ContactInfoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/contact-info")
public class AdminContactInfoController {

    private final ContactInfoService contactInfoService;

    public AdminContactInfoController(ContactInfoService contactInfoService) {
        this.contactInfoService = contactInfoService;
    }

    @PostMapping
    public ResponseEntity<ContactInfoResponse> createContactInfo(@Valid @RequestBody ContactInfoRequest request) {
        return ResponseEntity.ok(contactInfoService.createContactInfo(request));
    }

    @PutMapping
    public ResponseEntity<ContactInfoResponse> updateContactInfo(@Valid @RequestBody ContactInfoRequest request) {
        return ResponseEntity.ok(contactInfoService.updateContactInfo(request));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteContactInfo() {
        contactInfoService.deleteContactInfo();
        return ResponseEntity.ok(Map.of("success", true, "message", "Contact info deleted"));
    }
}
