package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.CommentAdminResponse;
import com.gokalp.psylog_api.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentAdminResponse>> getPendingComments() {
        return ResponseEntity.ok(commentService.getPendingComments());
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentAdminResponse>> getAllCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getAllCommentsByPost(postId));
    }

    @PatchMapping("/comments/{id}/approve")
    public ResponseEntity<CommentAdminResponse> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.approveComment(id));
    }

    @PatchMapping("/comments/{id}/reject")
    public ResponseEntity<CommentAdminResponse> rejectComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.rejectComment(id));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Comment deleted"));
    }
}
