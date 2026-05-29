package com.studycoach.application.port;

import com.studycoach.domain.model.StudyCoachState;

public interface StudyCoachRepository {
    StudyCoachState load();

    void save(StudyCoachState state);

    void initializeIfMissing();
}
