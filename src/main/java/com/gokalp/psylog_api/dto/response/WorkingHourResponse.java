package com.gokalp.psylog_api.dto.response;

import com.gokalp.psylog_api.entity.WorkingHour;
import com.gokalp.psylog_api.entity.WorkingHourStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WorkingHourResponse {

    private final DayOfWeek dayOfWeek;
    private final WorkingHourStatus status;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public WorkingHourResponse(WorkingHour w) {
        this.dayOfWeek = w.getDayOfWeek();
        this.status = w.getStatus();
        this.startTime = w.getStartTime();
        this.endTime = w.getEndTime();
    }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public WorkingHourStatus getStatus() { return status; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
