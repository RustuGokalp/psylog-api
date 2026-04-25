package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.ContactMessageResponse;
import com.gokalp.psylog_api.dto.response.PagedResponse;
import com.gokalp.psylog_api.repository.ContactMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/contact")
public class AdminContactController {

    private final ContactMessageRepository contactMessageRepository;

    public AdminContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ContactMessageResponse>> getContactMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<ContactMessageResponse> response = new PagedResponse<>(
                contactMessageRepository.findAll(pageable).map(ContactMessageResponse::new)
        );
        return ResponseEntity.ok(response);
    }
}
