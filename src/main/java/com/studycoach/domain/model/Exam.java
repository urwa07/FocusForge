package com.studycoach.domain.model;

import java.time.LocalDateTime;

public record Exam(
        String id,
        String courseId,
        String title,
        LocalDateTime startTime,
        int importance,
        double syllabusWeight
) {
}
