package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.ContactInfoResponse;
import com.gokalp.psylog_api.service.ContactInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact-info")
public class ContactInfoController {

    private final ContactInfoService contactInfoService;

    public ContactInfoController(ContactInfoService contactInfoService) {
        this.contactInfoService = contactInfoService;
    }

    @GetMapping
    public ResponseEntity<ContactInfoResponse> getContactInfo() {
        return ResponseEntity.ok(contactInfoService.getContactInfo());
    }
}
