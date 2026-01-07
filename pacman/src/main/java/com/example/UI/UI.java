package com.example.UI;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Application;
public class UI extends Application {

     @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Label("Hello, JavaFX"), 300, 200);
        stage.setScene(scene);
        stage.show();
    }
}