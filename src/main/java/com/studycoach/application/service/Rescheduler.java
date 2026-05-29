package com.studycoach.application.service;

import com.studycoach.domain.observer.TaskObserver;
import com.studycoach.domain.observer.TaskUpdateEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class Rescheduler implements TaskObserver {
    private final AtomicBoolean rescheduleRequested = new AtomicBoolean(false);

    @Override
    public void onTaskUpdated(TaskUpdateEvent event) {
        rescheduleRequested.set(true);
    }

    public boolean consumeRescheduleRequest() {
        return rescheduleRequested.getAndSet(false);
    }
}
