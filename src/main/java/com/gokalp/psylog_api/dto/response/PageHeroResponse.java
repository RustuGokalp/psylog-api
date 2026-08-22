package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.PageKey;

import java.time.LocalDateTime;

public record PageHeroResponse(
        Long id,
        PageKey pageKey,
        String subtitle,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
