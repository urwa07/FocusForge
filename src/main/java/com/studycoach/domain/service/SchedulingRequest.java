package com.studycoach.domain.service;

import com.studycoach.domain.model.AppConfig;
import com.studycoach.domain.model.BehaviorInsights;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.Exam;
import com.studycoach.domain.model.Student;
import com.studycoach.domain.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SchedulingRequest(
        LocalDateTime startTime,
        Student student,
        List<Course> courses,
        List<Task> tasks,
        List<Exam> exams,
        BehaviorInsights behaviorInsights,
        AppConfig config,
        Map<String, PriorityScore> priorityScores
) {
}
