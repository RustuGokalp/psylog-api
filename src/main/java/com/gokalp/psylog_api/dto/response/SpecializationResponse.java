package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;

public record SpecializationResponse(
        Long id,
        String title,
        String description,
        String image,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
