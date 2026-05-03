package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.ContactRequest;
import com.gokalp.psylog_api.entity.ContactMessage;
import com.gokalp.psylog_api.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    public ContactService(ContactMessageRepository contactMessageRepository, EmailService emailService) {
        this.contactMessageRepository = contactMessageRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void save(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        message.setMobilePhone(request.getMobilePhone());
        contactMessageRepository.save(message);
        log.info("Contact message received from: {}", request.getEmail());

        emailService.sendContactNotification(message);
    }
}
