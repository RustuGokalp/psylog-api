package com.gokalp.psylog_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactInfoRequest {

    @NotBlank
    @Pattern(
            regexp = "^(?:\\d{10}|\\d{3}-\\d{3}-\\d{2}-\\d{2}|(?:(?:\\+90|0)\\s)?(?:\\(\\d{3}\\)|\\d{3})\\s\\d{3}\\s\\d{2}\\s\\d{2})$",
            message = "Geçersiz telefon formatı"
    )
    @Schema(example = "532-111-22-33")
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 200)
    private String location;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
