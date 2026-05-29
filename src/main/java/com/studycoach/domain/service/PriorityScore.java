package com.studycoach.domain.service;

import com.studycoach.domain.model.PriorityLevel;

import java.util.List;

public record PriorityScore(
        String taskId,
        double score,
        PriorityLevel level,
        List<String> reasons
) {
}
