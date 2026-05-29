package com.studycoach.domain.service;

import com.studycoach.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineTest {
    @Test
    void extraHoursImproveRiskWhileSkippingWorsensIt() {
        var state = TestDataFactory.sampleState();
        DefaultSimulationEngine engine = new DefaultSimulationEngine(
                new BehaviorAnalyzer(),
                new PriorityEngine(),
                List.of(new DeadlineBasedStrategy(), new BalancedWorkloadStrategy(), new EnergyAwareStrategy())
        );

        SimulationResult addHours = engine.simulate(new SimulationRequest(
                state,
                LocalDateTime.now(),
                "Energy Aware",
                List.of(new SimulationAction("Add hours", 2, 0, "Energy Aware", false))
        ));
        SimulationResult skipToday = engine.simulate(new SimulationRequest(
                state,
                LocalDateTime.now(),
                "Energy Aware",
                List.of(new SimulationAction("Skip today", 0, 0, "Energy Aware", true))
        ));

        assertTrue(addHours.scenarios().getFirst().deadlineRiskPercent() <= addHours.baseline().deadlineRiskPercent());
        assertTrue(skipToday.scenarios().getFirst().deadlineRiskPercent() >= skipToday.baseline().deadlineRiskPercent());
    }
}
