package com.example.UI;

import java.util.List;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientGameController.RenderFrame;
import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.GameState;

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

    private ClientGameController gameController;
    private GameState gameState;

    private Canvas canvas;
    private GraphicsContext gc;
    private Image playerImage;

    @Override
    public void start(Stage stage) {
        int playerId = 0;
        gameController = new ClientGameController(playerId, 100, 100, Direction.SOUTH);
        gameState = new GameState();

        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);
        scene.setOnKeyPressed(e -> ClientMain.keyHandler.onKeyPressed(e.getCode()));
        stage.setScene(scene);

        canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        playerImage = new Image("./tilesets/pacman-sprite-sheet.png");

        stage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                List<Action> actionsOfClock = Constants.cleanActions.stream()
                        .filter(a -> a.getClock() == ClientMain.clock)
                        .toList();

                RenderFrame frame = gameController.tick(now, gameState, actionsOfClock);

                render(frame);

            
                ClientMain.clock++;
            }
        };
        timer.start();
    }

    private void render(RenderFrame frame) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.drawImage(
                playerImage,
                frame.sx, frame.sy, frame.sw, frame.sh,
                frame.dx, frame.dy, frame.dw, frame.dh
        );
    }
}
