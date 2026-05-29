package com.studycoach.domain.service;

import com.studycoach.domain.model.Task;

import java.util.Comparator;
import java.util.List;

public class BalancedWorkloadStrategy extends AbstractSchedulingStrategy {
    @Override
    public String name() {
        return "Balanced Workload";
    }

    @Override
    public List<com.studycoach.domain.model.DailyPlan> generateSchedule(SchedulingRequest request) {
        List<Task> orderedTasks = request.tasks().stream()
                .filter(Task::isActive)
                .sorted(Comparator.comparingDouble(Task::remainingHours).reversed()
                        .thenComparing(Task::deadline))
                .toList();
        return buildPlans(request, orderedTasks, request.behaviorInsights().bestWindow(), true);
    }
}
