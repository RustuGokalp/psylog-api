package com.gokalp.psylog_api.controller;

import com.gokalp.psylog_api.dto.response.PageHeroResponse;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.service.PageHeroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Page Heroes", description = "Public page hero endpoint")
@RestController
@RequestMapping("/api/page-heroes")
public class PageHeroController {

    private final PageHeroService pageHeroService;

    public PageHeroController(PageHeroService pageHeroService) {
        this.pageHeroService = pageHeroService;
    }

    @Operation(summary = "List all page heroes", description = "Returns all page heroes, empty array if none exist. No authentication required.")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<PageHeroResponse>> getAll() {
        return ResponseEntity.ok(pageHeroService.getAll());
    }

    @Operation(summary = "Get page hero by pageKey", description = "Returns a single page hero. 404 if not found, 400 if the pageKey is unknown.")
    @SecurityRequirements
    @GetMapping("/{pageKey}")
    public ResponseEntity<PageHeroResponse> getByPageKey(@PathVariable PageKey pageKey) {
        return ResponseEntity.ok(pageHeroService.getByPageKey(pageKey));
    }
}
