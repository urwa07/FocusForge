package com.studycoach;

import com.studycoach.domain.model.AppConfig;
import com.studycoach.domain.model.BehaviorPattern;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.FocusReflection;
import com.studycoach.domain.model.PriorityWeights;
import com.studycoach.domain.model.ProductivityProfile;
import com.studycoach.domain.model.SchedulingConfig;
import com.studycoach.domain.model.SimulationConfig;
import com.studycoach.domain.model.Student;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.StudySession;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.TaskStatus;
import com.studycoach.domain.model.TimeWindow;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static StudyCoachState sampleState() {
        Student student = new Student(
                "student-1",
                "Test Student",
                21,
                5.0,
                List.of(new TimeWindow(LocalTime.of(17, 0), LocalTime.of(22, 0))),
                new ProductivityProfile(List.of(new TimeWindow(LocalTime.of(18, 0), LocalTime.of(20, 0))), BehaviorPattern.PEAK_EVENING)
        );

        return new StudyCoachState(
                student,
                List.of(
                        new Course("cs", "Algorithms", "#FF7A59"),
                        new Course("se", "Software Engineering", "#4E6AF3")
                ),
                List.of(
                        new Task("urgent", "cs", "Urgent task", "", 5, 5, 6.0, 0.0, LocalDateTime.now().plusDays(1), TaskStatus.PARTIAL, LocalDateTime.now()),
                        new Task("distant", "se", "Distant task", "", 2, 2, 4.0, 0.0, LocalDateTime.now().plusDays(8), TaskStatus.PARTIAL, LocalDateTime.now()),
                        new Task("skipped", "se", "Skipped task", "", 4, 4, 3.0, 0.0, LocalDateTime.now().plusDays(3), TaskStatus.SKIPPED, LocalDateTime.now())
                ),
                List.of(),
                List.of(
                        new StudySession("s1", "urgent", "cs", "Urgent task", LocalDateTime.now().minusDays(1).withHour(18), LocalDateTime.now().minusDays(1).withHour(19), com.studycoach.domain.model.SessionType.FOCUS, 12.0, true, FocusReflection.NONE),
                        new StudySession("s2", "distant", "se", "Distant task", LocalDateTime.now().minusDays(2).withHour(8), LocalDateTime.now().minusDays(2).withHour(9), com.studycoach.domain.model.SessionType.FOCUS, 8.0, true, FocusReflection.NONE)
                ),
                List.of(),
                new AppConfig(
                        new PriorityWeights(0.55, 0.15, 0.2, 0.1),
                        new SchedulingConfig(5.0, 60, 15, 7),
                        new SimulationConfig(7, 4),
                        Map.of("cs", "#FF7A59", "se", "#4E6AF3")
                )
        );
    }
}
