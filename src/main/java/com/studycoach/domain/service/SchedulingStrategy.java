package com.studycoach.domain.service;

import com.studycoach.domain.model.DailyPlan;

import java.util.List;

public interface SchedulingStrategy {
    String name();

    List<DailyPlan> generateSchedule(SchedulingRequest request);
}
