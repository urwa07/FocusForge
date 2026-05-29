package com.studycoach.domain.model;

import java.util.List;

public record Student(
        String id,
        String name,
        int age,
        double maxStudyHoursPerDay,
        List<TimeWindow> availability,
        ProductivityProfile productivityProfile
) {
}
