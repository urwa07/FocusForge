package com.studycoach.application.dto;

import java.util.List;

public record SimulationSnapshot(
        double baselineDeadlineRisk,
        double baselineOverloadRisk,
        List<ScenarioCard> scenarios,
        String recommendation
) {
    public record ScenarioCard(
            String name,
            double deadlineRisk,
            double overloadRisk,
            double stability,
            String recommendation
    ) {
    }
}
