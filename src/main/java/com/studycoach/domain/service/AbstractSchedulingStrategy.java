package com.studycoach.domain.service;

import com.studycoach.domain.model.DailyPlan;
import com.studycoach.domain.model.FocusReflection;
import com.studycoach.domain.model.RiskAssessment;
import com.studycoach.domain.model.SessionType;
import com.studycoach.domain.model.StudySession;
import com.studycoach.domain.model.Task;
import com.studycoach.domain.model.TimeWindow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

abstract class AbstractSchedulingStrategy implements SchedulingStrategy {
    protected List<DailyPlan> buildPlans(
            SchedulingRequest request,
            List<Task> orderedTasks,
            TimeWindow preferredWindow,
            boolean balanceAcrossDays
    ) {
        int horizon = request.config().schedulingConfig().planningHorizonDays();
        double maxHours = Math.min(
                request.student().maxStudyHoursPerDay(),
                request.config().schedulingConfig().maxHoursPerDay()
        );
        double sessionHours = request.config().schedulingConfig().sessionMinutes() / 60.0;

        Map<LocalDate, List<StudySession>> sessionsByDay = new HashMap<>();
        Map<LocalDate, Double> hoursByDay = new HashMap<>();
        LocalDate startDate = request.startTime().toLocalDate();
        int rollingOffset = 0;

        for (Task task : orderedTasks) {
            double remaining = task.remainingHours();

            while (remaining > 0.01) {
                int dayOffset = balanceAcrossDays ? rollingOffset % horizon : 0;
                LocalDate targetDate = startDate.plusDays(dayOffset);

                LocalDateTime slot = nextAvailableSlot(
                        request,
                        targetDate,
                        preferredWindow,
                        hoursByDay
                );

                if (slot == null) {
                    break;
                }

                LocalDate date = slot.toLocalDate();
                double allocated = Math.min(sessionHours, remaining);

                StudySession session = new StudySession(
                        UUID.randomUUID().toString(),
                        task.id(),
                        task.courseId(),
                        task.title(),
                        slot,
                        slot.plusMinutes((long) (allocated * 60)),
                        task.deadline().minusDays(2).isBefore(slot) ? SessionType.EXAM_PREP : SessionType.FOCUS,
                        request.priorityScores().get(task.id()).score(),
                        false,
                        FocusReflection.NONE
                );

                sessionsByDay.computeIfAbsent(date, ignored -> new ArrayList<>()).add(session);
                hoursByDay.merge(date, allocated, Double::sum);

                remaining -= allocated;
                rollingOffset++;

                if (hoursByDay.get(date) >= maxHours) {
                    rollingOffset++;
                }
            }
        }

        List<DailyPlan> plans = new ArrayList<>();

        for (int i = 0; i < horizon; i++) {
            LocalDate date = startDate.plusDays(i);

            List<StudySession> daySessions = sessionsByDay
                    .getOrDefault(date, List.of())
                    .stream()
                    .sorted(Comparator.comparing(StudySession::start))
                    .toList();

            double totalHours = daySessions.stream()
                    .mapToDouble(StudySession::durationHours)
                    .sum();

            double deadlineRisk = calculateDeadlineRisk(request, date);

            RiskAssessment risk = deadlineRisk >= 80 ? RiskAssessment.CRITICAL
                    : deadlineRisk >= 60 ? RiskAssessment.HIGH
                    : deadlineRisk >= 35 ? RiskAssessment.MODERATE
                    : RiskAssessment.LOW;

            String recommendation = deadlineRisk >= 60
                    ? "Protect deep-work time and keep skipped tasks out of this window."
                    : "Momentum is healthy. Use the spare buffer for spaced review.";

            plans.add(new DailyPlan(
                    date,
                    daySessions,
                    totalHours,
                    risk,
                    deadlineRisk,
                    recommendation
            ));
        }

        return plans;
    }

    private LocalDateTime nextAvailableSlot(
            SchedulingRequest request,
            LocalDate date,
            TimeWindow preferredWindow,
            Map<LocalDate, Double> hoursByDay
    ) {
        double maxHours = Math.min(
                request.student().maxStudyHoursPerDay(),
                request.config().schedulingConfig().maxHoursPerDay()
        );

        double usedHours = hoursByDay.getOrDefault(date, 0.0);

        if (usedHours >= maxHours) {
            return null;
        }

        int sessionMinutes = request.config().schedulingConfig().sessionMinutes();
        int breakMinutes = request.config().schedulingConfig().breakMinutes();

        TimeWindow availabilityWindow = request.student().availability().getFirst();

        LocalTime anchor = preferredWindow != null
                ? preferredWindow.start()
                : availabilityWindow.start();

        long totalMinutes = (long) (usedHours * 60);

        if (usedHours > 0) {
            totalMinutes += breakMinutes;
        }

        LocalDateTime proposedStart = date.atTime(anchor).plusMinutes(totalMinutes);
        LocalDateTime proposedEnd = proposedStart.plusMinutes(sessionMinutes);

        LocalTime latestAllowedEnd = preferredWindow != null
                ? minTime(preferredWindow.end(), availabilityWindow.end())
                : availabilityWindow.end();

        if (proposedEnd.toLocalTime().isAfter(latestAllowedEnd)) {
            return null;
        }

        return proposedStart;
    }

    private LocalTime minTime(LocalTime first, LocalTime second) {
        return first.isBefore(second) ? first : second;
    }

    private double calculateDeadlineRisk(SchedulingRequest request, LocalDate date) {
        double atRisk = request.tasks()
                .stream()
                .filter(Task::isActive)
                .filter(task -> !task.deadline().toLocalDate().isAfter(date.plusDays(2)))
                .mapToDouble(Task::remainingHours)
                .sum();

        double capacity = request.config().schedulingConfig().maxHoursPerDay() * 2;

        return Math.min(
                100.0,
                capacity == 0 ? 100.0 : (atRisk / capacity) * 100.0
        );
    }
}
