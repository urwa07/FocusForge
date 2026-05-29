package com.studycoach.domain.service;

import com.studycoach.domain.model.Task;

public interface PriorityScoringPolicy {
    PriorityScore scoreTask(Task task, PriorityContext context);
}
