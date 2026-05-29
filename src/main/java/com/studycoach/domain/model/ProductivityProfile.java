package com.studycoach.domain.model;

import java.util.List;

public record ProductivityProfile(List<TimeWindow> preferredWindows, BehaviorPattern dominantPattern) {
}
