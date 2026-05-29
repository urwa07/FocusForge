package com.studycoach.domain.service;

import com.studycoach.domain.observer.TaskEventPublisher;
import com.studycoach.domain.observer.TaskObserver;
import com.studycoach.domain.observer.TaskUpdateEvent;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskEventPublisher implements TaskEventPublisher {
    private final List<TaskObserver> observers = new ArrayList<>();

    @Override
    public void register(TaskObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(TaskObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void publish(TaskUpdateEvent event) {
        observers.forEach(observer -> observer.onTaskUpdated(event));
    }
}
