package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.SpecializationRequest;
import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.entity.Specialization;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.SpecializationRepository;
import com.gokalp.psylog_api.util.HtmlSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Service
public class SpecializationService {

    private static final Logger log = LoggerFactory.getLogger(SpecializationService.class);

    private final SpecializationRepository specializationRepository;

    public SpecializationService(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    @Transactional(readOnly = true)
    public List<SpecializationResponse> getAll() {
        return specializationRepository.findAllOrdered()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecializationResponse getById(Long id) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found with id: " + id));
        return toResponse(specialization);
    }

    @Transactional(readOnly = true)
    public SpecializationResponse getBySlug(String slug) {
        Specialization specialization = specializationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found: " + slug));
        return toResponse(specialization);
    }

    @Transactional
    public SpecializationResponse create(SpecializationRequest request) {
        Specialization specialization = new Specialization();
        specialization.setTitle(request.getTitle());
        specialization.setSlug(generateUniqueSlug(request.getTitle(), null));
        specialization.setSummary(HtmlSanitizer.sanitize(request.getSummary()));
        specialization.setContent(HtmlSanitizer.sanitize(request.getContent()));
        specialization.setImage(normalizeUrl(request.getImage()));
        specialization.setDisplayOrder(request.getDisplayOrder());
        Specialization saved = specializationRepository.save(specialization);
        log.info("Specialization created: [id={}] {}", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public SpecializationResponse update(Long id, SpecializationRequest request) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found with id: " + id));
        specialization.setTitle(request.getTitle());
        specialization.setSlug(generateUniqueSlug(request.getTitle(), id));
        specialization.setSummary(HtmlSanitizer.sanitize(request.getSummary()));
        specialization.setContent(HtmlSanitizer.sanitize(request.getContent()));
        specialization.setImage(normalizeUrl(request.getImage()));
        specialization.setDisplayOrder(request.getDisplayOrder());
        Specialization saved = specializationRepository.save(specialization);
        log.info("Specialization updated: [id={}] {}", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found with id: " + id));
        specializationRepository.delete(specialization);
        log.info("Specialization deleted: [id={}]", id);
    }

    private String normalizeUrl(String url) {
        return (url != null && !url.isBlank()) ? url : null;
    }

    private String generateUniqueSlug(String title, Long excludeId) {
        String turkishReplaced = title
                .replace('ı', 'i').replace('İ', 'i')
                .replace('ğ', 'g').replace('Ğ', 'g')
                .replace('ü', 'u').replace('Ü', 'u')
                .replace('ş', 's').replace('Ş', 's')
                .replace('ö', 'o').replace('Ö', 'o')
                .replace('ç', 'c').replace('Ç', 'c');
        String normalized = Normalizer.normalize(turkishReplaced, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String baseSlug = normalized
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        String slug = baseSlug;
        int counter = 2;
        while (isSlugTaken(slug, excludeId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private boolean isSlugTaken(String slug, Long excludeId) {
        if (excludeId == null) {
            return specializationRepository.existsBySlug(slug);
        }
        return specializationRepository.existsBySlugAndIdNot(slug, excludeId);
    }

    private SpecializationResponse toResponse(Specialization s) {
        return new SpecializationResponse(
                s.getId(),
                s.getTitle(),
                s.getSlug(),
                s.getSummary(),
                s.getContent(),
                s.getImage(),
                s.getDisplayOrder(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
