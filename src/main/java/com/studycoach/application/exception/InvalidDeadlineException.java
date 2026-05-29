package com.studycoach.application.exception;

import java.time.LocalDateTime;

public class InvalidDeadlineException extends StudyCoachValidationException {
    public InvalidDeadlineException(LocalDateTime deadline) {
        super("Deadline must be in the future. Provided: " + deadline);
    }
}

