package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AboutResponse(
        Long id,
        String message,
        String profileImage,
        List<String> education,
        List<String> workingAreas,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
