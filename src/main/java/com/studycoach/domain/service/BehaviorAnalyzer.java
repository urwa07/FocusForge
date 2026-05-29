package com.studycoach.domain.service;

import com.studycoach.domain.model.BehaviorInsights;
import com.studycoach.domain.model.BehaviorPattern;
import com.studycoach.domain.model.StudySession;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.TaskStatus;
import com.studycoach.domain.model.TimeWindow;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BehaviorAnalyzer {
    public BehaviorInsights analyze(List<Task> tasks, List<StudySession> history) {
        Map<Integer, Double> productivityByHour = new HashMap<>();
        history.stream()
                .filter(StudySession::completed)
                .forEach(session -> productivityByHour.merge(session.start().getHour(), session.durationHours(), Double::sum));

        int bestHour = productivityByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(18);

        Set<String> avoidedTasks = tasks.stream()
                .filter(task -> task.status() == TaskStatus.SKIPPED)
                .map(Task::id)
                .collect(Collectors.toSet());

        BehaviorPattern pattern = bestHour < 12 ? BehaviorPattern.PEAK_MORNING
                : bestHour < 17 ? BehaviorPattern.PEAK_AFTERNOON
                : BehaviorPattern.PEAK_EVENING;
        if (!avoidedTasks.isEmpty()) {
            pattern = BehaviorPattern.AVOIDANCE_TREND;
        }

        TimeWindow bestWindow = new TimeWindow(LocalTime.of(bestHour, 0), LocalTime.of(Math.min(23, bestHour + 2), 0));
        String narrative = avoidedTasks.isEmpty()
                ? "Consistent follow-through with strongest focus around " + bestHour + ":00."
                : "Avoidance trend detected. The coach will front-load skipped topics near " + bestHour + ":00.";

        return new BehaviorInsights(productivityByHour, avoidedTasks, bestWindow, pattern, narrative);
    }
}
