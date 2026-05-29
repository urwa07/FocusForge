package com.studycoach.application.exception;

public class UnknownCourseException extends StudyCoachValidationException {
    public UnknownCourseException(String courseId) {
        super("Unknown course: " + courseId);
    }
}

