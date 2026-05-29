package com.studycoach.application.service;

import com.studycoach.domain.observer.TaskObserver;
import com.studycoach.domain.observer.TaskUpdateEvent;

import java.util.ArrayList;
import java.util.List;

public class ProgressTracker implements TaskObserver {
    private final List<TaskUpdateEvent> events = new ArrayList<>();

    @Override
    public void onTaskUpdated(TaskUpdateEvent event) {
        events.add(event);
    }

    public List<TaskUpdateEvent> recentEvents() {
        return List.copyOf(events);
    }
}
