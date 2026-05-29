package com.studycoach.application.exception;

public class UnknownTaskException extends StudyCoachValidationException {
    public UnknownTaskException(String taskId) {
        super("Unknown task: " + taskId);
    }
}

