package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;

public record AboutResponse(
        Long id,
        String message,
        String profileImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
