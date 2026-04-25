package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.SpecializationRequest;
import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin – Specializations", description = "Admin CRUD for specializations. Requires authentication.")
@RestController
@RequestMapping("/api/admin/specializations")
public class AdminSpecializationController {

    private final SpecializationService specializationService;

    public AdminSpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @Operation(summary = "List all specializations")
    @GetMapping
    public ResponseEntity<List<SpecializationResponse>> getAll() {
        return ResponseEntity.ok(specializationService.getAll());
    }

    @Operation(summary = "Get specialization by id", description = "Returns 404 if not found.")
    @GetMapping("/{id}")
    public ResponseEntity<SpecializationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(specializationService.getById(id));
    }

    @Operation(summary = "Create a specialization", description = "image and displayOrder are optional.")
    @PostMapping
    public ResponseEntity<SpecializationResponse> create(@Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.create(request));
    }

    @Operation(summary = "Update a specialization", description = "Returns 404 if not found.")
    @PutMapping("/{id}")
    public ResponseEntity<SpecializationResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.update(id, request));
    }

    @Operation(summary = "Delete a specialization", description = "Returns 404 if not found.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        specializationService.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Specialization deleted"));
    }
}
