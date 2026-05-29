package com.studycoach.application.service;

import com.studycoach.application.dto.AnalyticsSnapshot;
import com.studycoach.application.dto.DashboardSnapshot;
import com.studycoach.application.dto.FocusDashboardSnapshot;
import com.studycoach.application.dto.FocusInsightsSnapshot;
import com.studycoach.application.dto.ScheduleSnapshot;
import com.studycoach.application.dto.SimulationSnapshot;
import com.studycoach.application.dto.TaskBoardSnapshot;
import com.studycoach.application.exception.DuplicateCourseException;
import com.studycoach.application.exception.InvalidDeadlineException;
import com.studycoach.application.exception.InvalidTaskFieldsException;
import com.studycoach.application.exception.UnknownCourseException;
import com.studycoach.application.exception.UnknownTaskException;
import com.studycoach.application.port.StudyCoachRepository;
import com.studycoach.domain.model.AppConfig;
import com.studycoach.domain.model.BehaviorInsights;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.DailyPlan;
import com.studycoach.domain.model.FocusReflection;
import com.studycoach.domain.model.PriorityLevel;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.StudySession;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.TaskStatus;
import com.studycoach.domain.model.TimeWindow;
import com.studycoach.domain.model.ProductivityProfile;
import com.studycoach.domain.model.BehaviorPattern;
import com.studycoach.domain.model.SessionType;
import com.studycoach.domain.observer.TaskUpdateEvent;
import com.studycoach.domain.service.BalancedWorkloadStrategy;
import com.studycoach.domain.service.BehaviorAnalyzer;
import com.studycoach.domain.service.DefaultSimulationEngine;
import com.studycoach.domain.service.DeadlineBasedStrategy;
import com.studycoach.domain.service.EnergyAwareStrategy;
import com.studycoach.domain.service.InMemoryTaskEventPublisher;
import com.studycoach.domain.service.PriorityContext;
import com.studycoach.domain.service.PriorityEngine;
import com.studycoach.domain.service.PriorityScore;
import com.studycoach.domain.service.ScenarioOutcome;
import com.studycoach.domain.service.SchedulingRequest;
import com.studycoach.domain.service.SchedulingStrategy;
import com.studycoach.domain.service.SimulationAction;
import com.studycoach.domain.service.SimulationRequest;
import com.studycoach.domain.service.SimulationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudyCoachService {
    private static final DateTimeFormatter TIME_RANGE = DateTimeFormatter.ofPattern("HH:mm");

    private final StudyCoachRepository repository;
    private final BehaviorAnalyzer behaviorAnalyzer;
    private final PriorityEngine priorityEngine;
    private final Map<String, SchedulingStrategy> strategies;
    private final DefaultSimulationEngine simulationEngine;
    private final InMemoryTaskEventPublisher taskEventPublisher;
    private final ProgressTracker progressTracker;
    private final Rescheduler rescheduler;

    public StudyCoachService(StudyCoachRepository repository) {
        this.repository = repository;
        this.behaviorAnalyzer = new BehaviorAnalyzer();
        this.priorityEngine = new PriorityEngine();
        List<SchedulingStrategy> strategyList = List.of(
                new DeadlineBasedStrategy(),
                new BalancedWorkloadStrategy(),
                new EnergyAwareStrategy()
        );
        this.strategies = strategyList.stream().collect(Collectors.toMap(SchedulingStrategy::name, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        this.simulationEngine = new DefaultSimulationEngine(behaviorAnalyzer, priorityEngine, strategyList);
        this.taskEventPublisher = new InMemoryTaskEventPublisher();
        this.progressTracker = new ProgressTracker();
        this.rescheduler = new Rescheduler();
        taskEventPublisher.register(progressTracker);
        taskEventPublisher.register(rescheduler);
        repository.initializeIfMissing();
        ensureScheduleExists();
    }

    public DashboardSnapshot getDashboardSnapshot() {
        StudyCoachState state = repository.load();
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        DailyPlan today = state.generatedPlans().stream()
                .filter(plan -> plan.date().equals(LocalDate.now()))
                .findFirst()
                .orElseGet(() -> generateAndSaveSchedule(state, "Energy Aware").stream().findFirst().orElse(null));

        List<String> todaysPlan = today == null
                ? List.of("No sessions generated yet.")
                : today.sessions().stream()
                .map(session -> session.title() + "  " + TIME_RANGE.format(session.start()) + "-" + TIME_RANGE.format(session.end()))
                .toList();

        double completionRate = state.tasks().stream()
                .mapToDouble(task -> task.estimatedHours() == 0 ? 0 : task.completedHours() / task.estimatedHours())
                .average()
                .orElse(0.0) * 100.0;

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Best focus window: " + insights.bestWindow().start());
        recommendations.add(insights.narrative());
        state.tasks().stream()
                .filter(Task::isActive)
                .min(Comparator.comparing(Task::deadline))
                .ifPresent(task -> recommendations.add("Most urgent: " + task.title() + " due " + task.deadline().toLocalDate()));

        return new DashboardSnapshot(
                state.student().name(),
                todaysPlan,
                "Completion " + Math.round(completionRate) + "% across " + state.tasks().size() + " tasks",
                today == null ? 0.0 : today.deadlineRiskPercent(),
                recommendations,
                insights.narrative()
        );
    }

    public TaskBoardSnapshot getTaskBoardSnapshot() {
        StudyCoachState state = repository.load();
        Map<String, PriorityScore> scores = buildScores(state, LocalDateTime.now());
        Map<String, String> courseLookup = state.courses().stream().collect(Collectors.toMap(Course::id, Course::name));
        List<TaskBoardSnapshot.TaskCard> tasks = state.tasks().stream()
                .sorted(Comparator.comparing((Task task) -> scores.get(task.id()).score()).reversed())
                .map(task -> new TaskBoardSnapshot.TaskCard(
                        task.id(),
                        courseLookup.getOrDefault(task.courseId(), task.courseId()),
                        task.title(),
                        task.deadline(),
                        task.remainingHours(),
                        task.status(),
                        scores.get(task.id()).level(),
                        scores.get(task.id()).score()
                ))
                .toList();
        List<String> timeline = tasks.stream()
                .sorted(Comparator.comparing(TaskBoardSnapshot.TaskCard::deadline))
                .limit(8)
                .map(card -> card.deadline().toLocalDate() + "  " + card.title())
                .toList();
        return new TaskBoardSnapshot(tasks, timeline);
    }

    public ScheduleSnapshot getScheduleSnapshot() {
        StudyCoachState state = repository.load();
        Map<String, String> colors = state.config().courseColors();
        List<ScheduleSnapshot.DayPlanView> days = state.generatedPlans().stream()
                .map(plan -> new ScheduleSnapshot.DayPlanView(
                        plan.date(),
                        plan.sessions().stream().map(session -> new ScheduleSnapshot.SessionView(
                                session.id(),
                                session.taskId(),
                                session.courseId(),
                                session.title(),
                                session.start(),
                                session.end(),
                                TIME_RANGE.format(session.start()) + " - " + TIME_RANGE.format(session.end()),
                                colors.getOrDefault(session.courseId(), "#4E6AF3")
                        )).toList()
                ))
                .toList();
        return new ScheduleSnapshot(days);
    }

    public AnalyticsSnapshot getAnalyticsSnapshot() {
        StudyCoachState state = repository.load();
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());

        List<AnalyticsSnapshot.Point> productivity = insights.productivityByHour().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsSnapshot.Point(String.valueOf(entry.getKey()), entry.getValue()))
                .toList();

        Map<String, String> courseLookup = state.courses().stream().collect(Collectors.toMap(Course::id, Course::name));
        List<AnalyticsSnapshot.Point> distribution = state.tasks().stream()
                .collect(Collectors.groupingBy(Task::courseId, Collectors.summingDouble(Task::estimatedHours)))
                .entrySet().stream()
                .map(entry -> new AnalyticsSnapshot.Point(courseLookup.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue()))
                .toList();

        List<String> insightCards = List.of(
                insights.narrative(),
                "Avoided tasks: " + insights.avoidedTaskIds().size(),
                "Strongest daily window: " + insights.bestWindow().start() + " - " + insights.bestWindow().end()
        );

        return new AnalyticsSnapshot(productivity, distribution, insightCards);
    }

    public FocusDashboardSnapshot getFocusDashboardSnapshot() {
        StudyCoachState state = repository.load();
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        Map<String, PriorityScore> scores = buildScores(state, LocalDateTime.now());

        Task recommended = state.tasks().stream()
                .filter(Task::isActive)
                .max(Comparator.comparingDouble(task -> {
                    PriorityScore score = scores.get(task.id());
                    return score == null ? 0.0 : score.score();
                }))
                .orElse(null);

        Map<String, String> courseLookup = state.courses().stream().collect(Collectors.toMap(Course::id, Course::name));
        String courseName = recommended == null ? null : courseLookup.getOrDefault(recommended.courseId(), recommended.courseId());
        java.time.LocalDate dueDate = recommended == null ? null : recommended.deadline().toLocalDate();
        String targetLine = recommended == null
                ? "No focus targets yet. Add one to begin."
                : recommended.title() + "  •  due " + dueDate;

        String windowLine = insights.bestWindow() == null
                ? "Not enough data yet"
                : insights.bestWindow().start() + " - " + insights.bestWindow().end();

        FocusReflectionSummary reflectionSummary = summarizeReflections(state);
        String lastReflection = reflectionSummary.lastReflectionMessage();

        List<String> coachNotes = List.of(
                "Your next deep work target: " + targetLine,
                "Best focus window: " + windowLine,
                insights.narrative(),
                "Avoided targets this week: " + insights.avoidedTaskIds().size(),
                "Build the streak gently. Choose one task. Protect one session."
        ).stream().filter(Objects::nonNull).toList();

        return new FocusDashboardSnapshot(
                state.student().name(),
                calculateFocusStreakDays(state),
                countWeeklyFocusSessions(state),
                countCompletedFocusSessions(state),
                targetLine,
                recommended == null ? null : recommended.title(),
                courseName,
                dueDate,
                windowLine,
                lastReflection,
                coachNotes
        );
    }

    public FocusInsightsSnapshot getFocusInsightsSnapshot() {
        StudyCoachState state = repository.load();
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());

        String windowLine = insights.bestWindow() == null
                ? "Not enough data yet"
                : insights.bestWindow().start() + " - " + insights.bestWindow().end();

        FocusReflectionSummary reflectionSummary = summarizeReflections(state);

        Map<String, String> courseLookup = state.courses().stream().collect(Collectors.toMap(Course::id, Course::name));
        List<AnalyticsSnapshot.Point> distribution = state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .collect(Collectors.groupingBy(StudySession::courseId, Collectors.summingDouble(StudySession::durationHours)))
                .entrySet().stream()
                .map(entry -> new AnalyticsSnapshot.Point(courseLookup.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparingDouble(AnalyticsSnapshot.Point::value).reversed())
                .toList();

        List<String> avoidedTitles = state.tasks().stream()
                .filter(task -> insights.avoidedTaskIds().contains(task.id()))
                .map(Task::title)
                .sorted()
                .limit(8)
                .toList();

        List<String> coachNotes = List.of(
                insights.narrative(),
                "Strongest window: " + windowLine,
                "If distracted repeats, shorten sessions and reduce task difficulty for one week."
        );

        return new FocusInsightsSnapshot(
                calculateFocusStreakDays(state),
                countWeeklyFocusSessions(state),
                countCompletedFocusSessions(state),
                windowLine,
                reflectionSummary.counts(),
                distribution,
                avoidedTitles,
                coachNotes
        );
    }

    public SimulationSnapshot runSimulation(String label, int extraHours, int delayDays, String strategyName, boolean skipToday) {
        StudyCoachState state = repository.load();
        SimulationResult result = simulationEngine.simulate(new SimulationRequest(
                state,
                LocalDateTime.now(),
                strategyName,
                List.of(new SimulationAction(label, extraHours, delayDays, strategyName, skipToday))
        ));

        List<SimulationSnapshot.ScenarioCard> scenarios = result.scenarios().stream()
                .map(outcome -> new SimulationSnapshot.ScenarioCard(
                        outcome.name(),
                        outcome.deadlineRiskPercent(),
                        outcome.overloadRiskPercent(),
                        outcome.stabilityScore(),
                        outcome.recommendation()
                )).toList();

        return new SimulationSnapshot(
                result.baseline().deadlineRiskPercent(),
                result.baseline().overloadRiskPercent(),
                scenarios,
                "Recommended scenario: " + result.bestScenario().name() + " -> " + result.bestScenario().recommendation()
        );
    }

    public void generateSchedule(String strategyName) {
        StudyCoachState state = repository.load();
        generateAndSaveSchedule(state, strategyName);
    }

    public void addTask(String courseId, String title, LocalDateTime deadline, double hours, int difficulty, int importance) {
        StudyCoachState state = repository.load();
        validateCourseExists(state, courseId);
        validateTaskFields(title, deadline, hours, difficulty, importance);
        List<Task> updated = new ArrayList<>(state.tasks());
        updated.add(new Task(
                UUID.randomUUID().toString(),
                courseId,
                title,
                "Added from Task Management screen",
                difficulty,
                importance,
                hours,
                0.0,
                deadline,
                TaskStatus.PARTIAL,
                LocalDateTime.now()
        ));
        repository.save(state.withTasks(updated));
        generateSchedule("Balanced Workload");
    }

    public void updateTask(String taskId, String courseId, String title, String description, LocalDateTime deadline, double hours, int difficulty, int importance) {
        StudyCoachState state = repository.load();
        ensureTaskExists(state, taskId);
        validateCourseExists(state, courseId);
        validateTaskFields(title, deadline, hours, difficulty, importance);
        List<Task> updated = state.tasks().stream()
                .map(task -> task.id().equals(taskId)
                        ? new Task(task.id(), courseId, title, description, difficulty, importance, hours,
                        Math.min(task.completedHours(), hours), deadline, task.status(), LocalDateTime.now())
                        : task)
                .toList();
        repository.save(state.withTasks(updated));
        generateSchedule("Balanced Workload");
    }

    public void editTaskTitle(String taskId, String updatedTitle) {
        StudyCoachState state = repository.load();
        List<Task> updated = state.tasks().stream()
                .map(task -> task.id().equals(taskId)
                        ? new Task(task.id(), task.courseId(), updatedTitle, task.description(), task.difficulty(), task.importance(),
                        task.estimatedHours(), task.completedHours(), task.deadline(), task.status(), LocalDateTime.now())
                        : task)
                .toList();
        repository.save(state.withTasks(updated));
    }

    public void deleteTask(String taskId) {
        StudyCoachState state = repository.load();
        ensureTaskExists(state, taskId);
        repository.save(state.withTasks(state.tasks().stream().filter(task -> !task.id().equals(taskId)).toList()));
        generateSchedule("Balanced Workload");
    }

    public void updateTaskProgress(String taskId, TaskStatus status, double completedHoursDelta) {
        StudyCoachState state = repository.load();
        ensureTaskExists(state, taskId);
        List<Task> updatedTasks = state.tasks().stream()
                .map(task -> task.id().equals(taskId) ? task.updateProgress(completedHoursDelta, status, LocalDateTime.now()) : task)
                .toList();
        StudyCoachState updatedState = state.withTasks(updatedTasks);
        repository.save(updatedState);
        taskEventPublisher.publish(new TaskUpdateEvent(taskId, status, completedHoursDelta, LocalDateTime.now()));
        if (rescheduler.consumeRescheduleRequest()) {
            generateAndSaveSchedule(updatedState, "Energy Aware");
        }
    }

    public void moveSession(String sessionId, LocalDateTime targetStart) {
        StudyCoachState state = repository.load();
        List<DailyPlan> updatedPlans = state.generatedPlans().stream()
                .map(plan -> new DailyPlan(
                        plan.date(),
                        plan.sessions().stream().map(session -> session.id().equals(sessionId)
                                ? session.moveTo(targetStart, targetStart.plusMinutes((long) (session.durationHours() * 60)))
                                : session)
                                .toList(),
                        plan.totalHours(),
                        plan.riskAssessment(),
                        plan.deadlineRiskPercent(),
                        plan.recommendation()
                ))
                .toList();
        repository.save(new StudyCoachState(state.student(), state.courses(), state.tasks(), state.exams(), state.studyHistory(), updatedPlans, state.config()));
    }

    public String completeFocusSession(String taskId, int minutes) {
        StudyCoachState state = repository.load();
        Task task = state.tasks().stream()
                .filter(candidate -> candidate.id().equals(taskId))
                .findFirst()
                .orElse(null);
        if (task == null) {
            return null;
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(Math.max(5, minutes));
        double completedHours = Math.max(0.1, minutes / 60.0);

        List<Task> updatedTasks = state.tasks().stream()
                .map(candidate -> candidate.id().equals(taskId)
                        ? candidate.updateProgress(completedHours, TaskStatus.PARTIAL, end)
                        : candidate)
                .toList();
        List<StudySession> updatedHistory = new ArrayList<>(state.studyHistory());
        String sessionId = UUID.randomUUID().toString();
        updatedHistory.add(new StudySession(
                sessionId,
                task.id(),
                task.courseId(),
                task.title(),
                start,
                end,
                SessionType.FOCUS,
                Math.max(1.0, task.importance() * 2.0),
                true,
                FocusReflection.NONE
        ));

        StudyCoachState updatedState = new StudyCoachState(
                state.student(),
                state.courses(),
                updatedTasks,
                state.exams(),
                updatedHistory,
                state.generatedPlans(),
                state.config()
        );
        repository.save(updatedState);
        taskEventPublisher.publish(new TaskUpdateEvent(taskId, TaskStatus.PARTIAL, completedHours, end));
        generateAndSaveSchedule(updatedState, "Energy Aware");
        return sessionId;
    }

    public void applyFocusReflection(String sessionId, FocusReflection reflection) {
        if (sessionId == null || reflection == null) {
            return;
        }
        StudyCoachState state = repository.load();
        List<StudySession> updatedHistory = state.studyHistory().stream()
                .map(session -> session.id().equals(sessionId)
                        ? new StudySession(
                                session.id(),
                                session.taskId(),
                                session.courseId(),
                                session.title(),
                                session.start(),
                                session.end(),
                                session.sessionType(),
                                session.priorityScore(),
                                session.completed(),
                                reflection
                        )
                        : session)
                .toList();
        repository.save(state.withHistory(updatedHistory));
    }

    private long countWeeklyFocusSessions(StudyCoachState state) {
        LocalDate today = LocalDate.now();
        return state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .filter(session -> !session.start().toLocalDate().isBefore(today.minusDays(6)))
                .count();
    }

    private long countCompletedFocusSessions(StudyCoachState state) {
        return state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .count();
    }

    private int calculateFocusStreakDays(StudyCoachState state) {
        List<LocalDate> days = state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .map(session -> session.start().toLocalDate())
                .distinct()
                .sorted()
                .toList();
        if (days.isEmpty()) {
            return 0;
        }

        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor) && days.contains(cursor.minusDays(1))) {
            cursor = cursor.minusDays(1);
        }

        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private FocusReflectionSummary summarizeReflections(StudyCoachState state) {
        Map<FocusReflection, Long> counts = state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .collect(Collectors.groupingBy(StudySession::reflection, Collectors.counting()));

        StudySession latest = state.studyHistory().stream()
                .filter(session -> session.completed() && session.sessionType() == SessionType.FOCUS)
                .max(Comparator.comparing(StudySession::end))
                .orElse(null);

        String message = latest == null || latest.reflection() == FocusReflection.NONE
                ? "Reflect after your next session: Great / Okay / Distracted."
                : switch (latest.reflection()) {
                    case GREAT -> "Great. That session felt deep and clear.";
                    case OKAY -> "Okay. Solid session—habit intact.";
                    case DISTRACTED -> "Distracted. Next time: shorter sprint, smaller target.";
                    case NONE -> "Reflect after your next session: Great / Okay / Distracted.";
                };

        return new FocusReflectionSummary(counts, message);
    }

    private record FocusReflectionSummary(Map<FocusReflection, Long> counts, String lastReflectionMessage) {
    }

    public void updateStudentProfile(
            String name,
            int age,
            double maxHoursPerDay,
            LocalTime availabilityStart,
            LocalTime availabilityEnd,
            LocalTime preferredStart,
            LocalTime preferredEnd
    ) {
        StudyCoachState state = repository.load();
        var availability = List.of(new TimeWindow(availabilityStart, availabilityEnd));
        var profile = new ProductivityProfile(List.of(new TimeWindow(preferredStart, preferredEnd)), derivePattern(preferredStart));
        var student = new com.studycoach.domain.model.Student(state.student().id(), name, age, maxHoursPerDay, availability, profile);
        AppConfig config = new AppConfig(
                state.config().priorityWeights(),
                new com.studycoach.domain.model.SchedulingConfig(
                        maxHoursPerDay,
                        state.config().schedulingConfig().sessionMinutes(),
                        state.config().schedulingConfig().breakMinutes(),
                        state.config().schedulingConfig().planningHorizonDays()
                ),
                state.config().simulationConfig(),
                state.config().courseColors()
        );
        repository.save(new StudyCoachState(student, state.courses(), state.tasks(), state.exams(), state.studyHistory(), state.generatedPlans(), config));
        generateSchedule("Energy Aware");
    }

    public void clearAndInitializeWorkspace(
            String name,
            int age,
            double maxHoursPerDay,
            LocalTime availabilityStart,
            LocalTime availabilityEnd,
            LocalTime preferredStart,
            LocalTime preferredEnd
    ) {
        StudyCoachState state = repository.load();
        var availability = List.of(new TimeWindow(availabilityStart, availabilityEnd));
        var profile = new ProductivityProfile(List.of(new TimeWindow(preferredStart, preferredEnd)), derivePattern(preferredStart));
        var student = new com.studycoach.domain.model.Student(
                UUID.randomUUID().toString(),
                name,
                age,
                maxHoursPerDay,
                availability,
                profile
        );
        AppConfig config = new AppConfig(
                state.config().priorityWeights(),
                new com.studycoach.domain.model.SchedulingConfig(
                        maxHoursPerDay,
                        state.config().schedulingConfig().sessionMinutes(),
                        state.config().schedulingConfig().breakMinutes(),
                        state.config().schedulingConfig().planningHorizonDays()
                ),
                state.config().simulationConfig(),
                Map.of()
        );
        repository.save(new StudyCoachState(student, List.of(), List.of(), List.of(), List.of(), List.of(), config));
        generateSchedule("Balanced Workload");
    }

    public void addCourse(String name, String colorHex) {
        StudyCoachState state = repository.load();
        if (name == null || name.isBlank()) {
            throw new InvalidTaskFieldsException("Course name is required.");
        }
        String id = slugify(name);
        if (id.isBlank()) {
            throw new InvalidTaskFieldsException("Course name must contain letters or numbers.");
        }
        List<Course> updatedCourses = new ArrayList<>(state.courses());
        if (updatedCourses.stream().anyMatch(course -> course.id().equals(id))) {
            throw new DuplicateCourseException(id);
        }
        updatedCourses.add(new Course(id, name, colorHex));
        Map<String, String> colors = new java.util.LinkedHashMap<>(state.config().courseColors());
        colors.put(id, colorHex);
        AppConfig config = new AppConfig(state.config().priorityWeights(), state.config().schedulingConfig(), state.config().simulationConfig(), colors);
        repository.save(new StudyCoachState(state.student(), updatedCourses, state.tasks(), state.exams(), state.studyHistory(), state.generatedPlans(), config));
    }

    public void addExam(String courseId, String title, LocalDateTime startTime, int importance, double syllabusWeight) {
        StudyCoachState state = repository.load();
        validateCourseExists(state, courseId);
        if (title == null || title.isBlank()) {
            throw new InvalidTaskFieldsException("Exam title is required.");
        }
        if (startTime == null || !startTime.isAfter(LocalDateTime.now())) {
            throw new InvalidDeadlineException(startTime);
        }
        List<com.studycoach.domain.model.Exam> updated = new ArrayList<>(state.exams());
        updated.add(new com.studycoach.domain.model.Exam(UUID.randomUUID().toString(), courseId, title, startTime, importance, syllabusWeight));
        repository.save(new StudyCoachState(state.student(), state.courses(), state.tasks(), updated, state.studyHistory(), state.generatedPlans(), state.config()));
        generateSchedule("Deadline Based");
    }

    public List<Course> getCourses() {
        return repository.load().courses();
    }

    public StudyCoachState getState() {
        return repository.load();
    }

    public List<String> getStrategyNames() {
        return new ArrayList<>(strategies.keySet());
    }

    private void ensureScheduleExists() {
        StudyCoachState state = repository.load();
        if (state.generatedPlans().isEmpty()) {
            generateAndSaveSchedule(state, "Energy Aware");
        }
    }

    private List<DailyPlan> generateAndSaveSchedule(StudyCoachState state, String strategyName) {
        // Scheduling stays centralized here so the UI never talks directly to engines or persistence.
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        Map<String, PriorityScore> scores = buildScores(state, LocalDateTime.now());
        SchedulingRequest request = new SchedulingRequest(LocalDateTime.now(), state.student(), state.courses(), state.tasks(), state.exams(), insights, state.config(), scores);
        List<DailyPlan> plans = strategies.getOrDefault(strategyName, strategies.values().iterator().next()).generateSchedule(request);
        repository.save(state.withGeneratedPlans(plans));
        return plans;
    }

    private Map<String, PriorityScore> buildScores(StudyCoachState state, LocalDateTime now) {
        BehaviorInsights insights = behaviorAnalyzer.analyze(state.tasks(), state.studyHistory());
        PriorityContext context = new PriorityContext(now, insights.avoidedTaskIds(), insights.bestWindow(), state.config().priorityWeights());
        return state.tasks().stream().collect(Collectors.toMap(Task::id, task -> priorityEngine.scoreTask(task, context)));
    }

    private BehaviorPattern derivePattern(LocalTime preferredStart) {
        if (preferredStart.isBefore(LocalTime.NOON)) {
            return BehaviorPattern.PEAK_MORNING;
        }
        if (preferredStart.isBefore(LocalTime.of(17, 0))) {
            return BehaviorPattern.PEAK_AFTERNOON;
        }
        return BehaviorPattern.PEAK_EVENING;
    }

    private String slugify(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private void validateCourseExists(StudyCoachState state, String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new UnknownCourseException(String.valueOf(courseId));
        }
        boolean exists = state.courses().stream().anyMatch(course -> course.id().equals(courseId));
        if (!exists) {
            throw new UnknownCourseException(courseId);
        }
    }

    private void ensureTaskExists(StudyCoachState state, String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new UnknownTaskException(String.valueOf(taskId));
        }
        boolean exists = state.tasks().stream().anyMatch(task -> task.id().equals(taskId));
        if (!exists) {
            throw new UnknownTaskException(taskId);
        }
    }

    private void validateTaskFields(String title, LocalDateTime deadline, double hours, int difficulty, int importance) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskFieldsException("Task title is required.");
        }
        if (deadline == null || !deadline.isAfter(LocalDateTime.now())) {
            throw new InvalidDeadlineException(deadline);
        }
        if (hours <= 0) {
            throw new InvalidTaskFieldsException("Estimated hours must be greater than 0.");
        }
        if (difficulty < 1 || difficulty > 5) {
            throw new InvalidTaskFieldsException("Difficulty must be between 1 and 5.");
        }
        if (importance < 1 || importance > 5) {
            throw new InvalidTaskFieldsException("Importance must be between 1 and 5.");
        }
    }
}
