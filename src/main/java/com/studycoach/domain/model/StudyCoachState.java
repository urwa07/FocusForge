package com.studycoach.domain.model;

import java.util.List;

public record StudyCoachState(
        Student student,
        List<Course> courses,
        List<Task> tasks,
        List<Exam> exams,
        List<StudySession> studyHistory,
        List<DailyPlan> generatedPlans,
        AppConfig config
) {
    public StudyCoachState withTasks(List<Task> updatedTasks) {
        return new StudyCoachState(student, courses, updatedTasks, exams, studyHistory, generatedPlans, config);
    }

    public StudyCoachState withGeneratedPlans(List<DailyPlan> plans) {
        return new StudyCoachState(student, courses, tasks, exams, studyHistory, plans, config);
    }

    public StudyCoachState withHistory(List<StudySession> history) {
        return new StudyCoachState(student, courses, tasks, exams, history, generatedPlans, config);
    }
}
