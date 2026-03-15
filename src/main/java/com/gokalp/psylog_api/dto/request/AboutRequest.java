package com.gokalp.psylog_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class AboutRequest {

    @NotBlank(message = "Message is required")
    private String message;

    @URL(message = "Profile image must be a valid URL")
    private String profileImage;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
