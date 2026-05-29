package com.studycoach.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ScheduleSnapshot(
        List<DayPlanView> days
) {
    public record DayPlanView(
            LocalDate date,
            List<SessionView> sessions
    ) {
    }

    public record SessionView(
            String id,
            String taskId,
            String courseId,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            String timeRange,
            String colorHex
    ) {
    }
}
