package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.SpecializationRequest;
import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.entity.Specialization;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.SpecializationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public SpecializationResponse create(SpecializationRequest request) {
        Specialization specialization = new Specialization();
        specialization.setTitle(request.getTitle());
        specialization.setDescription(request.getDescription());
        specialization.setImage(normalizeUrl(request.getImage()));
        specialization.setDisplayOrder(request.getDisplayOrder());
        Specialization saved = specializationRepository.save(specialization);
        log.info("Specialization created: [id={}]", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public SpecializationResponse update(Long id, SpecializationRequest request) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found with id: " + id));
        specialization.setTitle(request.getTitle());
        specialization.setDescription(request.getDescription());
        specialization.setImage(normalizeUrl(request.getImage()));
        specialization.setDisplayOrder(request.getDisplayOrder());
        Specialization saved = specializationRepository.save(specialization);
        log.info("Specialization updated: [id={}]", saved.getId());
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

    private SpecializationResponse toResponse(Specialization s) {
        return new SpecializationResponse(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getImage(),
                s.getDisplayOrder(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
