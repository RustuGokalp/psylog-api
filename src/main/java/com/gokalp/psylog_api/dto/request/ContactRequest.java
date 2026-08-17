package com.gokalp.psylog_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactRequest {

    @NotBlank
    @Size(max = 50)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 150)
    private String subject;

    @NotBlank
    @Size(max = 750)
    private String message;

    @Pattern(regexp = "^(\\(\\d+\\)(\\s\\d+)+|\\d{7,15}|\\d+(-\\d+)+)$", message = "Invalid phone number format")
    @Schema(example = "532-111-22-33")
    private String mobilePhone;

    // Honeypot — hidden field on the form, only bots fill it in. No validation on purpose.
    @Schema(description = "Honeypot alanı — gerçek kullanıcılar boş bırakır", example = "")
    private String website;

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMobilePhone() { return mobilePhone; }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = (mobilePhone == null || mobilePhone.isBlank()) ? null : mobilePhone;
    }
}
