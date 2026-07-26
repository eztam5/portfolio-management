package com.example.portfoliomanagement;

import com.example.portfoliomanagement.persistence.LiquibaseMigrationRunner;
import com.example.portfoliomanagement.persistence.PersistenceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        LiquibaseMigrationRunner.runMigrations();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("MainWindowView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1040, 640);
        stage.setMinWidth(760);
        stage.setMinHeight(420);
        stage.setTitle("Portfolio Management");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        PersistenceManager.close();
    }
}
