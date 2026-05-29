package com.studycoach.domain.service;

import com.studycoach.domain.model.StudyCoachState;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationRequest(
        StudyCoachState state,
        LocalDateTime now,
        String baselineStrategy,
        List<SimulationAction> actions
) {
}
