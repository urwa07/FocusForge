package com.studycoach.application.service;

import com.studycoach.TestDataFactory;
import com.studycoach.application.exception.DuplicateCourseException;
import com.studycoach.application.exception.InvalidDeadlineException;
import com.studycoach.application.exception.UnknownCourseException;
import com.studycoach.application.exception.UnknownTaskException;
import com.studycoach.application.port.StudyCoachRepository;
import com.studycoach.domain.model.StudyCoachState;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StudyCoachServiceValidationTest {
    @Test
    void addTaskRejectsPastDeadlines() {
        StudyCoachService service = new StudyCoachService(new InMemoryRepository(TestDataFactory.sampleState()));
        assertThrows(
                InvalidDeadlineException.class,
                () -> service.addTask("cs", "Test", LocalDateTime.now().minusDays(1), 1.0, 3, 3)
        );
    }

    @Test
    void addTaskRejectsUnknownCourse() {
        StudyCoachService service = new StudyCoachService(new InMemoryRepository(TestDataFactory.sampleState()));
        assertThrows(
                UnknownCourseException.class,
                () -> service.addTask("does-not-exist", "Test", LocalDateTime.now().plusDays(1), 1.0, 3, 3)
        );
    }

    @Test
    void updateTaskRejectsUnknownTask() {
        StudyCoachService service = new StudyCoachService(new InMemoryRepository(TestDataFactory.sampleState()));
        assertThrows(
                UnknownTaskException.class,
                () -> service.updateTask("missing", "cs", "Test", "", LocalDateTime.now().plusDays(1), 1.0, 3, 3)
        );
    }

    @Test
    void addCourseRejectsDuplicates() {
        StudyCoachService service = new StudyCoachService(new InMemoryRepository(TestDataFactory.sampleState()));
        assertThrows(DuplicateCourseException.class, () -> service.addCourse("cs", "#000000"));
    }

    private static final class InMemoryRepository implements StudyCoachRepository {
        private StudyCoachState state;

        private InMemoryRepository(StudyCoachState initialState) {
            this.state = initialState;
        }

        @Override
        public StudyCoachState load() {
            return state;
        }

        @Override
        public void save(StudyCoachState state) {
            this.state = state;
        }

        @Override
        public void initializeIfMissing() {
        }
    }
}

