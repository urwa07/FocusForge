package com.studycoach.domain.service;

import java.util.List;

public record SimulationResult(
        ScenarioOutcome baseline,
        List<ScenarioOutcome> scenarios,
        ScenarioOutcome bestScenario
) {
}
