package com.studycoach.domain.service;

import com.studycoach.domain.model.PriorityWeights;
import com.studycoach.domain.model.TimeWindow;

import java.time.LocalDateTime;
import java.util.Set;

public record PriorityContext(
        LocalDateTime now,
        Set<String> avoidedTaskIds,
        TimeWindow bestWindow,
        PriorityWeights weights
) {
}
