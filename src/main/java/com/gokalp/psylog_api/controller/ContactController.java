package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.ContactRequest;
import com.gokalp.psylog_api.service.ContactService;
import com.gokalp.psylog_api.service.RateLimitService;
import com.gokalp.psylog_api.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;
    private final RateLimitService rateLimitService;

    public ContactController(ContactService contactService, RateLimitService rateLimitService) {
        this.contactService = contactService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContact(@Valid @RequestBody ContactRequest request,
                                                             HttpServletRequest httpRequest) {
        String clientIp = ClientIpResolver.resolve(httpRequest);
        rateLimitService.checkContact(clientIp);

        // Honeypot filled in — silently drop it, but answer as if it succeeded so the bot does not retry.
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.warn("Honeypot tetiklendi — iletişim mesajı yok sayıldı [ip={}]", clientIp);
            return ResponseEntity.ok(Map.of("success", true, "message", "Message received"));
        }

        contactService.save(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Message received"));
    }
}
