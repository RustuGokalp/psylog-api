package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.CommentRequest;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.service.CommentService;
import com.gokalp.psylog_api.service.RateLimitService;
import com.gokalp.psylog_api.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    private final RateLimitService rateLimitService;

    public CommentController(CommentService commentService, RateLimitService rateLimitService) {
        this.commentService = commentService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    public ResponseEntity<CommentPublicResponse> submitComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = ClientIpResolver.resolve(httpRequest);
        rateLimitService.checkComment(clientIp);

        // Honeypot filled in — silently drop it, but answer as if it succeeded so the bot does not retry.
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.warn("Honeypot tetiklendi — yorum yok sayıldı [ip={}, postId={}]", clientIp, postId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new CommentPublicResponse(
                    null,
                    request.getAuthor(),
                    request.getEmail(),
                    request.getContent(),
                    LocalDateTime.now()
            ));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.submitComment(postId, request));
    }
}
