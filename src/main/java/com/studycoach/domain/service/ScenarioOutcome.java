package com.studycoach.domain.service;

import com.studycoach.domain.model.DailyPlan;

import java.util.List;

public record ScenarioOutcome(
        String name,
        double deadlineRiskPercent,
        double overloadRiskPercent,
        double stabilityScore,
        String recommendation,
        List<DailyPlan> projectedPlans
) {
}
