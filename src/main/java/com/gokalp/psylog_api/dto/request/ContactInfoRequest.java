package com.gokalp.psylog_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

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

    @Valid
    @NotNull
    @Size(min = 7, max = 7, message = "Haftanın 7 günü de gönderilmeli")
    private List<WorkingHourRequest> workingHours;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public List<WorkingHourRequest> getWorkingHours() { return workingHours; }
    public void setWorkingHours(List<WorkingHourRequest> workingHours) { this.workingHours = workingHours; }
}
