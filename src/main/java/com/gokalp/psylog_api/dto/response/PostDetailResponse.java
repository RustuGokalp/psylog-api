package com.gokalp.psylog_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PostDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImage;
    private List<String> tags;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishAt;
    private List<CommentPublicResponse> comments;
    private Integer readingTime;

    public PostDetailResponse(Long id, String title, String slug, String summary, String content,
                               String coverImage, List<String> tags, boolean published,
                               LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime publishAt,
                               List<CommentPublicResponse> comments, Integer readingTime) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.content = content;
        this.coverImage = coverImage;
        this.tags = tags;
        this.published = published;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishAt = publishAt;
        this.comments = comments;
        this.readingTime = readingTime;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public String getCoverImage() { return coverImage; }
    public List<String> getTags() { return tags; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPublishAt() { return publishAt; }
    public List<CommentPublicResponse> getComments() { return comments; }
    public Integer getReadingTime() { return readingTime; }
}
