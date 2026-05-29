package com.studycoach.domain.model;

import java.time.Duration;
import java.time.LocalTime;

public record TimeWindow(LocalTime start, LocalTime end) {
    public double durationHours() {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(start) && !time.isAfter(end);
    }
}
