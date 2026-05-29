package com.studycoach.domain.model;

import java.time.LocalDateTime;

public record Task(
        String id,
        String courseId,
        String title,
        String description,
        int difficulty,
        int importance,
        double estimatedHours,
        double completedHours,
        LocalDateTime deadline,
        TaskStatus status,
        LocalDateTime lastUpdated
) {
    public double remainingHours() {
        return Math.max(0.0, estimatedHours - completedHours);
    }

    public boolean isActive() {
        return remainingHours() > 0.01 && status != TaskStatus.COMPLETED;
    }

    public Task updateProgress(double additionalHours, TaskStatus nextStatus, LocalDateTime timestamp) {
        double progress = Math.min(estimatedHours, completedHours + Math.max(0.0, additionalHours));
        TaskStatus derivedStatus = progress >= estimatedHours ? TaskStatus.COMPLETED : nextStatus;
        return new Task(id, courseId, title, description, difficulty, importance, estimatedHours, progress, deadline, derivedStatus, timestamp);
    }
}
