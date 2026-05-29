package com.studycoach.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoach.application.port.StudyCoachRepository;
import com.studycoach.domain.model.AppConfig;
import com.studycoach.domain.model.Course;
import com.studycoach.domain.model.DailyPlan;
import com.studycoach.domain.model.Exam;
import com.studycoach.domain.model.StudyCoachState;
import com.studycoach.domain.model.Student;
import com.studycoach.domain.model.StudySession;
import com.studycoach.domain.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class JsonStudyCoachRepository implements StudyCoachRepository {
    private final Path dataDirectory;
    private final ObjectMapper objectMapper;

    public JsonStudyCoachRepository(Path dataDirectory, ObjectMapper objectMapper) {
        this.dataDirectory = dataDirectory;
        this.objectMapper = objectMapper;
    }

    @Override
    public StudyCoachState load() {
        try {
            return new StudyCoachState(
                    readFile("student.json", Student.class),
                    readList("courses.json", new TypeReference<>() {}),
                    readList("tasks.json", new TypeReference<>() {}),
                    readList("exams.json", new TypeReference<>() {}),
                    readList("history.json", new TypeReference<>() {}),
                    readList("plans.json", new TypeReference<>() {}),
                    readFile("config.json", AppConfig.class)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load study coach data", exception);
        }
    }

    @Override
    public void save(StudyCoachState state) {
        try {
            writeAtomic("student.json", state.student());
            writeAtomic("courses.json", state.courses());
            writeAtomic("tasks.json", state.tasks());
            writeAtomic("exams.json", state.exams());
            writeAtomic("history.json", state.studyHistory());
            writeAtomic("plans.json", state.generatedPlans());
            writeAtomic("config.json", state.config());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist study coach data", exception);
        }
    }

    @Override
    public void initializeIfMissing() {
        try {
            Files.createDirectories(dataDirectory);
            copyIfMissing("student.json");
            copyIfMissing("courses.json");
            copyIfMissing("tasks.json");
            copyIfMissing("exams.json");
            copyIfMissing("history.json");
            copyIfMissing("plans.json");
            copyIfMissing("config.json");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to bootstrap sample data", exception);
        }
    }

    private <T> T readFile(String fileName, Class<T> type) throws IOException {
        return objectMapper.readValue(dataDirectory.resolve(fileName).toFile(), type);
    }

    private <T> List<T> readList(String fileName, TypeReference<List<T>> type) throws IOException {
        return objectMapper.readValue(dataDirectory.resolve(fileName).toFile(), type);
    }

    private void writeAtomic(String fileName, Object value) throws IOException {
        // Temporary files plus atomic move keep the desktop data store resilient against interrupted writes.
        Path tempFile = dataDirectory.resolve(fileName + ".tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), value);
        Files.move(tempFile, dataDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void copyIfMissing(String fileName) throws IOException {
        Path target = dataDirectory.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        try (InputStream inputStream = getClass().getResourceAsStream("/sample-data/" + fileName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing bundled resource: " + fileName);
            }
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
