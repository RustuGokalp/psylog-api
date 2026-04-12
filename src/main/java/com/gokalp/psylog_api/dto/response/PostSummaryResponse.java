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
    private LocalDateTime updatedAt;
    private LocalDateTime publishAt;
    private Integer readingTime;
    private List<CommentAdminResponse> comments;

    public PostSummaryResponse(Long id, String title, String slug, String summary,
                                String coverImage, List<String> tags, boolean published,
                                LocalDateTime createdAt, LocalDateTime updatedAt,
                                LocalDateTime publishAt, Integer readingTime,
                                List<CommentAdminResponse> comments) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.coverImage = coverImage;
        this.tags = tags;
        this.published = published;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishAt = publishAt;
        this.readingTime = readingTime;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getSummary() { return summary; }
    public String getCoverImage() { return coverImage; }
    public List<String> getTags() { return tags; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPublishAt() { return publishAt; }
    public Integer getReadingTime() { return readingTime; }
    public List<CommentAdminResponse> getComments() { return comments; }
}
