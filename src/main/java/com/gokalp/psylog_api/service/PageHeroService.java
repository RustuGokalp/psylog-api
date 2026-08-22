package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.PageHeroRequest;
import com.gokalp.psylog_api.dto.response.PageHeroResponse;
import com.gokalp.psylog_api.entity.PageHero;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.PageHeroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Page hero strips (subtitle + title + description) shown at the top of the public pages.
 * The four records are seeded at startup; there is no create or delete — only update.
 * All three fields are plain text, so no HTML sanitization is applied here.
 */
@Service
public class PageHeroService {

    private static final Logger log = LoggerFactory.getLogger(PageHeroService.class);

    private final PageHeroRepository pageHeroRepository;

    public PageHeroService(PageHeroRepository pageHeroRepository) {
        this.pageHeroRepository = pageHeroRepository;
    }

    @Transactional(readOnly = true)
    public List<PageHeroResponse> getAll() {
        return pageHeroRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageHeroResponse getByPageKey(PageKey pageKey) {
        PageHero pageHero = pageHeroRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Page hero not found: " + pageKey));
        return toResponse(pageHero);
    }

    @Transactional
    public PageHeroResponse update(PageKey pageKey, PageHeroRequest request) {
        PageHero pageHero = pageHeroRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Page hero not found: " + pageKey));
        pageHero.setSubtitle(request.getSubtitle());
        pageHero.setTitle(request.getTitle());
        pageHero.setDescription(normalizeText(request.getDescription()));
        PageHero saved = pageHeroRepository.save(pageHero);
        log.info("Page hero updated: [pageKey={}] {}", saved.getPageKey(), saved.getTitle());
        return toResponse(saved);
    }

    private String normalizeText(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }

    private PageHeroResponse toResponse(PageHero p) {
        return new PageHeroResponse(
                p.getId(),
                p.getPageKey(),
                p.getSubtitle(),
                p.getTitle(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
