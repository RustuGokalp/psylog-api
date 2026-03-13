package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.PostDetailResponse;
import com.gokalp.psylog_api.dto.response.PostSummaryResponse;
import com.gokalp.psylog_api.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostSummaryResponse>> getPublishedPosts() {
        return ResponseEntity.ok(postService.getPublishedPosts());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostDetailResponse> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPublishedPostBySlug(slug));
    }
}
