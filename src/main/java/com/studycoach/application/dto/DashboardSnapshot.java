package com.studycoach.application.dto;

import java.util.List;

public record DashboardSnapshot(
        String studentName,
        List<String> todaysPlan,
        String progressSummary,
        double deadlineRiskPercent,
        List<String> recommendations,
        String dominantInsight
) {
}
