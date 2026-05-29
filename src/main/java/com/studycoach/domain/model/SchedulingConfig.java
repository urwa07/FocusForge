package com.studycoach.domain.model;

public record SchedulingConfig(
        double maxHoursPerDay,
        int sessionMinutes,
        int breakMinutes,
        int planningHorizonDays
) {
}
