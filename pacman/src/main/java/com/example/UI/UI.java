package com.example.UI;

import java.util.List;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class UI extends Application {

    private ClientGameController controller;
    private GameState gameState;

    private Canvas canvas;
    private GraphicsContext gc;
    private Image playerImage;

    private long lastTime = 0;

    @Override
    public void start(Stage stage) {
        int playerId = 0;

        controller = new ClientGameController(playerId, 100, 100, Direction.SOUTH);
        gameState = new GameState();

        playerImage = new Image("./tilesets/pacman-sprite-sheet.png");

        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        // Forward JavaFX input to KeyHandler
        scene.setOnKeyPressed(e -> {
            if (ClientMain.keyHandler != null) {
                ClientMain.keyHandler.onKeyPressed(e.getCode());
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                long deltaTime = (lastTime == 0) ? 0 : (now - lastTime);
                lastTime = now;

                // Apply actions (no clock filtering for now)
                List<Action> actions = List.copyOf(Constants.cleanActions);
                controller.updateGameState(gameState, actions);

                // Move player
                controller.stepMovement(deltaTime);

                // Render
                render(now, controller.getPlayer());

                ClientMain.clock++;
            }
        };
        timer.start();

        stage.setTitle("Client");
        stage.show();
    }

    private void render(long timeNanos, Player player) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Position pos = player.getPosition();
        Direction dir = player.getDirection();

        // Sprite sheet constants
        int sx = 757;
        int sw = 32;
        int sh = 32;

        int syBase = 42;
        int sy = syBase;

        switch (dir) {
            case WEST  -> sy = syBase + 43 * 6;
            case EAST  -> sy = syBase;
            case NORTH -> sy = syBase + 43 * 9;
            case SOUTH -> sy = syBase + 43 * 3;
        }

        long frame = (timeNanos / 200_000_000L) % 4;
        int drawSy;

        if (frame == 0) {
            drawSy = syBase;
        } else if (frame == 1 || frame == 3) {
            drawSy = sy + 43;
        } else {
            drawSy = sy + 43 * 2;
        }

        gc.drawImage(
                playerImage,
                sx, drawSy, sw, sh,
                pos.x, pos.y, 100, 100
        );
    }
}
