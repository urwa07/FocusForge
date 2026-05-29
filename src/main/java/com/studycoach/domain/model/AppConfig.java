package com.studycoach.domain.model;

import java.util.Map;

public record AppConfig(
        PriorityWeights priorityWeights,
        SchedulingConfig schedulingConfig,
        SimulationConfig simulationConfig,
        Map<String, String> courseColors
) {
}
