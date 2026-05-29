package com.studycoach.application.service;

import com.studycoach.TestDataFactory;
import com.studycoach.application.port.StudyCoachRepository;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyCoachServiceTest {
    @Test
    void taskUpdatesTriggerAdaptiveRescheduling() {
        InMemoryRepository repository = new InMemoryRepository(TestDataFactory.sampleState());
        StudyCoachService service = new StudyCoachService(repository);
        int initialPlanCount = repository.state.generatedPlans().size();

        service.updateTaskProgress("urgent", TaskStatus.SKIPPED, 0.0);

        assertFalse(repository.state.generatedPlans().isEmpty());
        assertTrue(repository.state.generatedPlans().size() >= initialPlanCount);
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
