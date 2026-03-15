package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;

public record CommentPublicResponse(
        Long id,
        String author,
        String content,
        LocalDateTime createdAt
) {}
