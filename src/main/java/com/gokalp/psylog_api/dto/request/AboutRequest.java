package com.gokalp.psylog_api.dto.request;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class AboutRequest {

    @Size(max = 50000, message = "Message must be at most 50000 characters")
    private String message;

    @URL(message = "Profile image must be a valid URL")
    private String profileImage;

    private List<String> education;

    private List<String> workingAreas;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }
    public List<String> getWorkingAreas() { return workingAreas; }
    public void setWorkingAreas(List<String> workingAreas) { this.workingAreas = workingAreas; }
}
