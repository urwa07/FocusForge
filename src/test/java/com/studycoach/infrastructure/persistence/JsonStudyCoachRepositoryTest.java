package com.studycoach.infrastructure.persistence;

import com.studycoach.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonStudyCoachRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripPreservesTasksPlansAndConfig() {
        JsonStudyCoachRepository repository = new JsonStudyCoachRepository(tempDir, ObjectMapperFactory.create());
        var state = TestDataFactory.sampleState();

        repository.save(state);
        var loaded = repository.load();

        assertEquals(state.tasks().size(), loaded.tasks().size());
        assertEquals(state.config().priorityWeights().deadlineWeight(), loaded.config().priorityWeights().deadlineWeight());
        assertEquals(state.student().name(), loaded.student().name());
    }
}
