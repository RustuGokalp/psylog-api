package com.gokalp.psylog_api.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PostPatchRequest {

    private Optional<String> title;
    private Optional<String> summary;
    private Optional<String> content;
    private Optional<String> coverImage;
    private Optional<List<String>> tags;
    private Optional<Boolean> published;
    private Optional<LocalDateTime> publishAt;
    private Optional<Integer> readingTime;

    public Optional<String> getTitle() { return title; }
    public void setTitle(Optional<String> title) { this.title = title; }
    public Optional<String> getSummary() { return summary; }
    public void setSummary(Optional<String> summary) { this.summary = summary; }
    public Optional<String> getContent() { return content; }
    public void setContent(Optional<String> content) { this.content = content; }
    public Optional<String> getCoverImage() { return coverImage; }
    public void setCoverImage(Optional<String> coverImage) { this.coverImage = coverImage; }
    public Optional<List<String>> getTags() { return tags; }
    public void setTags(Optional<List<String>> tags) { this.tags = tags; }
    public Optional<Boolean> getPublished() { return published; }
    public void setPublished(Optional<Boolean> published) { this.published = published; }
    public Optional<LocalDateTime> getPublishAt() { return publishAt; }
    public void setPublishAt(Optional<LocalDateTime> publishAt) { this.publishAt = publishAt; }
    public Optional<Integer> getReadingTime() { return readingTime; }
    public void setReadingTime(Optional<Integer> readingTime) { this.readingTime = readingTime; }
}
