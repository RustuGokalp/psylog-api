package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.CommentRequest;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentPublicResponse> submitComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.submitComment(postId, request));
    }
}
