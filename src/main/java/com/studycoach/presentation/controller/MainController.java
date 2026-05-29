package com.studycoach.presentation.controller;

import com.studycoach.application.dto.FocusDashboardSnapshot;
import com.studycoach.application.dto.ScheduleSnapshot;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.DailyPlan;
import com.studycoach.domain.model.Exam;
import com.studycoach.domain.model.FocusReflection;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.TaskStatus;
import com.studycoach.domain.model.TimeWindow;
import com.studycoach.application.service.FocusAudioService;
import com.studycoach.presentation.viewmodel.MainViewModel;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;

public class MainController {
    private enum WorkspaceScreen {
    DASHBOARD("Home", "Choose one thing. Start small."),
    DEEP_WORK("Focus", "Protect one focus block. Reflect, then move on."),
    TASKS("Tasks", ""),
    SCHEDULE("Schedule", "");

    private final String title;
    private final String subtitle;

    WorkspaceScreen(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }
}


    @FXML private Button todayNavButton;
    @FXML private Button focusNavButton;
    @FXML private Button tasksNavButton;
    @FXML private Button scheduleNavButton;
    @FXML private Label bannerLabel;
    @FXML private Label studentLabel;
    @FXML private Label workspaceTitleLabel;
    @FXML private Label workspaceSubtitleLabel;
    @FXML private Label homeActiveTargetsLabel;
    @FXML private Label homeDueSoonLabel;
    @FXML private Label homeCompletedTargetsLabel;
    @FXML private Label homeBestWindowLabel;
    @FXML private ListView<String> homeDeadlinesList;
    @FXML private ScrollPane todayScreen;
    @FXML private ScrollPane tasksScreen;
    @FXML private ScrollPane scheduleScreen;
    @FXML private ScrollPane focusScreen;
    @FXML private StackPane profileOverlay;
    @FXML private Label todayHeadlineLabel;
    @FXML private Label nextFocusTaskLabel;
    @FXML private Label homeHeroKickerLabel;
    @FXML private Label homeHeroTaskLabel;
    @FXML private Label homeHeroMetaLabel;
    @FXML private Button homeStartFocusButton;
    @FXML private Button homeViewTasksButton;
    @FXML private Button homeAddTargetButton;
    @FXML private Label focusStreakLabel;
    @FXML private Label weeklyFocusLabel;
    @FXML private Label gardenStageLabel;
    @FXML private Label profileSummaryLabel;
    @FXML private Label lastReflectionLabel;
    @FXML private ListView<String> topPriorityList;
    @FXML private FlowPane profileCoursesPane;
    @FXML private ListView<String> todayPlanList;
    @FXML private Label progressLabel;
    @FXML private Label riskLabel;
    @FXML private ChoiceBox<String> strategyChoice;
    @FXML private GridPane scheduleGrid;
    @FXML private ComboBox<String> courseCombo;
    @FXML private Label selectedTaskLabel;
    @FXML private Label totalTargetsLabel;
    @FXML private Label dueSoonLabel;
    @FXML private Label completedTargetsLabel;
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionArea;
    @FXML private DatePicker taskDeadlineDatePicker;
    @FXML private Spinner<Integer> taskDeadlineHourSpinner;
    @FXML private Spinner<Integer> taskDeadlineMinuteSpinner;
    @FXML private ListView<String> timelineList;
    @FXML private Button generateButton;
    @FXML private TextField studentNameField;
    @FXML private Spinner<Integer> studentAgeSpinner;
    @FXML private Spinner<Integer> maxHoursSpinner;
    @FXML private Spinner<Integer> availabilityStartHourSpinner;
    @FXML private Spinner<Integer> availabilityStartMinuteSpinner;
    @FXML private Spinner<Integer> availabilityEndHourSpinner;
    @FXML private Spinner<Integer> availabilityEndMinuteSpinner;
    @FXML private Spinner<Integer> preferredStartHourSpinner;
    @FXML private Spinner<Integer> preferredStartMinuteSpinner;
    @FXML private Spinner<Integer> preferredEndHourSpinner;
    @FXML private Spinner<Integer> preferredEndMinuteSpinner;
    @FXML private TextField courseNameField;
    @FXML private ColorPicker courseColorPicker;
    @FXML private ComboBox<String> examCourseCombo;
    @FXML private TextField examTitleField;
    @FXML private DatePicker examDatePicker;
    @FXML private Spinner<Integer> examHourSpinner;
    @FXML private Spinner<Integer> examMinuteSpinner;
    @FXML private Spinner<Integer> examImportanceSpinner;
    @FXML private Spinner<Double> examWeightSpinner;
    @FXML private ListView<String> examListView;
    @FXML private ComboBox<String> focusTaskCombo;
    @FXML private Spinner<Integer> focusMinutesSpinner;
    @FXML private Button focusSoundToggleButton;
    @FXML private Button focusPauseButton;
    @FXML private Label focusSoundModeLabel;
    @FXML private StackPane focusPulseStage;
    @FXML private Region focusPulseOuter;
    @FXML private Region focusPulseInner;
    @FXML private Label focusSelectedTaskLabel;
    @FXML private Label focusCountdownLabel;
    @FXML private Button focusReflectionGreatButton;
    @FXML private Button focusReflectionOkayButton;
    @FXML private Button focusReflectionDistractedButton;

    private MainViewModel viewModel;
    private final FocusAudioService focusAudioService = new FocusAudioService();
    private WorkspaceScreen activeScreen = WorkspaceScreen.DASHBOARD;
    private static final Color DEFAULT_COURSE_COLOR = Color.web("#6A8DFF");
    private String selectedTaskId;
    private Timeline focusCountdownTimeline;
    private ScaleTransition outerPulse;
    private ScaleTransition innerPulse;
    private int focusSecondsRemaining = 25 * 60;
    private boolean rainSoundEnabled = false;
    private boolean focusPaused = false;
    private String selectedSessionId;
    private LocalDateTime selectedSessionStart;
    private double selectedSessionDurationHours = 1.0;
    private String currentFocusTaskId;
    private int currentFocusMinutes = 25;
    private boolean focusSessionRecorded;
    private String lastCompletedFocusSessionId;
    private final Map<String, String> focusTaskIdByDisplay = new LinkedHashMap<>();
    private final Map<String, String> timelineTaskIdByDisplay = new LinkedHashMap<>();
    private static final DateTimeFormatter FRIENDLY_DUE = DateTimeFormatter.ofPattern("EEE, MMM d");
    private static final DateTimeFormatter FRIENDLY_HOME_DUE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public void bind(MainViewModel viewModel) {
        this.viewModel = viewModel;
        bannerLabel.textProperty().bind(viewModel.bannerTextProperty());
        configureControls();
        refresh();
        showScreen(WorkspaceScreen.DASHBOARD, false);
    }

