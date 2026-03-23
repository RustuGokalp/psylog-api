package com.gokalp.psylog_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "about")
public class About {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String profileImage;

    @ElementCollection
    @CollectionTable(name = "about_education", joinColumns = @JoinColumn(name = "about_id"))
    @Column(name = "item")
    private List<String> education = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "about_working_areas", joinColumns = @JoinColumn(name = "about_id"))
    @Column(name = "item")
    private List<String> workingAreas = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
        updatedAt = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
    }

    public About() {}

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }
    public List<String> getWorkingAreas() { return workingAreas; }
    public void setWorkingAreas(List<String> workingAreas) { this.workingAreas = workingAreas; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
