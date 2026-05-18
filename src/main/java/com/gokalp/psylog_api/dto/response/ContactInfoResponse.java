package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.ContactInfo;

import java.time.LocalDateTime;

public class ContactInfoResponse {

    private final Long id;
    private final String phone;
    private final String email;
    private final String location;
    private final LocalDateTime updatedAt;

    public ContactInfoResponse(ContactInfo c) {
        this.id = c.getId();
        this.phone = c.getPhone();
        this.email = c.getEmail();
        this.location = c.getLocation();
        this.updatedAt = c.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getLocation() { return location; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
