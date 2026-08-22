package com.gokalp.psylog_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "page_heroes")
public class PageHero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private PageKey pageKey;

    @Column(nullable = false, length = 60)
    private String subtitle;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 400)
    private String description;

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

    public PageHero() {}

    public PageHero(PageKey pageKey, String subtitle, String title, String description) {
        this.pageKey = pageKey;
        this.subtitle = subtitle;
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public PageKey getPageKey() { return pageKey; }
    public void setPageKey(PageKey pageKey) { this.pageKey = pageKey; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
