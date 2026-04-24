package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.ContactMessage;

import java.time.LocalDateTime;

public class ContactMessageResponse {

    private Long id;
    private String fullName;
    private String email;
    private String subject;
    private String message;
    private String mobilePhone;
    private LocalDateTime createdAt;

    public ContactMessageResponse(ContactMessage m) {
        this.id = m.getId();
        this.fullName = m.getFullName();
        this.email = m.getEmail();
        this.subject = m.getSubject();
        this.message = m.getMessage();
        this.mobilePhone = m.getMobilePhone();
        this.createdAt = m.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getMobilePhone() { return mobilePhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
