package com.studycoach.domain.service;

import com.studycoach.domain.model.PriorityLevel;
import com.studycoach.domain.model.Task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PriorityEngine implements PriorityScoringPolicy {
    @Override
    public PriorityScore scoreTask(Task task, PriorityContext context) {
        long hoursToDeadline = Math.max(1, Duration.between(context.now(), task.deadline()).toHours());
        double deadlineFactor = 1.0 / hoursToDeadline;
        double difficultyFactor = task.difficulty() / 5.0;
        double importanceFactor = task.importance() / 5.0;
        double behaviorFactor = context.avoidedTaskIds().contains(task.id()) ? 1.0 : 0.25;

        double score = deadlineFactor * context.weights().deadlineWeight() * 100
                + difficultyFactor * context.weights().difficultyWeight() * 10
                + importanceFactor * context.weights().importanceWeight() * 10
                + behaviorFactor * context.weights().behaviorWeight() * 10;

        List<String> reasons = new ArrayList<>();
        reasons.add("Deadline in " + hoursToDeadline + "h");
        reasons.add("Difficulty " + task.difficulty() + "/5");
        reasons.add("Importance " + task.importance() + "/5");
        if (behaviorFactor > 0.5) {
            reasons.add("Avoidance detected");
        }

        PriorityLevel level = score >= 18 ? PriorityLevel.CRITICAL
                : score >= 12 ? PriorityLevel.HIGH
                : score >= 7 ? PriorityLevel.MEDIUM
                : PriorityLevel.LOW;

        return new PriorityScore(task.id(), score, level, reasons);
    }
}
