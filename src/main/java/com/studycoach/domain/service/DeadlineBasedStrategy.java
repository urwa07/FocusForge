package com.studycoach.domain.service;

import com.studycoach.domain.model.Task;

import java.util.Comparator;
import java.util.List;

public class DeadlineBasedStrategy extends AbstractSchedulingStrategy {
    @Override
    public String name() {
        return "Deadline Based";
    }

    @Override
    public List<com.studycoach.domain.model.DailyPlan> generateSchedule(SchedulingRequest request) {
        List<Task> orderedTasks = request.tasks().stream()
                .filter(Task::isActive)
                .sorted(Comparator.comparing(Task::deadline)
                        .thenComparing(task -> -request.priorityScores().get(task.id()).score()))
                .toList();
        return buildPlans(request, orderedTasks, request.behaviorInsights().bestWindow(), false);
    }
}
