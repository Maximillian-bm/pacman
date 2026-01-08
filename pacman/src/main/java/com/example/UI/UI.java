package com.example.UI;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientMain;
import com.example.GameLogic.GameController;
import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.GameState;

import javafx.application.Application;
public class UI extends Application {

    @Override
    public void start(Stage stage) {

        ClientGameController gameController = new ClientGameController();
        GameState gameState = new GameState();

        Scene scene = new Scene(new Label("Hello, JavaFX"), 300, 200);
        stage.setScene(scene);
        stage.show();

        while(true){
            List<Action> ActionOfClock = Constants.cleanActions.stream()
            .filter(e -> e.getClock() == ClientMain.clock)
            .toList();
            gameState = gameController.updateGameState(gameState, ActionOfClock);
        }
    }
}
