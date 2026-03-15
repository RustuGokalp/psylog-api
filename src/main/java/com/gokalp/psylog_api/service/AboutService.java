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
        About about = new About();
        about.setMessage(request.getMessage());
        about.setProfileImage(normalizeUrl(request.getProfileImage()));
        About saved = aboutRepository.save(about);
        log.info("About record created: [id={}]", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AboutResponse updateAbout(AboutRequest request) {
        About about = aboutRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("About record not found"));
        about.setMessage(request.getMessage());
        about.setProfileImage(normalizeUrl(request.getProfileImage()));
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

    private AboutResponse toResponse(About about) {
        return new AboutResponse(
                about.getId(),
                about.getMessage(),
                about.getProfileImage(),
                about.getCreatedAt(),
                about.getUpdatedAt()
        );
    }
}
