package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.ContactMessageResponse;
import com.gokalp.psylog_api.repository.ContactMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contact")
public class AdminContactController {

    private final ContactMessageRepository contactMessageRepository;

    public AdminContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping
    public ResponseEntity<List<ContactMessageResponse>> getContactMessages() {
        List<ContactMessageResponse> messages = contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ContactMessageResponse::new)
                .toList();
        return ResponseEntity.ok(messages);
    }
}
