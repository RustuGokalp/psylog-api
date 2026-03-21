package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Specializations", description = "Public specializations endpoint")
@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @Operation(summary = "List all specializations", description = "Returns all specializations ordered by displayOrder (nulls last), then createdAt. No authentication required.")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<SpecializationResponse>> getAll() {
        return ResponseEntity.ok(specializationService.getAll());
    }
}
