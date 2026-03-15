package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.CommentStatus;

import java.time.LocalDateTime;

public record CommentAdminResponse(
        Long id,
        Long postId,
        String postTitle,
        String postSlug,
        String author,
        String email,
        String content,
        CommentStatus status,
        LocalDateTime createdAt
) {}
