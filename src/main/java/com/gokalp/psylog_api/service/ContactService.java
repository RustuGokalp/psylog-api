package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.ContactRequest;
import com.gokalp.psylog_api.entity.ContactMessage;
import com.gokalp.psylog_api.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public void save(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        contactMessageRepository.save(message);
    }
}
