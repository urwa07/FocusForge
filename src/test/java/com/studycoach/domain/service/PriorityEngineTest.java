package com.studycoach.domain.service;

import com.studycoach.TestDataFactory;
import com.studycoach.domain.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityEngineTest {
    private final PriorityEngine engine = new PriorityEngine();

    @Test
    void ranksUrgentHighImportanceTaskAboveDistantLowImportanceTask() {
        Task urgent = TestDataFactory.sampleState().tasks().get(0);
        Task distant = TestDataFactory.sampleState().tasks().get(1);
        PriorityContext context = new PriorityContext(LocalDateTime.now(), Set.of("skipped"), TestDataFactory.sampleState().student().productivityProfile().preferredWindows().getFirst(), TestDataFactory.sampleState().config().priorityWeights());

        PriorityScore urgentScore = engine.scoreTask(urgent, context);
        PriorityScore distantScore = engine.scoreTask(distant, context);

        assertTrue(urgentScore.score() > distantScore.score());
    }

    @Test
    void skippedBehaviorRaisesPrioritySignals() {
        Task skipped = TestDataFactory.sampleState().tasks().get(2);
        PriorityContext context = new PriorityContext(LocalDateTime.now(), Set.of("skipped"), TestDataFactory.sampleState().student().productivityProfile().preferredWindows().getFirst(), TestDataFactory.sampleState().config().priorityWeights());

        PriorityScore skippedScore = engine.scoreTask(skipped, context);

        assertTrue(skippedScore.reasons().stream().anyMatch(reason -> reason.contains("Avoidance")));
    }
}
