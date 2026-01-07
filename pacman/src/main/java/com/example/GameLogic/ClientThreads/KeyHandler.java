package com.example.GameLogic.ClientThreads;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class KeyHandler  extends Application implements Runnable {

private static final double Speed = 3.0;

private final Set<String> pressedKeys = new HashSet<>();

@Override
public void start(Stage stage) {

    Circle player = new Circle();
    player.setCenterX(200f);
    player.setCenterY(200f);
    player.setRadius(50f);
    player.setFill(Color.YELLOW);

    Pane root = new Pane(player);
    Scene scene = new Scene(root, 800, 600);
    scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode().toString()));
    scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode().toString()));

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (pressedKeys.contains("UP")) {
                player.setCenterY(player.getCenterY() - Speed);
            }
            if (pressedKeys.contains("DOWN")) {
                player.setCenterY(player.getCenterY() + Speed);
            }
            if (pressedKeys.contains("LEFT")) {
                player.setCenterX(player.getCenterX() - Speed);
            }
            if (pressedKeys.contains("RIGHT")) {
                player.setCenterX(player.getCenterX() + Speed);
            }
        }
    };

    timer.start();
    stage.setScene(scene);
    stage.show();
    root.requestFocus();
}

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
 
}