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
import javafx.util.Pair;

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
    private Image wallSheet;

    private long lastTime = 0;

    private long firstTime = 0;

    private KeyHandler keyHandler;

    @Override
    public void start(Stage stage) {
        spriteSheet = new Image("./tilesets/pacman-sprite-sheet.png");
        wallSheet = new Image("./tilesets/chompermazetiles.png");

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

            //Proof that action is sent to game controller
            /*if(ActionOfClock.size() != 0){
                for (Action a : ActionOfClock) {
                    System.out.println(a.getMove() +" "+ a.getClock());
                }
            }*/

            draw(time);

            ClientMain.clock++;

            prevTime = time;
        }

        private void draw(long time) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            drawMap();

            drawPlayerPosition(time);

            drawPoints();
        }

        private void drawPoints() {
            Player localPlayer = gameController.getLocalPlayer();
            if (localPlayer != null) {
                gc.setFill(Color.WHITE);
                gc.setFont(new javafx.scene.text.Font(20));
                gc.fillText("Score: " + localPlayer.getPoints(), 10, 25);
            }
        }

        private void drawMap() {
            TileType[][] tiles = gameState.tiles();
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[0].length; j++) {
                    switch (tiles[i][j]) {
                        case EMPTY:
                            //gc.setFill(Color.BLACK);
                            //gc.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case WALL:
                            drawWall(i, j);
                            break;
                        case PAC_DOT:
                            double pacDotSize = TILE_SIZE / 8.0;
                            gc.setFill(Color.YELLOW);
                            gc.fillRect(i * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0, j * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0, pacDotSize, pacDotSize);
                            break;
                        case CHERRY:
                            gc.drawImage(spriteSheet, 600, 0, 50, 50, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case STRAWBERRY:
                            gc.drawImage(spriteSheet, 600, 50, 50, 50, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case ORANGE:
                            gc.drawImage(spriteSheet, 600, 100, 50, 50, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case APPLE:
                            gc.drawImage(spriteSheet, 600, 150, 50, 50, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case MELON:
                            gc.drawImage(spriteSheet, 600, 200, 50, 50, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                    }

                    // Show which tile the player is on
                    /*int finalI = i;
                    int finalJ = j;
                    gameState.players().forEach(player -> {
                        Pair<Integer, Integer> playerGridPosition = player.getPosition().ToGridPosition();
                        System.out.println(playerGridPosition.getKey() + " " + playerGridPosition.getValue());
                        if (playerGridPosition.getKey() == finalI && playerGridPosition.getValue() == finalJ) {
                            gc.setFill(Color.DARKRED);
                            gc.fillRect(finalI * TILE_SIZE, finalJ * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    });*/
                }
            }
        }

        private void drawPlayerPosition(long time) {
            gameState.players().forEach(player -> {
                int sy = 0;
                switch (player.getDirection()) {
                    case WEST:
                        sy += 50 * 6;
                        break;
                    case NORTH:
                        sy += 50 * 9;
                        break;
                    case SOUTH:
                        sy += 50 * 3;
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

        private void drawWall(int i, int j) {
            TileType[][] tiles = gameState.tiles();
            final int N = tiles.length;
            final int M = tiles[0].length;

            boolean nWall = 0 > j-1 || tiles[i][j-1] != TileType.WALL;
            boolean wWall = 0 > i-1 || tiles[i-1][j] != TileType.WALL;
            boolean sWall = M <= j+1 || tiles[i][j+1] != TileType.WALL;
            boolean eWall = N <= i+1 || tiles[i+1][j] != TileType.WALL;

            if (wWall) {
                if (sWall) {
                    if (eWall) {
                        gc.drawImage(wallSheet, 32*6, 32*2, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    } else {
                        if (nWall) {
                            gc.drawImage(wallSheet, 32*7, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32*0, 32*2, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                } else {
                    if (eWall) {
                        if (nWall) {
                            gc.drawImage(wallSheet, 32*6, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32*6, 32, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    } else {
                        if (nWall) {
                            gc.drawImage(wallSheet, 32*0, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32*0, 32, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            } else {
                if (eWall) {
                    if (nWall) {
                        if (sWall) {
                            gc.drawImage(wallSheet, 32*9, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32*2, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    } else {
                        if (sWall) {
                            gc.drawImage(wallSheet, 32*2, 32*2, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32*2, 32, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                } else {
                    if (nWall) {
                        if (sWall) {
                            gc.drawImage(wallSheet, 32*8, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32, 32*0, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    } else {
                        if (sWall) {
                            gc.drawImage(wallSheet, 32, 32*2, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSheet, 32, 32, 32, 32, i*TILE_SIZE, j*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }
    }
}
