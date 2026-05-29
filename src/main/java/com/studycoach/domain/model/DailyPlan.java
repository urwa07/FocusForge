package com.studycoach.domain.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record DailyPlan(
        LocalDate date,
        List<StudySession> sessions,
        double totalHours,
        RiskAssessment riskAssessment,
        double deadlineRiskPercent,
        String recommendation
) {
    public DailyPlan {
        sessions = sessions.stream().sorted(Comparator.comparing(StudySession::start)).toList();
    }
}
