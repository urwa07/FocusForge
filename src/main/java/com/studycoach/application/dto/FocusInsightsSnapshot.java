package com.studycoach.application.dto;

import com.studycoach.domain.model.FocusReflection;

import java.util.List;
import java.util.Map;

public record FocusInsightsSnapshot(
        int focusStreakDays,
        long sessionsThisWeek,
        long completedFocusSessions,
        String bestFocusWindow,
        Map<FocusReflection, Long> reflectionCounts,
        List<AnalyticsSnapshot.Point> subjectDistribution,
        List<String> avoidedTargets,
        List<String> coachNotes
) {
}

