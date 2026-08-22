package com.gokalp.psylog_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// pageKey is never part of the body — it always comes from the path and can never change
public class PageHeroRequest {

    @NotBlank(message = "Subtitle is required")
    @Size(max = 60, message = "Subtitle must be at most 60 characters")
    private String subtitle;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be at most 120 characters")
    private String title;

    @Size(max = 400, message = "Description must be at most 400 characters")
    private String description;

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
