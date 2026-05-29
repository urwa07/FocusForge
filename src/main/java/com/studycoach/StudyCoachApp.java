package com.studycoach;

import com.studycoach.application.service.StudyCoachService;
import com.studycoach.infrastructure.persistence.JsonStudyCoachRepository;
import com.studycoach.infrastructure.persistence.ObjectMapperFactory;
import com.studycoach.presentation.controller.MainController;
import com.studycoach.presentation.viewmodel.MainViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class StudyCoachApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Path dataDir = Path.of(System.getProperty("user.home"), ".ai-study-coach");
        StudyCoachService service = new StudyCoachService(new JsonStudyCoachRepository(dataDir, ObjectMapperFactory.create()));
        MainViewModel viewModel = new MainViewModel(service);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.bind(viewModel);

        Scene scene = new Scene(root, 1460, 920);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setTitle("FocusForge - Deep Work Companion");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
