package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.ContactInfo;

import java.time.LocalDateTime;
import java.util.List;

public class ContactInfoResponse {

    private final Long id;
    private final String phone;
    private final String email;
    private final String location;
    private final String instagram;
    private final LocalDateTime updatedAt;
    private final List<WorkingHourResponse> workingHours;

    public ContactInfoResponse(ContactInfo c) {
        this.id = c.getId();
        this.phone = c.getPhone();
        this.email = c.getEmail();
        this.location = c.getLocation();
        this.instagram = c.getInstagram();
        this.updatedAt = c.getUpdatedAt();
        this.workingHours = c.getWorkingHours().stream()
                .map(WorkingHourResponse::new)
                .toList();
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getLocation() { return location; }
    public String getInstagram() { return instagram; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<WorkingHourResponse> getWorkingHours() { return workingHours; }
}
