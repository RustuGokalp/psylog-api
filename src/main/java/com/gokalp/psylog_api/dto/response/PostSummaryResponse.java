package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PostSummaryResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverImage;
    private List<String> tags;
    private boolean published;
    private LocalDateTime createdAt;
    private Integer readingTime;

    public PostSummaryResponse(Long id, String title, String slug, String summary,
                                String coverImage, List<String> tags, boolean published,
                                LocalDateTime createdAt, Integer readingTime) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.coverImage = coverImage;
        this.tags = tags;
        this.published = published;
        this.createdAt = createdAt;
        this.readingTime = readingTime;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getSummary() { return summary; }
    public String getCoverImage() { return coverImage; }
    public List<String> getTags() { return tags; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getReadingTime() { return readingTime; }
}
