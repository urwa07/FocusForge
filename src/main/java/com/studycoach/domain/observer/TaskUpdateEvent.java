package com.studycoach.domain.observer;

import com.studycoach.domain.model.TaskStatus;

import java.time.LocalDateTime;

public record TaskUpdateEvent(
        String taskId,
        TaskStatus status,
        double completedHoursDelta,
        LocalDateTime timestamp
) {
}
