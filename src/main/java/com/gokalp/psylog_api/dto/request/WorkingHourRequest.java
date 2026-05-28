package com.gokalp.psylog_api.dto.request;

import com.gokalp.psylog_api.entity.WorkingHourStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WorkingHourRequest {

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private WorkingHourStatus status;

    private LocalTime startTime;

    private LocalTime endTime;

    // OPEN günlerde başlangıç/bitiş zorunlu ve başlangıç < bitiş olmalı;
    // diğer durumlarda (BY_APPOINTMENT / CLOSED) saatler boş bırakılmalı.
    @JsonIgnore
    @AssertTrue(message = "OPEN günlerde başlangıç ve bitiş saati gerekli (başlangıç < bitiş); diğer durumlarda saatler boş olmalı")
    public boolean isValidTimes() {
        if (status == null) {
            return true; // @NotNull ayrı raporlasın
        }
        if (status == WorkingHourStatus.OPEN) {
            return startTime != null && endTime != null && startTime.isBefore(endTime);
        }
        return startTime == null && endTime == null;
    }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public WorkingHourStatus getStatus() { return status; }
    public void setStatus(WorkingHourStatus status) { this.status = status; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
