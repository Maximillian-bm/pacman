package com.example.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.example.GameLogic.ClientGameController;
import com.example.GameLogic.ClientMain;
import com.example.GameLogic.ClientThreads.KeyHandler;
import com.example.GameLogic.ClientThreads.KeyHandlerOffline;
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
import com.example.model.TileType;

import static com.example.model.Constants.TILE_SIZE;

public class UI extends Application {
    private final ClientGameController gameController = new ClientGameController();
    private GameState gameState;

    private GraphicsContext gc;
    private Canvas canvas;

    private Image playerImage;
    private Player player;

    private final Set<KeyCode> down = EnumSet.noneOf(KeyCode.class);

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

        if(Constants.online){
            KeyHandler keyHandler = new KeyHandler(down);
            Thread t = new Thread(keyHandler);
            t.setDaemon(true);;
            t.start();
        }else{
            KeyHandlerOffline keyHandler = new KeyHandlerOffline(down);
            Thread t = new Thread(keyHandler);
            t.setDaemon(true);;
            t.start();
        }

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

            // System.out.println(deltaTime);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            TileType[][] tiles = gameState.tiles();
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[0].length; j++) {
                    switch(tiles[i][j]) {
                        case EMPTY:
                            gc.setFill(Color.BLACK);
                            gc.fillRect(i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case WALL:
                            gc.setFill(Color.DARKBLUE);
                            gc.fillRect(i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                    }
                }
            }

            Position pos = player.getPosition();
            pos.x += x * (deltaTime / 2000000);
            pos.y += y * (deltaTime / 2000000);
            player.setPosition(pos);

            int sy = 0;
            switch (player.getDirection()) {
                case WEST: sy += 50*6; break;
                case EAST: ; break;
                case NORTH: sy += 50*9; break;
                case SOUTH: sy += 50*3; break;
            }

            long pacmanFrame = (time / 200000000) % 4;
            if (pacmanFrame == 0) {
                gc.drawImage(playerImage, 850, 50, 50, 50, pos.x, pos.y, TILE_SIZE, TILE_SIZE);
            } else if (pacmanFrame == 1 || pacmanFrame == 3) {
                gc.drawImage(playerImage, 850, sy+50, 50, 50, pos.x, pos.y, TILE_SIZE, TILE_SIZE);
            } else {
                gc.drawImage(playerImage, 850, sy+50*2, 50, 50, pos.x, pos.y, TILE_SIZE, TILE_SIZE);
            }

            lastTime = time;
        }
    }
}
