package com.studycoach.domain.model;

import java.util.Map;
import java.util.Set;

public record BehaviorInsights(
        Map<Integer, Double> productivityByHour,
        Set<String> avoidedTaskIds,
        TimeWindow bestWindow,
        BehaviorPattern dominantPattern,
        String narrative
) {
}
