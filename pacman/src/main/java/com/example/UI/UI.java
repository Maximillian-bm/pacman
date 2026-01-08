package com.example.UI;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientMain;
import com.example.GameLogic.ClientThreads.KeyHandlerOnline;
import com.example.GameLogic.ClientThreads.KeyHandler;
import com.example.model.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.example.model.Constants.TILE_SIZE;
import static com.example.model.Constants.TARGET_FPS;

public class UI extends Application {
    private final ClientGameController gameController = new ClientGameController();
    private GameState gameState;

    private GraphicsContext gc;
    private Canvas canvas;

    private Image spriteSheet;

    private long lastTime = 0;

    private long firstTime = 0;

    private KeyHandler keyHandler;

    @Override
    public void start(Stage stage) {
        spriteSheet = new Image("./tilesets/pacman-sprite-sheet.png");

        final Group root = new Group();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        if(Constants.online){
            keyHandler = new KeyHandlerOnline();
        }else{
            keyHandler = new KeyHandler();
        }

        scene.setOnKeyPressed(e -> keyHandler.move(e.getCode()));

        stage.setScene(scene);

        canvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        final AnimationTimer tm = new GameAnimator();
        tm.start();

        gc = canvas.getGraphicsContext2D();

        stage.show();
    }

    private class GameAnimator extends AnimationTimer {
        long prevTime = 0;

        @Override
        public void handle(long time) {
            if (prevTime != 0 && (time - prevTime) < (1000000000 / TARGET_FPS)) {
                return;
            }

            List<Action> ActionOfClock = Constants.cleanActions.stream()
                .filter(e -> e.getClock() == ClientMain.clock)
                .toList();
            gameState = gameController.updateGameState(gameState, ActionOfClock);

            draw(time);

            ClientMain.clock++;

            prevTime = time;
        }

        private void draw(long time) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            drawMap();

            drawPlayerPosition(time);
        }

        private void drawMap() {
            TileType[][] tiles = gameState.tiles();
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[0].length; j++) {
                    switch (tiles[i][j]) {
                        case EMPTY:
                            gc.setFill(Color.BLACK);
                            gc.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case WALL:
                            gc.setFill(Color.DARKBLUE);
                            gc.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case PAC_DOT:
                            gc.setFill(Color.YELLOW);
                            gc.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                    }
                }
            }
        }

        private void drawPlayerPosition(long time) {
            gameState.players().forEach(player -> {
                int sy = 0;
                switch (player.getDirection()) {
                    case WEST:
                        sy += 50 * 5;
                        break;
                    case NORTH:
                        sy += 50 * 8;
                        break;
                    case SOUTH:
                        sy += 50 * 2;
                        break;
                }

                Position playerPos = player.getPosition();

                int pacmanFrame = (int)(time / 75000000) % 4;

                int syf = switch (pacmanFrame) {
                    case 0 -> sy;
                    case 2 -> sy + 50 * 2;
                    default -> sy + 50;
                };

                gc.drawImage(spriteSheet, 850, syf, 50, 50, playerPos.x, playerPos.y, TILE_SIZE, TILE_SIZE);
            });

            lastTime = time;
        }
    }
}
