package com.studycoach.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;

public record StudySession(
        String id,
        String taskId,
        String courseId,
        String title,
        LocalDateTime start,
        LocalDateTime end,
        SessionType sessionType,
        double priorityScore,
        boolean completed,
        FocusReflection reflection
) {
    public StudySession {
        if (reflection == null) {
            reflection = FocusReflection.NONE;
        }
    }

    public double durationHours() {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    public StudySession moveTo(LocalDateTime newStart, LocalDateTime newEnd) {
        return new StudySession(id, taskId, courseId, title, newStart, newEnd, sessionType, priorityScore, completed, reflection);
    }
}
