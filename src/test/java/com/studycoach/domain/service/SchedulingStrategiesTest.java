package com.studycoach.domain.service;

import com.studycoach.TestDataFactory;
import com.studycoach.domain.model.DailyPlan;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.StudySession;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulingStrategiesTest {
    private final BehaviorAnalyzer behaviorAnalyzer = new BehaviorAnalyzer();
    private final PriorityEngine priorityEngine = new PriorityEngine();

    @Test
    void strategiesRespectMaxHoursPerDayAndBreakIntoMultipleDays() {
        var state = TestDataFactory.sampleState();
        var insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        var context = new PriorityContext(
                LocalDateTime.now(),
                insights.avoidedTaskIds(),
                insights.bestWindow(),
                state.config().priorityWeights()
        );

        Map<String, PriorityScore> scores = state.tasks().stream()
                .collect(Collectors.toMap(Task::id, task -> priorityEngine.scoreTask(task, context)));

        SchedulingRequest request = new SchedulingRequest(
                LocalDateTime.now(),
                state.student(),
                state.courses(),
                state.tasks(),
                state.exams(),
                insights,
                state.config(),
                scores
        );

        List<DailyPlan> deadlinePlans = new DeadlineBasedStrategy().generateSchedule(request);
        List<DailyPlan> balancedPlans = new BalancedWorkloadStrategy().generateSchedule(request);
        List<DailyPlan> energyPlans = new EnergyAwareStrategy().generateSchedule(request);

        assertTrue(deadlinePlans.stream()
                .allMatch(plan -> plan.totalHours() <= state.config().schedulingConfig().maxHoursPerDay()));

        assertTrue(balancedPlans.stream()
                .filter(plan -> !plan.sessions().isEmpty())
                .count() >= 2);

        assertTrue(energyPlans.stream()
                .flatMap(plan -> plan.sessions().stream())
                .anyMatch(session -> state.student()
                        .productivityProfile()
                        .preferredWindows()
                        .getFirst()
                        .contains(session.start().toLocalTime())));
    }

    @Test
    void sessionsOnSameDayStartAfterPreviousSessionPlusBreakOnly() {
        var state = TestDataFactory.sampleState();
        var insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());

        var context = new PriorityContext(
                LocalDateTime.now(),
                insights.avoidedTaskIds(),
                insights.bestWindow(),
                state.config().priorityWeights()
        );

        Map<String, PriorityScore> scores = state.tasks().stream()
                .collect(Collectors.toMap(Task::id, task -> priorityEngine.scoreTask(task, context)));

        SchedulingRequest request = new SchedulingRequest(
                LocalDateTime.now(),
                state.student(),
                state.courses(),
                state.tasks(),
                state.exams(),
                insights,
                state.config(),
                scores
        );

        List<DailyPlan> plans = new DeadlineBasedStrategy().generateSchedule(request);

        List<StudySession> sessionsOnSameDay = plans.stream()
                .filter(plan -> plan.sessions().size() >= 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected at least one day with multiple study sessions"))
                .sessions()
                .stream()
                .sorted(Comparator.comparing(StudySession::start))
                .toList();

        StudySession firstSession = sessionsOnSameDay.get(0);
        StudySession secondSession = sessionsOnSameDay.get(1);

        long actualGapMinutes = Duration.between(firstSession.end(), secondSession.start()).toMinutes();
        long expectedBreakMinutes = state.config().schedulingConfig().breakMinutes();

        assertEquals(
                expectedBreakMinutes,
                actualGapMinutes,
                "The second session should start after only the configured break, not after an extra full session duration."
        );
    }
}