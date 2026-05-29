package com.studycoach.application.dto;

import java.time.LocalDate;
import java.util.List;

public record FocusDashboardSnapshot(
        String studentName,
        int focusStreakDays,
        long sessionsThisWeek,
        long completedFocusSessions,
        String nextDeepWorkTarget,
        String nextTaskTitle,
        String nextTaskCourse,
        LocalDate nextTaskDueDate,
        String bestFocusWindow,
        String lastReflection,
        List<String> coachNotes
) {
}
