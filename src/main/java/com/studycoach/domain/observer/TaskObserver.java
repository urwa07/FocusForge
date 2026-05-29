package com.studycoach.domain.observer;

public interface TaskObserver {
    void onTaskUpdated(TaskUpdateEvent event);
}
