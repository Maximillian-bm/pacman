package com.example.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientMain;
import com.example.GameLogic.ClientThreads.KeyHandler;
import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.GameState;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.Group;
import javafx.animation.AnimationTimer;

import com.example.model.Player;
import com.example.model.Position;
import com.example.model.Direction;

public class UI extends Application {
    private final ClientGameController gameController = new ClientGameController();
    private GameState gameState;

    private GraphicsContext gc;
    private Canvas canvas;

    private Image playerImage;
    private Player player;

    private final Set<KeyCode> down = EnumSet.noneOf(KeyCode.class);

    //private KeyHandler keyHandler = new KeyHandler(down);

    @Override
    public void start(Stage stage) {
        player = new Player(0);
        Position pos = new Position();
        pos.x = 100;
        pos.y = 100;
        player.setPosition(pos);
        player.setDirection(Direction.SOUTH);

        playerImage = new Image("./tilesets/pacman-sprite-sheet.png");

        final Group root = new Group();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        scene.setOnKeyPressed(e -> down.add(e.getCode()));
        scene.setOnKeyReleased(e -> down.remove(e.getCode()));

        //keyHandler.run();

        stage.setScene(scene);

        canvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        final AnimationTimer tm = new GameAnimator();
        tm.start();

        gc = canvas.getGraphicsContext2D();

        stage.show();
    }

    private class GameAnimator extends AnimationTimer {
        private long lastTime;

        @Override
        public void handle(long time) {
            List<Action> ActionOfClock = Constants.cleanActions.stream()
                .filter(e -> e.getClock() == ClientMain.clock)
                .toList();
            gameState = gameController.updateGameState(gameState, ActionOfClock);

            switch(player.getDirection()) {
                case WEST: drawPlayerPosition(-1, 0, time); break;
                case EAST: drawPlayerPosition(1, 0, time); break;
                case NORTH: drawPlayerPosition(0, -1, time); break;
                case SOUTH: drawPlayerPosition(0, 1, time); break;
            }
        }

        private void drawPlayerPosition(int x, int y, long time) {
            long deltaTime;
            if (lastTime == 0) {
                deltaTime = 0;
            } else {
                deltaTime = time - lastTime;
            }

            System.out.println(deltaTime);
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            Position pos = player.getPosition();
            pos.x += x * (deltaTime / 4000000);
            pos.y += y * (deltaTime / 4000000);
            player.setPosition(pos);

            int sy = 42;
            switch (player.getDirection()) {
                case WEST: sy += 43*6; break;
                case EAST: ; break;
                case NORTH: sy += 43*9; break;
                case SOUTH: sy += 43*3; break;
            }

            long pacmanFrame = (time / 200000000) % 4;
            if (pacmanFrame == 0) {
                gc.drawImage(playerImage, 757, 42, 32, 32, pos.x, pos.y, 100, 100);
            } else if (pacmanFrame == 1 || pacmanFrame == 3) {
                gc.drawImage(playerImage, 757, sy+43, 32, 32, pos.x, pos.y, 100, 100);
            } else {
                gc.drawImage(playerImage, 757, sy+43*2, 32, 32, pos.x, pos.y, 100, 100);
            }

            lastTime = time;
        }
    }
}
