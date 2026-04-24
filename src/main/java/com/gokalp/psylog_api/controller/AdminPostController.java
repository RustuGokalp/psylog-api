package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.PostPatchRequest;
import com.gokalp.psylog_api.dto.request.PostRequest;
import com.gokalp.psylog_api.dto.response.PagedResponse;
import com.gokalp.psylog_api.dto.response.PostDetailResponse;
import com.gokalp.psylog_api.dto.response.PostSummaryResponse;
import com.gokalp.psylog_api.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PostSummaryResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailResponse> updatePost(@PathVariable Long id,
                                                          @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostDetailResponse> patchPost(@PathVariable Long id,
                                                         @RequestBody PostPatchRequest request) {
        return ResponseEntity.ok(postService.patchPost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Post deleted"));
    }
}
