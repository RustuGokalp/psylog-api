package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.AboutRequest;
import com.gokalp.psylog_api.dto.response.AboutResponse;
import com.gokalp.psylog_api.entity.About;
import com.gokalp.psylog_api.exception.AlreadyExistsException;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.AboutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AboutService {

    private static final Logger log = LoggerFactory.getLogger(AboutService.class);

    private final AboutRepository aboutRepository;

    public AboutService(AboutRepository aboutRepository) {
        this.aboutRepository = aboutRepository;
    }

    @Transactional(readOnly = true)
    public AboutResponse getAbout() {
        About about = aboutRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("About record not found"));
        log.info("About record fetched");
        return toResponse(about);
    }

    @Transactional
    public AboutResponse createAbout(AboutRequest request) {
        if (aboutRepository.findFirstBy().isPresent()) {
            throw new AlreadyExistsException("About record already exists");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        About about = new About();
        about.setMessage(request.getMessage());
        about.setProfileImage(normalizeUrl(request.getProfileImage()));
        about.setEducation(normalizeList(request.getEducation()));
        about.setWorkingAreas(normalizeList(request.getWorkingAreas()));
        About saved = aboutRepository.save(about);
        log.info("About record created: [id={}]", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AboutResponse updateAbout(AboutRequest request) {
        About about = aboutRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("About record not found"));
        if (request.getMessage() != null) {
            about.setMessage(request.getMessage());
        }
        if (request.getProfileImage() != null) {
            about.setProfileImage(normalizeUrl(request.getProfileImage()));
        }
        if (request.getEducation() != null) {
            about.setEducation(request.getEducation());
        }
        if (request.getWorkingAreas() != null) {
            about.setWorkingAreas(request.getWorkingAreas());
        }
        About saved = aboutRepository.save(about);
        log.info("About record updated: [id={}]", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteAbout() {
        About about = aboutRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("About record not found"));
        aboutRepository.delete(about);
        log.info("About record deleted: [id={}]", about.getId());
    }

    private String normalizeUrl(String url) {
        return (url != null && !url.isBlank()) ? url : null;
    }

    private List<String> normalizeList(List<String> list) {
        return (list != null) ? list : new ArrayList<>();
    }

    private AboutResponse toResponse(About about) {
        return new AboutResponse(
                about.getId(),
                about.getMessage(),
                about.getProfileImage(),
                about.getEducation(),
                about.getWorkingAreas(),
                about.getCreatedAt(),
                about.getUpdatedAt()
        );
    }
}
