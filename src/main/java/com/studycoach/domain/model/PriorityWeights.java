package com.studycoach.domain.model;

public record PriorityWeights(
        double deadlineWeight,
        double difficultyWeight,
        double importanceWeight,
        double behaviorWeight
) {
}
