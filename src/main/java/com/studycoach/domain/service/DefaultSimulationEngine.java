package com.studycoach.domain.service;

import com.studycoach.domain.model.AppConfig;
import com.studycoach.domain.model.SchedulingConfig;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultSimulationEngine implements SimulationEngine {
    private final BehaviorAnalyzer behaviorAnalyzer;
    private final PriorityScoringPolicy priorityEngine;
    private final Map<String, SchedulingStrategy> strategies;

    public DefaultSimulationEngine(
            BehaviorAnalyzer behaviorAnalyzer,
            PriorityScoringPolicy priorityEngine,
            List<SchedulingStrategy> strategies
    ) {
        this.behaviorAnalyzer = behaviorAnalyzer;
        this.priorityEngine = priorityEngine;
        this.strategies = strategies.stream().collect(Collectors.toMap(SchedulingStrategy::name, Function.identity()));
    }

    @Override
    public SimulationResult simulate(SimulationRequest request) {
        ScenarioOutcome baseline = runScenario("Baseline", request.state(), request.now(), request.baselineStrategy());
        List<ScenarioOutcome> scenarios = new ArrayList<>();

        for (SimulationAction action : request.actions()) {
            StudyCoachState modifiedState = applyAction(request.state(), action);
            String strategyName = action.strategyName() == null || action.strategyName().isBlank()
                    ? request.baselineStrategy()
                    : action.strategyName();
            scenarios.add(runScenario(action.label(), modifiedState, request.now(), strategyName));
        }

        ScenarioOutcome best = scenarios.stream()
                .min(Comparator.comparingDouble(ScenarioOutcome::deadlineRiskPercent)
                        .thenComparingDouble(ScenarioOutcome::overloadRiskPercent))
                .orElse(baseline);

        return new SimulationResult(baseline, scenarios, best);
    }

    private ScenarioOutcome runScenario(String name, StudyCoachState state, LocalDateTime now, String strategyName) {
        var insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        var context = new PriorityContext(now, insights.avoidedTaskIds(), insights.bestWindow(), state.config().priorityWeights());
        var scores = state.tasks().stream().collect(Collectors.toMap(Task::id, task -> priorityEngine.scoreTask(task, context)));
        var request = new SchedulingRequest(now, state.student(), state.courses(), state.tasks(), state.exams(), insights, state.config(), scores);
        List<com.studycoach.domain.model.DailyPlan> plans = strategies.getOrDefault(strategyName, strategies.values().iterator().next()).generateSchedule(request);

        double deadlineRisk = plans.stream().mapToDouble(com.studycoach.domain.model.DailyPlan::deadlineRiskPercent).average().orElse(0.0);
        double maxHours = state.config().schedulingConfig().maxHoursPerDay();
        double overloadRisk = plans.stream()
                .mapToDouble(plan -> maxHours == 0 ? 100.0 : Math.max(0, (plan.totalHours() / maxHours) * 100.0 - 100.0))
                .average()
                .orElse(0.0);
        double stability = Math.max(0.0, 100.0 - ((deadlineRisk * 0.6) + (overloadRisk * 0.4)));
        String recommendation = deadlineRisk > 60
                ? "Protect near-term deadlines with shorter, higher-frequency sessions."
                : overloadRisk > 25
                ? "Reduce daily load or extend the horizon to preserve consistency."
                : "This scenario is resilient and keeps buffer for recovery.";

        return new ScenarioOutcome(name, deadlineRisk, overloadRisk, stability, recommendation, plans);
    }

    private StudyCoachState applyAction(StudyCoachState baseState, SimulationAction action) {
        List<Task> adjustedTasks = baseState.tasks().stream()
                .map(task -> action.delayTaskByDays() > 0 && task.status() != com.studycoach.domain.model.TaskStatus.COMPLETED
                        ? new Task(task.id(), task.courseId(), task.title(), task.description(), task.difficulty(), task.importance(),
                        task.estimatedHours(), task.completedHours(), task.deadline().plusDays(action.delayTaskByDays()), task.status(), task.lastUpdated())
                        : task)
                .toList();

        SchedulingConfig schedulingConfig = new SchedulingConfig(
                Math.max(1.0, baseState.config().schedulingConfig().maxHoursPerDay() + action.extraHoursPerDay()),
                baseState.config().schedulingConfig().sessionMinutes(),
                baseState.config().schedulingConfig().breakMinutes(),
                baseState.config().schedulingConfig().planningHorizonDays()
        );
        if (action.skipToday()) {
            schedulingConfig = new SchedulingConfig(
                    Math.max(1.0, schedulingConfig.maxHoursPerDay() - 2),
                    schedulingConfig.sessionMinutes(),
                    schedulingConfig.breakMinutes(),
                    schedulingConfig.planningHorizonDays()
            );
        }

        AppConfig config = new AppConfig(baseState.config().priorityWeights(), schedulingConfig, baseState.config().simulationConfig(), baseState.config().courseColors());
        return new StudyCoachState(baseState.student(), baseState.courses(), adjustedTasks, baseState.exams(), baseState.studyHistory(), baseState.generatedPlans(), config);
    }
}
