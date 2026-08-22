package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.request.PageHeroRequest;
import com.gokalp.psylog_api.dto.response.PageHeroResponse;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.service.PageHeroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin – Page Heroes", description = "Admin management of page heroes. Requires authentication. The four records are seeded, so there is no create or delete.")
@RestController
@RequestMapping("/api/admin/page-heroes")
public class AdminPageHeroController {

    private final PageHeroService pageHeroService;

    public AdminPageHeroController(PageHeroService pageHeroService) {
        this.pageHeroService = pageHeroService;
    }

    @Operation(summary = "List all page heroes")
    @GetMapping
    public ResponseEntity<List<PageHeroResponse>> getAll() {
        return ResponseEntity.ok(pageHeroService.getAll());
    }

    @Operation(summary = "Get page hero by pageKey", description = "Returns 404 if not found.")
    @GetMapping("/{pageKey}")
    public ResponseEntity<PageHeroResponse> getByPageKey(@PathVariable PageKey pageKey) {
        return ResponseEntity.ok(pageHeroService.getByPageKey(pageKey));
    }

    @Operation(summary = "Update a page hero", description = "pageKey comes from the path and can never change. Returns 404 if not found.")
    @PutMapping("/{pageKey}")
    public ResponseEntity<PageHeroResponse> update(@PathVariable PageKey pageKey,
                                                   @Valid @RequestBody PageHeroRequest request) {
        return ResponseEntity.ok(pageHeroService.update(pageKey, request));
    }
}
