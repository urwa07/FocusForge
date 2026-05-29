package com.studycoach.domain.service;

public record SimulationAction(
        String label,
        int extraHoursPerDay,
        int delayTaskByDays,
        String strategyName,
        boolean skipToday
) {
}
