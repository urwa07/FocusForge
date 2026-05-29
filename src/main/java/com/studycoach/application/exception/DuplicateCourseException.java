package com.studycoach.application.exception;

public class DuplicateCourseException extends StudyCoachValidationException {
    public DuplicateCourseException(String courseId) {
        super("Course already exists: " + courseId);
    }
}

