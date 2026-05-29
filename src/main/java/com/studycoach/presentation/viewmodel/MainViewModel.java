package com.studycoach.presentation.viewmodel;

import com.studycoach.application.dto.AnalyticsSnapshot;
import com.studycoach.application.dto.DashboardSnapshot;
import com.studycoach.application.dto.FocusDashboardSnapshot;
import com.studycoach.application.dto.FocusInsightsSnapshot;
import com.studycoach.application.dto.ScheduleSnapshot;
import com.studycoach.application.dto.SimulationSnapshot;
import com.studycoach.application.dto.TaskBoardSnapshot;
import com.studycoach.application.exception.StudyCoachValidationException;
import com.studycoach.application.service.StudyCoachService;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.FocusReflection;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.TaskStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MainViewModel {
    private final StudyCoachService service;
    private final StringProperty bannerText = new SimpleStringProperty();
    private TaskBoardSnapshot taskBoardSnapshot;
    private ScheduleSnapshot scheduleSnapshot;
    private FocusDashboardSnapshot focusDashboardSnapshot;
    private FocusInsightsSnapshot focusInsightsSnapshot;
    private SimulationSnapshot simulationSnapshot;

    public MainViewModel(StudyCoachService service) {
        this.service = service;
        refreshAll();
    }

    public void refreshAll() {
        taskBoardSnapshot = service.getTaskBoardSnapshot();
        scheduleSnapshot = service.getScheduleSnapshot();
        focusDashboardSnapshot = service.getFocusDashboardSnapshot();
        focusInsightsSnapshot = service.getFocusInsightsSnapshot();
        simulationSnapshot = service.runSimulation("Baseline compare", 1, 0, "Energy Aware", false);
        if (bannerText.get() == null || bannerText.get().isBlank()) {
            bannerText.set("Welcome back. Choose one thing. Start small.");
        }
    }

    public FocusDashboardSnapshot focusDashboard() {
        return focusDashboardSnapshot;
    }

    public TaskBoardSnapshot taskBoard() {
        return taskBoardSnapshot;
    }

    public ScheduleSnapshot schedule() {
        return scheduleSnapshot;
    }

    public FocusInsightsSnapshot focusInsights() {
        return focusInsightsSnapshot;
    }

    public SimulationSnapshot simulation() {
        return simulationSnapshot;
    }

    public StringProperty bannerTextProperty() {
        return bannerText;
    }

    public void generateSchedule(String strategyName) {
        runOrBanner(() -> service.generateSchedule(strategyName));
    }

    public void addTask(String courseId, String title, LocalDateTime deadline, double hours, int difficulty, int importance) {
        runOrBanner(() -> service.addTask(courseId, title, deadline, hours, difficulty, importance));
    }

    public void editTaskTitle(String taskId, String title) {
        service.editTaskTitle(taskId, title);
        refreshAll();
    }

    public void updateTask(String taskId, String courseId, String title, String description, LocalDateTime deadline, double hours, int difficulty, int importance) {
        runOrBanner(() -> service.updateTask(taskId, courseId, title, description, deadline, hours, difficulty, importance));
    }

    public void deleteTask(String taskId) {
        runOrBanner(() -> service.deleteTask(taskId));
    }

    public void markTask(String taskId, TaskStatus status, double completedHours) {
        runOrBanner(() -> service.updateTaskProgress(taskId, status, completedHours));
    }

    public String completeFocusSession(String taskId, int minutes) {
        String sessionId = service.completeFocusSession(taskId, minutes);
        refreshAll();
        return sessionId;
    }

    public void applyFocusReflection(String sessionId, FocusReflection reflection) {
        service.applyFocusReflection(sessionId, reflection);
        refreshAll();
    }

    public void moveSession(String sessionId, LocalDateTime targetStart) {
        runOrBanner(() -> service.moveSession(sessionId, targetStart));
    }

    public void runSimulation(String label, int extraHours, int delayDays, String strategyName, boolean skipToday) {
        simulationSnapshot = service.runSimulation(label, extraHours, delayDays, strategyName, skipToday);
    }

    public List<String> strategyNames() {
        return service.getStrategyNames();
    }

    public ObservableList<Course> courses() {
        return FXCollections.observableArrayList(service.getCourses());
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
        runOrBanner(() -> service.updateStudentProfile(name, age, maxHoursPerDay, availabilityStart, availabilityEnd, preferredStart, preferredEnd));
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
        runOrBanner(() -> service.clearAndInitializeWorkspace(name, age, maxHoursPerDay, availabilityStart, availabilityEnd, preferredStart, preferredEnd));
    }

    public void addCourse(String name, String colorHex) {
        runOrBanner(() -> service.addCourse(name, colorHex));
    }

    public void addExam(String courseId, String title, LocalDateTime startTime, int importance, double syllabusWeight) {
        runOrBanner(() -> service.addExam(courseId, title, startTime, importance, syllabusWeight));
    }

    public StudyCoachState state() {
        return service.getState();
    }

    public void postBanner(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        bannerText.set(message);
    }

    private void runOrBanner(Runnable action) {
        try {
            action.run();
            refreshAll();
        } catch (StudyCoachValidationException exception) {
            postBanner(exception.getMessage());
        }
    }
}
