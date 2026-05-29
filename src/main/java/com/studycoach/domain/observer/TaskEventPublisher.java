package com.studycoach.domain.observer;

public interface TaskEventPublisher {
    void register(TaskObserver observer);

    void unregister(TaskObserver observer);

    void publish(TaskUpdateEvent event);
}
