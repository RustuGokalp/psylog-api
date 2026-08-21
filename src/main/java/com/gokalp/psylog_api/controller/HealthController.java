package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "Public liveness endpoint")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final HealthResponse UP = new HealthResponse("UP");

    @Operation(
            summary = "Liveness check",
            description = "Returns 200 as long as the application is running. Does not touch the database. "
                    + "Used by an external cron service to keep the free-tier instance awake. No authentication required.")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(UP);
    }
}
