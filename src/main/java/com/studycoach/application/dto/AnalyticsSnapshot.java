package com.studycoach.application.dto;

import java.util.List;

public record AnalyticsSnapshot(
        List<Point> productivityPoints,
        List<Point> subjectDistribution,
        List<String> insights
) {
    public record Point(String label, double value) {
    }
}