    private void configureControls() {
        studentAgeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 21));
        taskDeadlineHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        taskDeadlineMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        maxHoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 14, 5));
        availabilityStartHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        availabilityStartMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        availabilityEndHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 22));
        availabilityEndMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        preferredStartHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        preferredStartMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        preferredEndHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 20));
        preferredEndMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        examHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        examMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        examImportanceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 4));
        examWeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1.0, 0.3, 0.1));

        focusMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 90, 25, 5));

        strategyChoice.setItems(FXCollections.observableArrayList(viewModel.strategyNames()));
        if (!viewModel.strategyNames().isEmpty()) {
            strategyChoice.setValue(viewModel.strategyNames().getFirst());
        }

        if (courseColorPicker != null) {
            courseColorPicker.setValue(DEFAULT_COURSE_COLOR);
        }
        taskDeadlineDatePicker.setValue(LocalDate.now().plusDays(1));
        examDatePicker.setValue(LocalDate.now().plusDays(7));
        selectedTaskLabel.setText("Click a task in the schedule to edit it, or use + Add Task to create one.");
        initializePulseAnimations();
        updateFocusSoundLabel();
        updateFocusPauseButton();
        updateReflectionButtons(false);

        // Tasks list: selecting an item opens it in the editor without exposing UUIDs.
        timelineList.getSelectionModel().selectedItemProperty().addListener((ignored, previous, selected) -> {
            if (selected == null) {
                return;
            }
            String taskId = timelineTaskIdByDisplay.get(selected);
            if (taskId == null) {
                return;
            }
            Task task = findTask(taskId);
            if (task == null) {
                return;
            }
            selectedTaskId = task.id();
            selectCourse(task.courseId());
            taskTitleField.setText(task.title());
            taskDescriptionArea.setText(task.description());
            taskDeadlineDatePicker.setValue(task.deadline().toLocalDate());
            taskDeadlineHourSpinner.getValueFactory().setValue(task.deadline().getHour());
            taskDeadlineMinuteSpinner.getValueFactory().setValue(task.deadline().getMinute());
            selectedTaskLabel.setText("Editing: " + task.title());
        });

        timelineList.setCellFactory(list -> new RoundedCardCell("task-cell"));
    }

    @FXML
    private void onShowToday() {
        showScreen(WorkspaceScreen.DASHBOARD, true);
    }

    @FXML
    private void onShowFocus() {
        showScreen(WorkspaceScreen.DEEP_WORK, true);
    }

    @FXML
    private void onShowTasks() {
        showScreen(WorkspaceScreen.TASKS, true);
    }

    @FXML
    private void onShowSchedule() {
        showScreen(WorkspaceScreen.SCHEDULE, true);
    }

    @FXML
    private void onJumpToSchedule() {
        showScreen(WorkspaceScreen.TASKS, true);
    }

    @FXML
    private void onJumpToFocus() {
        if (focusTaskCombo.getValue() == null && !focusTaskCombo.getItems().isEmpty()) {
            focusTaskCombo.setValue(focusTaskCombo.getItems().getFirst());
        }
        showScreen(WorkspaceScreen.DEEP_WORK, true);
    }

    @FXML
    private void onGenerateSchedule() {
        String selectedStrategy = strategyChoice.getValue();
        if (selectedStrategy == null || selectedStrategy.isBlank()) {
            viewModel.postBanner("Choose a scheduling strategy first.");
            return;
        }
        viewModel.generateSchedule(selectedStrategy);
        refresh();
        viewModel.postBanner("Study plan generated with " + selectedStrategy + ". This demonstrates the Strategy Pattern.");
    }

    @FXML
    private void onPrepareTaskCreate() {
        selectedTaskId = null;
        selectedSessionId = null;
        selectedSessionStart = null;
        selectedSessionDurationHours = 1.0;
        selectedTaskLabel.setText("Creating a new task from the schedule workspace.");
        clearTaskForm();
        refreshTimeline();
        showScreen(WorkspaceScreen.TASKS, true);
    }

    @FXML
    private void onAddTask() {
        String courseId = selectedCourseId(courseCombo);
        if (courseId == null) {
            viewModel.postBanner("Add a course before creating a focus target.");
            return;
        }
        if (taskTitleField.getText().isBlank()) {
            viewModel.postBanner("Enter a focus target title first.");
            return;
        }
        if (taskDeadlineDatePicker.getValue() == null) {
            viewModel.postBanner("Choose a deadline date.");
            return;
        }
        LocalDateTime deadlineAt = combine(taskDeadlineDatePicker.getValue(), spinnerTime(taskDeadlineHourSpinner, taskDeadlineMinuteSpinner));
        if (!deadlineAt.isAfter(LocalDateTime.now())) {
            viewModel.postBanner("Deadline must be in the future.");
            return;
        }
        viewModel.addTask(
                courseId,
                taskTitleField.getText().trim(),
                deadlineAt,
                2,
                3,
                4
        );
        selectedTaskLabel.setText("Focus target added. You can refine it now.");
        clearTaskForm();
        refresh();
    }

    @FXML
    private void onUpdateTask() {
        String courseId = selectedCourseId(courseCombo);
        if (selectedTaskId == null) {
            viewModel.postBanner("Select a focus target to edit first.");
            return;
        }
        if (courseId == null) {
            viewModel.postBanner("Choose a course for this focus target.");
            return;
        }
        if (taskTitleField.getText().isBlank()) {
            viewModel.postBanner("Enter a focus target title first.");
            return;
        }
        if (taskDeadlineDatePicker.getValue() == null) {
            viewModel.postBanner("Choose a deadline date.");
            return;
        }
        Task existingTask = findTask(selectedTaskId);
        if (existingTask == null) {
            return;
        }
        LocalDateTime deadlineAt = combine(taskDeadlineDatePicker.getValue(), spinnerTime(taskDeadlineHourSpinner, taskDeadlineMinuteSpinner));
        if (!deadlineAt.isAfter(LocalDateTime.now())) {
            viewModel.postBanner("Deadline must be in the future.");
            return;
        }
        viewModel.updateTask(
                selectedTaskId,
                courseId,
                taskTitleField.getText().trim(),
                taskDescriptionArea.getText().trim(),
                deadlineAt,
                existingTask.estimatedHours(),
                existingTask.difficulty(),
                existingTask.importance()
        );
        refresh();
    }

    @FXML
    private void onDeleteTask() {
        if (selectedTaskId == null) {
            return;
        }
        viewModel.deleteTask(selectedTaskId);
        selectedTaskId = null;
        selectedSessionId = null;
        selectedSessionStart = null;
        selectedSessionDurationHours = 1.0;
        selectedTaskLabel.setText("Task removed. Use + Add Task to create another one.");
        clearTaskForm();
        refresh();
    }

    @FXML
    private void onMarkCompleted() {
        if (selectedTaskId == null) {
            return;
        }
        Task task = findTask(selectedTaskId);
        if (task == null) {
            return;
        }
        viewModel.markTask(selectedTaskId, TaskStatus.COMPLETED, task.remainingHours());
        refresh();
    }

    @FXML
    private void onSaveProfile() {
        if (studentNameField.getText().isBlank()) {
            return;
        }
        viewModel.updateStudentProfile(
                studentNameField.getText().trim(),
                studentAgeSpinner.getValue(),
                maxHoursSpinner.getValue(),
                spinnerTime(availabilityStartHourSpinner, availabilityStartMinuteSpinner),
                spinnerTime(availabilityEndHourSpinner, availabilityEndMinuteSpinner),
                spinnerTime(preferredStartHourSpinner, preferredStartMinuteSpinner),
                spinnerTime(preferredEndHourSpinner, preferredEndMinuteSpinner)
        );
        refresh();
    }

    @FXML
    private void onStartFresh() {
        String name = studentNameField.getText().isBlank() ? "New Student" : studentNameField.getText().trim();
        viewModel.clearAndInitializeWorkspace(
                name,
                studentAgeSpinner.getValue(),
                maxHoursSpinner.getValue(),
                spinnerTime(availabilityStartHourSpinner, availabilityStartMinuteSpinner),
                spinnerTime(availabilityEndHourSpinner, availabilityEndMinuteSpinner),
                spinnerTime(preferredStartHourSpinner, preferredStartMinuteSpinner),
                spinnerTime(preferredEndHourSpinner, preferredEndMinuteSpinner)
        );
        selectedTaskId = null;
        selectedTaskLabel.setText("Workspace reset. Add a course, then create a task from Schedule.");
        clearTaskForm();
        examTitleField.clear();
        courseNameField.clear();
        refresh();
    }

    @FXML
    private void onAddCourse() {
        if (courseNameField.getText().isBlank()) {
            return;
        }
        Color selectedColor = courseColorPicker == null ? DEFAULT_COURSE_COLOR : courseColorPicker.getValue();
        viewModel.addCourse(courseNameField.getText().trim(), toHex(selectedColor));
        courseNameField.clear();
        refresh();
    }

    @FXML
    private void onAddExam() {
        String courseId = selectedCourseId(examCourseCombo);
        if (courseId == null || examTitleField.getText().isBlank() || examDatePicker.getValue() == null) {
            return;
        }
        viewModel.addExam(
                courseId,
                examTitleField.getText().trim(),
                combine(examDatePicker.getValue(), spinnerTime(examHourSpinner, examMinuteSpinner)),
                examImportanceSpinner.getValue(),
                examWeightSpinner.getValue()
        );
        examTitleField.clear();
        examDatePicker.setValue(LocalDate.now().plusDays(7));
        refresh();
    }

    @FXML
    private void onStartFocusSession() {
        String selected = focusTaskCombo.getValue();
        if (selected == null) {
            viewModel.postBanner("Choose a focus target before starting Deep Work.");
            return;
        }
        String taskId = focusTaskIdByDisplay.get(selected);
        if (taskId == null) {
            viewModel.postBanner("Choose a focus target before starting Deep Work.");
            return;
        }
        Task task = findTask(taskId);
        if (task == null) {
            viewModel.postBanner("Choose a focus target before starting Deep Work.");
            return;
        }

        currentFocusTaskId = taskId;
        currentFocusMinutes = focusMinutesSpinner.getValue();
        focusSessionRecorded = false;
        focusSecondsRemaining = currentFocusMinutes * 60;
        focusPaused = false;
        lastReflectionLabel.setText(viewModel.focusDashboard().lastReflection());
        updateReflectionButtons(false);
        focusSelectedTaskLabel.setText(task.title());
        updateFocusCountdownLabel();
        startPulseAnimations();
        focusAudioService.play(rainSoundEnabled);
        updateFocusPauseButton();
        stopCountdownTimeline();
        focusCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            focusSecondsRemaining = Math.max(0, focusSecondsRemaining - 1);
            updateFocusCountdownLabel();
            if (focusSecondsRemaining == 0) {
                completeFocusSession(task);
                stopFocusSessionVisuals();
                focusSelectedTaskLabel.setText("Session complete. Choose a reflection and keep the streak alive.");
            }
        }));
        focusCountdownTimeline.setCycleCount(Animation.INDEFINITE);
        focusCountdownTimeline.play();
    }

    @FXML
    private void onStopFocusSession() {
        stopFocusSessionVisuals();
        currentFocusTaskId = null;
        focusSessionRecorded = false;
    }

    @FXML
    private void onToggleFocusPause() {
        if (focusCountdownTimeline == null) {
            return;
        }
        focusPaused = !focusPaused;
        if (focusPaused) {
            focusCountdownTimeline.pause();
            outerPulse.pause();
            innerPulse.pause();
            focusAudioService.stop();
        } else {
            focusCountdownTimeline.play();
            outerPulse.play();
            innerPulse.play();
            focusAudioService.play(rainSoundEnabled);
        }
        updateFocusPauseButton();
    }

    @FXML
    private void onToggleFocusSound() {
        rainSoundEnabled = !rainSoundEnabled;
        updateFocusSoundLabel();
        focusAudioService.setRainMode(rainSoundEnabled);
        if (focusCountdownTimeline != null && !focusPaused) {
            focusAudioService.play(rainSoundEnabled);
        }
    }

    @FXML
    private void onReflectionGreat() {
        applyFocusReflection("GREAT");
    }

    @FXML
    private void onReflectionOkay() {
        applyFocusReflection("OKAY");
    }

    @FXML
    private void onReflectionDistracted() {
        applyFocusReflection("DISTRACTED");
    }

    private void refresh() {
        StudyCoachState state = viewModel.state();
        refreshFocusDashboard(viewModel.focusDashboard(), state);
        refreshSchedule(viewModel.schedule(), state);
        refreshDataSetup(state);
        refreshFocusTasks(state);
        refreshFocusDashboardInsights(viewModel.focusDashboard(), state);
    }

    private void refreshFocusDashboard(FocusDashboardSnapshot snapshot, StudyCoachState state) {
        studentLabel.setText(snapshot.studentName() + ", " + state.student().age());
        todayHeadlineLabel.setText(greetingFor(LocalTime.now()) + ", " + snapshot.studentName());

        boolean hasTarget = snapshot.nextTaskTitle() != null;
        if (hasTarget) {
            if (homeHeroKickerLabel != null) {
                homeHeroKickerLabel.setText("Today’s Focus");
            }
            homeHeroTaskLabel.setText(snapshot.nextTaskTitle());
            String course = snapshot.nextTaskCourse() == null ? "" : snapshot.nextTaskCourse();
            String due = snapshot.nextTaskDueDate() == null ? "" : FRIENDLY_HOME_DUE.format(snapshot.nextTaskDueDate());
            homeHeroMetaLabel.setText(course + "  ·  Due " + due + "  ·  25 min");
        } else {
            if (homeHeroKickerLabel != null) {
                homeHeroKickerLabel.setText("Today’s Focus");
            }
            homeHeroTaskLabel.setText("No focus target yet 🌱");
            homeHeroMetaLabel.setText("Add one small target to begin your first focus block.");
        }

        if (homeStartFocusButton != null) {
            homeStartFocusButton.setVisible(hasTarget);
            homeStartFocusButton.setManaged(hasTarget);
        }
        if (homeViewTasksButton != null) {
            homeViewTasksButton.setVisible(hasTarget);
            homeViewTasksButton.setManaged(hasTarget);
        }
        if (homeAddTargetButton != null) {
            homeAddTargetButton.setVisible(!hasTarget);
            homeAddTargetButton.setManaged(!hasTarget);
        }

        if (riskLabel != null) {
            riskLabel.setText(snapshot.bestFocusWindow());
        }

        long activeTargets = state.tasks().stream()
        .filter(Task::isActive)
        .count();

        long dueThisWeek = state.tasks().stream()
                .filter(Task::isActive)
                .filter(task -> !task.deadline().toLocalDate().isAfter(LocalDate.now().plusDays(7)))
                .count();

        long completedTargets = state.tasks().stream()
                .filter(task -> task.status() == TaskStatus.COMPLETED)
                .count();

        if (homeActiveTargetsLabel != null) {
            homeActiveTargetsLabel.setText(activeTargets + " active");
        }

        if (homeDueSoonLabel != null) {
            homeDueSoonLabel.setText(dueThisWeek + " due soon");
        }

        if (homeCompletedTargetsLabel != null) {
            homeCompletedTargetsLabel.setText(completedTargets + " completed");
        }

        if (homeBestWindowLabel != null) {
            homeBestWindowLabel.setText(snapshot.bestFocusWindow());
        }

        if (homeDeadlinesList != null) {
            var upcoming = state.tasks().stream()
                    .filter(Task::isActive)
                    .sorted(Comparator.comparing(Task::deadline))
                    .limit(5)
                    .map(task -> {
                        String courseName = findCourseName(task.courseId(), state);
                        return task.title()
                                + "\n"
                                + courseName
                                + "  •  due "
                                + FRIENDLY_DUE.format(task.deadline());
                    })
                    .toList();

            if (upcoming.isEmpty()) {
                homeDeadlinesList.setItems(FXCollections.observableArrayList(
                        java.util.List.of("No active deadlines yet.\nAdd a target to begin planning.")
                ));
            } else {
                homeDeadlinesList.setItems(FXCollections.observableArrayList(upcoming));
            }
        }
    }

    private String greetingFor(LocalTime now) {
        if (now.isBefore(LocalTime.NOON)) {
            return "Good morning";
        }
        if (now.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    private void refreshFocusDashboardInsights(FocusDashboardSnapshot snapshot, StudyCoachState state) {
        focusStreakLabel.setText(snapshot.focusStreakDays() == 0 ? "No streak yet" : snapshot.focusStreakDays() + " day streak");
        weeklyFocusLabel.setText(snapshot.sessionsThisWeek() == 0 ? "No sessions yet" : snapshot.sessionsThisWeek() + " sessions this week");
        if (gardenStageLabel != null) {
            gardenStageLabel.setText(describeGardenStage((int) snapshot.completedFocusSessions()));
        }
        lastReflectionLabel.setText(snapshot.lastReflection());
        // Focus screen no longer renders the garden preview; keep only the home dashboard stage label.
    }

    private void refreshSchedule(ScheduleSnapshot snapshot, StudyCoachState state) {
        scheduleGrid.getChildren().clear();
        Map<LocalDate, DailyPlan> planLookup = state.generatedPlans().stream().collect(Collectors.toMap(DailyPlan::date, Function.identity(), (left, right) -> left));
        int row = 0;
        for (ScheduleSnapshot.DayPlanView day : snapshot.days()) {
            DailyPlan livePlan = planLookup.get(day.date());

            VBox lane = new VBox(14);
            lane.getStyleClass().add("heat-column");
            lane.getStyleClass().add(heatClass(livePlan));
            if (selectedSessionStart != null && selectedSessionStart.toLocalDate().equals(day.date())) {
                lane.getStyleClass().add("heat-column-active");
            }

            HBox header = new HBox(12);
            Region intensityBar = new Region();
            intensityBar.getStyleClass().add("heat-strip");
            intensityBar.getStyleClass().add(heatClass(livePlan) + "-strip");

            VBox headerCopy = new VBox(4);
            Label dayLabel = new Label(day.date().getDayOfWeek() + "  " + day.date());
            dayLabel.getStyleClass().add("schedule-day-label");
            Label heatMeta = new Label(describeHeatMeta(livePlan));
            heatMeta.getStyleClass().add("heat-meta");
            Label heatDots = new Label(describeLoadDots(livePlan) + "   Focus load: " + describeLoadText(livePlan));
            heatDots.getStyleClass().add("heat-dots");
            headerCopy.getChildren().addAll(dayLabel, heatMeta, heatDots);
            header.getChildren().addAll(intensityBar, headerCopy);

            FlowPane sessionsBox = new FlowPane();
            sessionsBox.getStyleClass().add("schedule-session-box");
            sessionsBox.setHgap(10);
            sessionsBox.setVgap(10);
            // FocusForge de-emphasizes calendar shuffling; we keep the path view read-only for stability.
            sessionsBox.setOnDragOver(event -> event.consume());
            sessionsBox.setOnDragDropped(event -> event.consume());

            for (ScheduleSnapshot.SessionView session : day.sessions()) {
                Task task = findTask(session.taskId());
                VBox sessionCard = createScheduleCard(session, task, state);
                sessionCard.setUserData(session.id());
                sessionCard.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openTaskFromSchedule(session));
                sessionCard.setOnMouseEntered(event -> sessionCard.setTranslateY(-2));
                sessionCard.setOnMouseExited(event -> sessionCard.setTranslateY(0));
                sessionCard.setOnDragDetected(event -> {
                    event.consume();
                });
                sessionsBox.getChildren().add(sessionCard);
            }

            VBox dayPane = new VBox(12);
            dayPane.getStyleClass().add("schedule-day-pane");
            dayPane.getChildren().addAll(header, sessionsBox);
            lane.getChildren().add(dayPane);
            scheduleGrid.add(lane, 0, row++);
        }
        refreshTimeline();
    }

    private static class RoundedCardCell extends javafx.scene.control.ListCell<String> {
        private final String styleClass;

        private RoundedCardCell(String styleClass) {
            this.styleClass = styleClass;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setWrapText(true);
            setPrefWidth(0);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().remove(styleClass);
                return;
            }
            setText(item);
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        }
    }

    private void refreshDataSetup(StudyCoachState state) {
        studentNameField.setText(state.student().name());
        studentAgeSpinner.getValueFactory().setValue(state.student().age());
        maxHoursSpinner.getValueFactory().setValue((int) Math.round(state.student().maxStudyHoursPerDay()));
        profileSummaryLabel.setText(buildProfileSummary(state));

        TimeWindow availability = state.student().availability().isEmpty()
                ? new TimeWindow(LocalTime.of(17, 0), LocalTime.of(22, 0))
                : state.student().availability().getFirst();
        TimeWindow preferred = state.student().productivityProfile().preferredWindows().isEmpty()
                ? new TimeWindow(LocalTime.of(18, 0), LocalTime.of(20, 0))
                : state.student().productivityProfile().preferredWindows().getFirst();

        availabilityStartHourSpinner.getValueFactory().setValue(availability.start().getHour());
        availabilityStartMinuteSpinner.getValueFactory().setValue(availability.start().getMinute());
        availabilityEndHourSpinner.getValueFactory().setValue(availability.end().getHour());
        availabilityEndMinuteSpinner.getValueFactory().setValue(availability.end().getMinute());
        preferredStartHourSpinner.getValueFactory().setValue(preferred.start().getHour());
        preferredStartMinuteSpinner.getValueFactory().setValue(preferred.start().getMinute());
        preferredEndHourSpinner.getValueFactory().setValue(preferred.end().getHour());
        preferredEndMinuteSpinner.getValueFactory().setValue(preferred.end().getMinute());

        var courseItems = FXCollections.observableArrayList(state.courses().stream()
                .map(course -> course.id() + " • " + course.name())
                .toList());
        courseCombo.setItems(courseItems);
        examCourseCombo.setItems(FXCollections.observableArrayList(courseItems));
        if (!courseItems.isEmpty()) {
            if (courseCombo.getValue() == null || !courseItems.contains(courseCombo.getValue())) {
                courseCombo.setValue(courseItems.getFirst());
            }
            if (examCourseCombo.getValue() == null || !courseItems.contains(examCourseCombo.getValue())) {
                examCourseCombo.setValue(courseItems.getFirst());
            }
        } else {
            courseCombo.setValue(null);
            examCourseCombo.setValue(null);
        }

        refreshCourseChips(state);
        examListView.setItems(FXCollections.observableArrayList(state.exams().stream()
                .map(exam -> formatExam(exam, state))
                .toList()));
    }

    private void refreshFocusTasks(StudyCoachState state) {
        focusTaskIdByDisplay.clear();
        var items = state.tasks().stream()
                .filter(Task::isActive)
                .sorted(Comparator.comparing(Task::deadline))
                .map(task -> {
                    String courseName = findCourseName(task.courseId(), state);
                    String display = courseName + " · " + task.title();
                    focusTaskIdByDisplay.put(display, task.id());
                    return display;
                })
                .toList();
        focusTaskCombo.setItems(FXCollections.observableArrayList(items));
        if (focusTaskCombo.getValue() == null && !items.isEmpty()) {
            focusTaskCombo.setValue(items.getFirst());
        }
    }

    private void refreshCourseChips(StudyCoachState state) {
        profileCoursesPane.getChildren().clear();
        if (state.courses().isEmpty()) {
            Label emptyState = new Label("No courses yet. Add your first one to start shaping the profile.");
            emptyState.getStyleClass().add("panel-meta");
            profileCoursesPane.getChildren().add(emptyState);
            return;
        }

        for (Course course : state.courses()) {
            Label chip = new Label(course.name());
            chip.getStyleClass().add("profile-course-chip");
            chip.setBackground(new Background(new BackgroundFill(
                    Color.web(state.config().courseColors().getOrDefault(course.id(), course.colorHex())),
                    new CornerRadii(999),
                    Insets.EMPTY
            )));
            profileCoursesPane.getChildren().add(chip);
        }
    }

    private String buildProfileSummary(StudyCoachState state) {
        String focusWindow = state.student().productivityProfile().preferredWindows().isEmpty()
                ? "Not set"
                : state.student().productivityProfile().preferredWindows().getFirst().start()
                + " - "
                + state.student().productivityProfile().preferredWindows().getFirst().end();
        return state.student().name() + ", age " + state.student().age()
                + "  •  "
                + state.courses().size() + " courses"
                + "  •  Focus window " + focusWindow
                + "  •  "
                + countCompletedFocusSessions(state) + " completed focus sessions";
    }

    private VBox createScheduleCard(ScheduleSnapshot.SessionView session, Task task, StudyCoachState state) {
        VBox card = new VBox(12);
        card.getStyleClass().add("session-card");
        card.setPrefWidth(330);
        card.setMinHeight(180);

        Label courseTag = new Label(findCourseName(session.courseId(), state));
        courseTag.getStyleClass().add("session-course-tag");
        courseTag.setBackground(new Background(new BackgroundFill(
                Color.web(session.colorHex()),
                new CornerRadii(999),
                Insets.EMPTY
        )));

        Label timeLabel = new Label(session.timeRange());
        timeLabel.getStyleClass().add("session-time-label");
        HBox topRow = new HBox(10, courseTag, timeLabel);

        TextField titleField = new TextField(task == null ? session.title() : task.title());
        titleField.getStyleClass().add("session-title-field");
        titleField.setOnMouseClicked(MouseEvent::consume);
        titleField.setOnAction(event -> saveInlineTaskTitle(task, titleField.getText()));

        double progress = task == null || task.estimatedHours() <= 0 ? 0.0 : Math.min(1.0, task.completedHours() / task.estimatedHours());
        ProgressIndicator progressIndicator = new ProgressIndicator(progress);
        progressIndicator.getStyleClass().add("session-progress-ring");
        progressIndicator.setPrefSize(58, 58);
        Label progressText = new Label(Math.round(progress * 100) + "%");
        progressText.getStyleClass().add("session-progress-text");
        StackPane progressStack = new StackPane(progressIndicator, progressText);

        VBox details = new VBox(6);
        details.getChildren().addAll(
                buildMetaLabel("Due  •  " + formatDeadline(task)),
                buildMetaLabel("Estimate  •  " + formatHours(task == null ? 0.0 : task.estimatedHours())),
                buildMetaLabel("Difficulty  •  " + difficultyDots(task == null ? 0 : task.difficulty()))
        );

        HBox middleRow = new HBox(14, details, progressStack);

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("mini-button");
        saveButton.setOnAction(event -> saveInlineTaskTitle(task, titleField.getText()));
        Button plusButton = new Button("+30m");
        plusButton.getStyleClass().add("mini-button");
        plusButton.setOnAction(event -> addInlineProgress(task, 0.5));
        Button doneButton = new Button("Done");
        doneButton.getStyleClass().add("mini-button");
        doneButton.setOnAction(event -> completeInlineTask(task));
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("mini-button");
        deleteButton.setOnAction(event -> deleteInlineTask(task));
        HBox actionRow = new HBox(8, saveButton, plusButton, doneButton, deleteButton);

        card.getChildren().addAll(topRow, titleField, middleRow, actionRow);
        card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, rgba(255, 255, 255, 0.96), rgba(247, 245, 255, 0.92));"
                            + "-fx-border-color: " + withOpacity(session.colorHex(), 0.35) + ";"
            );
        return card;
    }

    private Label buildMetaLabel(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("session-meta-label");
        return label;
    }

    private void saveInlineTaskTitle(Task task, String newTitle) {
        if (task == null || newTitle == null || newTitle.isBlank()) {
            return;
        }
        viewModel.editTaskTitle(task.id(), newTitle.trim());
        refresh();
    }

    private void addInlineProgress(Task task, double hours) {
        if (task == null) {
            return;
        }
        viewModel.markTask(task.id(), TaskStatus.PARTIAL, hours);
        refresh();
    }

    private void completeInlineTask(Task task) {
        if (task == null) {
            return;
        }
        viewModel.markTask(task.id(), TaskStatus.COMPLETED, task.remainingHours());
        refresh();
    }

    private void deleteInlineTask(Task task) {
        if (task == null) {
            return;
        }
        viewModel.deleteTask(task.id());
        refresh();
    }

    private void openTaskFromSchedule(ScheduleSnapshot.SessionView session) {
        Task task = findTask(session.taskId());
        if (task == null) {
            return;
        }
        selectedTaskId = task.id();
        selectedSessionId = null;
        selectedSessionStart = null;
        selectedSessionDurationHours = 1.0;
        selectCourse(task.courseId());
        taskTitleField.setText(task.title());
        taskDescriptionArea.setText(task.description());
        taskDeadlineDatePicker.setValue(task.deadline().toLocalDate());
        taskDeadlineHourSpinner.getValueFactory().setValue(task.deadline().getHour());
        taskDeadlineMinuteSpinner.getValueFactory().setValue(task.deadline().getMinute());
        selectedTaskLabel.setText("Editing focus target: " + task.title());
        refreshTimeline();
        showScreen(WorkspaceScreen.TASKS, true);
    }

    private Task findTask(String taskId) {
        return viewModel.state().tasks().stream().filter(task -> task.id().equals(taskId)).findFirst().orElse(null);
    }

    private void initializePulseAnimations() {
        outerPulse = createPulse(focusPulseOuter, 1.0, 1.18, 3.6);
        innerPulse = createPulse(focusPulseInner, 1.0, 1.1, 2.8);
    }

    private ScaleTransition createPulse(Node node, double from, double to, double seconds) {
        ScaleTransition transition = new ScaleTransition(Duration.seconds(seconds), node);
        transition.setFromX(from);
        transition.setFromY(from);
        transition.setToX(to);
        transition.setToY(to);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setAutoReverse(true);
        return transition;
    }

    private void startPulseAnimations() {
        outerPulse.play();
        innerPulse.play();
    }

    private void stopFocusSessionVisuals() {
        stopCountdownTimeline();
        focusAudioService.stop();
        focusPaused = false;
        outerPulse.stop();
        innerPulse.stop();
        focusPulseOuter.setScaleX(1.0);
        focusPulseOuter.setScaleY(1.0);
        focusPulseInner.setScaleX(1.0);
        focusPulseInner.setScaleY(1.0);
        updateFocusPauseButton();
        updateFocusCountdownLabel();
    }

    private void completeFocusSession(Task task) {
        if (task == null || currentFocusTaskId == null || focusSessionRecorded) {
            return;
        }
        lastCompletedFocusSessionId = viewModel.completeFocusSession(currentFocusTaskId, currentFocusMinutes);
        focusSessionRecorded = true;
        updateReflectionButtons(true);
        refresh();
        viewModel.postBanner("Session saved. Choose a reflection.");
    }

    private void updateFocusSoundLabel() {
        focusSoundToggleButton.setText(rainSoundEnabled ? "Rain Sound: On" : "Rain Sound: Off");
        focusSoundModeLabel.setText(rainSoundEnabled ? "Rain" : "Ambient");
    }

    private void stopCountdownTimeline() {
        if (focusCountdownTimeline != null) {
            focusCountdownTimeline.stop();
            focusCountdownTimeline = null;
        }
    }

    private void updateFocusCountdownLabel() {
        int minutes = focusSecondsRemaining / 60;
        int seconds = focusSecondsRemaining % 60;
        focusCountdownLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateReflectionButtons(boolean enabled) {
        focusReflectionGreatButton.setDisable(!enabled);
        focusReflectionOkayButton.setDisable(!enabled);
        focusReflectionDistractedButton.setDisable(!enabled);
        focusReflectionGreatButton.setVisible(enabled);
        focusReflectionGreatButton.setManaged(enabled);
        focusReflectionOkayButton.setVisible(enabled);
        focusReflectionOkayButton.setManaged(enabled);
        focusReflectionDistractedButton.setVisible(enabled);
        focusReflectionDistractedButton.setManaged(enabled);
    }

    private void applyFocusReflection(String message) {
        if (focusReflectionGreatButton.isDisabled()) {
            return;
        }
        if (lastCompletedFocusSessionId == null) {
            viewModel.postBanner("No completed session to reflect on yet.");
            return;
        }
        FocusReflection reflection = switch (message) {
            case "GREAT" -> FocusReflection.GREAT;
            case "OKAY" -> FocusReflection.OKAY;
            case "DISTRACTED" -> FocusReflection.DISTRACTED;
            default -> FocusReflection.NONE;
        };
        viewModel.applyFocusReflection(lastCompletedFocusSessionId, reflection);
        lastCompletedFocusSessionId = null;
        lastReflectionLabel.setText(viewModel.focusDashboard().lastReflection());
        updateReflectionButtons(false);
        viewModel.postBanner("Reflection saved. Keep the streak gently.");
        refresh();
    }

    private void showScreen(WorkspaceScreen screen, boolean animated) {
        activeScreen = screen;
        workspaceTitleLabel.setText(screen.title);
        workspaceSubtitleLabel.setText(screen.subtitle);

        todayScreen.setVisible(screen == WorkspaceScreen.DASHBOARD);
        todayScreen.setManaged(screen == WorkspaceScreen.DASHBOARD);

        focusScreen.setVisible(screen == WorkspaceScreen.DEEP_WORK);
        focusScreen.setManaged(screen == WorkspaceScreen.DEEP_WORK);

        tasksScreen.setVisible(screen == WorkspaceScreen.TASKS);
        tasksScreen.setManaged(screen == WorkspaceScreen.TASKS);

        scheduleScreen.setVisible(screen == WorkspaceScreen.SCHEDULE);
        scheduleScreen.setManaged(screen == WorkspaceScreen.SCHEDULE);

        setNavClasses(todayNavButton, screen == WorkspaceScreen.DASHBOARD);
        setNavClasses(focusNavButton, screen == WorkspaceScreen.DEEP_WORK);
        setNavClasses(tasksNavButton, screen == WorkspaceScreen.TASKS);
        setNavClasses(scheduleNavButton, screen == WorkspaceScreen.SCHEDULE);

        if (animated) {
            animate(currentScreenNode());
        }
    }

    private void setNavClasses(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().setAll("nav-button");
        if (active) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    private void onOpenProfile() {
        profileOverlay.setManaged(true);
        profileOverlay.setVisible(true);
        profileOverlay.toFront();
    }

    @FXML
    private void onCloseProfileAction(ActionEvent ignored) {
        closeProfileOverlay();
    }

    @FXML
    private void onCloseProfileClick(MouseEvent ignored) {
        closeProfileOverlay();
    }

    private void closeProfileOverlay() {
        profileOverlay.setVisible(false);
        profileOverlay.setManaged(false);
        profileOverlay.toBack();
    }

    private Node currentScreenNode() {
        return switch (activeScreen) {
            case DASHBOARD -> todayScreen;
            case DEEP_WORK -> focusScreen;
            case TASKS -> tasksScreen;
            case SCHEDULE -> scheduleScreen;
        };
    }

    private String heatClass(DailyPlan plan) {
        if (plan == null) {
            return "heat-cool";
        }
        if (plan.deadlineRiskPercent() >= 70 || plan.totalHours() >= 5.5) {
            return "heat-hot";
        }
        if (plan.deadlineRiskPercent() >= 35 || plan.totalHours() >= 3.5) {
            return "heat-warm";
        }
        return "heat-cool";
    }

    private String formatExam(Exam exam, StudyCoachState state) {
        String courseName = state.courses().stream()
                .filter(course -> course.id().equals(exam.courseId()))
                .map(Course::name)
                .findFirst()
                .orElse(exam.courseId());
        return courseName + "  |  " + exam.title() + "  |  " + exam.startTime().toString().replace('T', ' ');
    }

    private void animate(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(240), node);
        transition.setFromValue(0.75);
        transition.setToValue(1.0);
        transition.play();
    }

    private void clearTaskForm() {
        taskTitleField.clear();
        taskDescriptionArea.clear();
        taskDeadlineDatePicker.setValue(LocalDate.now().plusDays(1));
        taskDeadlineHourSpinner.getValueFactory().setValue(18);
        taskDeadlineMinuteSpinner.getValueFactory().setValue(0);
    }

    private String selectedCourseId(ComboBox<String> comboBox) {
        String raw = comboBox.getValue();
        if (raw == null || !raw.contains(" ")) {
            return null;
        }
        return raw.substring(0, raw.indexOf(' '));
    }

    private void selectCourse(String courseId) {
        if (courseId == null) {
            return;
        }
        courseCombo.getItems().stream()
                .filter(item -> item.startsWith(courseId + " "))
                .findFirst()
                .ifPresent(courseCombo::setValue);
    }

    private LocalDateTime combine(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    private LocalTime spinnerTime(Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner) {
        return LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
    }

    private void refreshTimeline() {
        if (selectedTaskId != null) {
            Task task = findTask(selectedTaskId);
            if (task != null) {
                java.util.List<String> details = new java.util.ArrayList<>();
                if (selectedSessionStart != null) {
                    details.add("Scheduled  •  " + selectedSessionStart.toLocalDate() + " at " + selectedSessionStart.toLocalTime().withSecond(0).withNano(0));
                }
                details.add("Deadline  •  " + task.deadline().toLocalDate() + " at " + task.deadline().toLocalTime().withSecond(0).withNano(0));
                timelineList.setItems(FXCollections.observableArrayList(details));
                return;
            }
        }
        timelineTaskIdByDisplay.clear();
        var tasks = viewModel.state().tasks();
        var items = tasks.stream()
                .sorted(Comparator.comparing(Task::deadline))
                .map(task -> {
                    String courseName = findCourseName(task.courseId(), viewModel.state());
                    String display = task.title()
                            + "\n" + courseName
                            + "  •  due " + FRIENDLY_DUE.format(task.deadline());
                    timelineTaskIdByDisplay.put(display, task.id());
                    return display;
                })
                .toList();
        if (items.isEmpty()) {
            timelineList.setItems(FXCollections.observableArrayList(java.util.List.of("No focus targets yet. Add one to begin.")));
        } else {
            timelineList.setItems(FXCollections.observableArrayList(items));
        }

        refreshTargetSummary(tasks);
    }

    private void refreshTargetSummary(java.util.List<Task> tasks) {
        if (totalTargetsLabel == null || dueSoonLabel == null || completedTargetsLabel == null) {
            return;
        }
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(task -> task.status() == TaskStatus.COMPLETED).count();
        int dueSoon = (int) tasks.stream()
                .filter(Task::isActive)
                .filter(task -> !task.deadline().toLocalDate().isAfter(java.time.LocalDate.now().plusDays(7)))
                .count();

        totalTargetsLabel.setText(total + " active targets");
        dueSoonLabel.setText(dueSoon + " due this week");
        completedTargetsLabel.setText(completed + " completed");
    }

    private void updateFocusPauseButton() {
        focusPauseButton.setText(focusPaused ? "Resume" : "Pause");
    }

    private int countCompletedFocusSessions(StudyCoachState state) {
        return (int) state.studyHistory().stream()
                .filter(session -> session.completed())
                .count();
    }

    private int calculateFocusStreak(StudyCoachState state) {
        java.util.Set<LocalDate> sessionDays = state.studyHistory().stream()
                .filter(session -> session.completed())
                .map(session -> session.start().toLocalDate())
                .collect(Collectors.toSet());
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (sessionDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String describeGardenStage(int sessions) {
        if (sessions >= 16) {
            return "Garden stage: Flourishing grove";
        }
        if (sessions >= 8) {
            return "Garden stage: Growing canopy";
        }
        if (sessions >= 3) {
            return "Garden stage: Fresh bloom";
        }
        return "Garden stage: First sprout";
    }

    private String describeHeatMeta(DailyPlan plan) {
        if (plan == null) {
            return "0.0h scheduled  •  System load: Calm";
        }
        return Math.round(plan.totalHours() * 10) / 10.0 + "h scheduled  •  System load: " + describeLoadText(plan);
    }

    private String describeLoadText(DailyPlan plan) {
        if (plan == null) {
            return "Calm";
        }
        if (plan.deadlineRiskPercent() >= 70 || plan.totalHours() >= 5.5) {
            return "Overloaded";
        }
        if (plan.deadlineRiskPercent() >= 35 || plan.totalHours() >= 3.5) {
            return "Active";
        }
        return "Calm";
    }

    private String describeLoadDots(DailyPlan plan) {
        return switch (describeLoadText(plan)) {
            case "Overloaded" -> "● ● ● ● ○";
            case "Active" -> "● ● ● ○ ○";
            default -> "● ● ○ ○ ○";
        };
    }

    private String findCourseName(String courseId, StudyCoachState state) {
        return state.courses().stream()
                .filter(course -> course.id().equals(courseId))
                .map(Course::name)
                .findFirst()
                .orElse(courseId);
    }

    private String formatDeadline(Task task) {
        if (task == null) {
            return "No deadline";
        }
        return task.deadline().toLocalDate() + " at " + task.deadline().toLocalTime().withSecond(0).withNano(0);
    }

    private String formatHours(double hours) {
        return Math.round(hours * 10) / 10.0 + "h";
    }

    private String difficultyDots(int difficulty) {
        if (difficulty <= 0) {
            return "Not set";
        }
        return "● ".repeat(Math.max(0, difficulty)).trim();
    }

    private String withOpacity(String hex, double opacity) {
        Color base = Color.web(hex);
        return String.format("rgba(%d, %d, %d, %.2f)",
                Math.round(base.getRed() * 255),
                Math.round(base.getGreen() * 255),
                Math.round(base.getBlue() * 255),
                opacity);
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255),
                Math.round(color.getBlue() * 255));
    }
}
