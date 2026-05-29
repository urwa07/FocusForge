package com.studycoach.application.dto;

import com.studycoach.domain.model.PriorityLevel;
import com.studycoach.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TaskBoardSnapshot(
        List<TaskCard> tasks,
        List<String> timelineItems
) {
    public record TaskCard(
            String id,
            String course,
            String title,
            LocalDateTime deadline,
            double remainingHours,
            TaskStatus status,
            PriorityLevel priorityLevel,
            double score
    ) {
    }
}
