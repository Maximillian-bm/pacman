package com.example.GUI;

import static com.example.model.Constants.TARGET_FPS;
import static com.example.model.Constants.TILES_TALL;
import static com.example.model.Constants.TILES_WIDE;
import static com.example.model.Constants.TILE_SIZE;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.KeyHandler;
import com.example.GameLogic.ClientGameController;
import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;
import com.example.model.Sound;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.stream.Collectors;

public class UI extends Application {

    // private final SoundEngine soundEngine = new SoundEngine();

    private final ConnectToLobby lobbyHandler = new ConnectToLobby();

    private final ClientGameController gameController = new ClientGameController();
    private GameState gameState;
    private GameState savedState;

    private GraphicsContext gc;
    private Canvas canvas;

    private final Image spriteSheet = new Image(
            Objects.requireNonNull(getClass().getResource("/tilesets/pacman-sprite-sheet.png")).toExternalForm());
    private final Image wallSpriteSheet = new Image(
            Objects.requireNonNull(getClass().getResource("/tilesets/chompermazetiles.png")).toExternalForm());

    private KeyHandler keyHandler;

    private Runnable createLobby;

    private Text notificationText;

    record TilePos(int x, int y) {
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pacman");
        initializeLobby(stage);
    }

    private void initializeLobby(Stage stage) {
        Canvas backgroundCanvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        GraphicsContext bgGc = backgroundCanvas.getGraphicsContext2D();
        bgGc.setFill(Color.BLACK);
        bgGc.fillRect(0, 0, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        drawRectangle(bgGc, 0, 0, TILES_WIDE, TILES_TALL);

        Button joinLobbyButton = createTiledButton("Join Lobby", 6, 2);

        Text joinedLobbyText = new Text("");
        joinedLobbyText.setFill(Color.WHITE);
        joinedLobbyText.setStyle("-fx-font-size: 14px;");

        Text errorText = new Text("");
        errorText.setId("errorText");
        errorText.setFill(Color.RED);
        errorText.setStyle("-fx-font-size: 14px;");

        notificationText = new Text("");
        notificationText.setId("notificationText");
        notificationText.setFill(Color.YELLOW);
        notificationText.setStyle("-fx-font-size: 14px;");

        Text playerCountText = new Text("Select number of players:");
        playerCountText.setFill(Color.WHITE);
        playerCountText.setStyle("-fx-font-size: 14px;");
        ChoiceBox playerCountChoices = new ChoiceBox();
        playerCountChoices.getItems().add("1");
        playerCountChoices.getItems().add("2");
        playerCountChoices.getItems().add("3");
        playerCountChoices.getItems().add("4");
        playerCountChoices.setValue("1");
        HBox createLobbyH = new HBox(
                playerCountText,
                playerCountChoices);
        createLobbyH.setAlignment(Pos.CENTER);

        Button createLobbyButton = createTiledButton("Create Lobby", 6, 2);
        VBox createLobbyV = new VBox(
                createLobbyH,
                createLobbyButton);
        createLobbyV.setAlignment(Pos.CENTER);

        Button startButton = createTiledButton("Start Game", 6, 2);

        Text LobbyIDText = new Text("Lobby ID: ");
        LobbyIDText.setFill(Color.WHITE);
        LobbyIDText.setStyle("-fx-font-size: 14px;");
        TextField lobbyIDInput = new TextField();
        lobbyIDInput.setMaxWidth(6 * TILE_SIZE);
        HBox joinLobbyH = new HBox(
                LobbyIDText,
                lobbyIDInput);
        joinLobbyH.setAlignment(Pos.CENTER);

        VBox joinLobbyV = new VBox(
                joinLobbyH,
                joinLobbyButton,
                errorText);
        joinLobbyV.setAlignment(Pos.CENTER);

        Text header = new Text("Pacman");
        header.setFill(Color.WHITE);
        header.setStyle("-fx-font: 48 arial;");

        VBox startRoot = new VBox(
                header,
                joinLobbyV,
                createLobbyV,
                notificationText);
        startRoot.setAlignment(Pos.CENTER);
        startRoot.setSpacing(48);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundCanvas, startRoot);

        Scene startScene = new Scene(
                root,
                Constants.INIT_SCREEN_WIDTH,
                Constants.INIT_SCREEN_HEIGHT);

        joinLobbyButton.setOnAction(e -> {
            // soundEngine.play(Sound.EAT_FRUIT);
            String input = lobbyIDInput.getText();
            System.out.println("Connecting to: " + input);
            errorText.setText("");
            joinLobbyButton.setDisable(true);

            new Thread(() -> {
                try {
                    lobbyHandler.joinLobby(input);
                    javafx.application.Platform.runLater(() -> {
                        startRoot.getChildren().remove(joinLobbyV);
                        startRoot.getChildren().remove(createLobbyV);
                        joinedLobbyText.setText("Joined lobby with ID: " + input);
                        startRoot.getChildren().add(startButton);
                        startRoot.getChildren().add(joinedLobbyText);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorText.setText(ex.getMessage());
                        joinLobbyButton.setDisable(false);
                        System.err.println("--- Join Failed ---");
                        System.err.println(ex.getMessage());
                    });
                }
            }).start();
        });

        createLobby = () -> {
            if (playerCountChoices.getValue() == null) {
                return;
            }
            String playerCount = playerCountChoices.getValue().toString();

            System.out.println("Creating lobby with " + playerCount + " number of player");

            lobbyHandler.createLobby(Integer.parseInt(playerCount));

            startRoot.getChildren().remove(joinLobbyV);
            startRoot.getChildren().remove(createLobbyV);

            joinedLobbyText.setText("Joined lobby with ID: " + lobbyHandler.getLobbyID());
            startRoot.getChildren().add(startButton);
            startRoot.getChildren().add(joinedLobbyText);
        };

        createLobbyButton.setOnAction(e -> {
            createLobby.run();
            // soundEngine.play(Sound.EAT_FRUIT);
        });

        startButton.setOnAction(e -> {
            startLobby(stage);
            // soundEngine.play(Sound.START_MUSIC);
        });

        stage.setScene(startScene);

        stage.sizeToScene();
        stage.setResizable(false);

        stage.show();
    }

    public void notifyDisconnection(String message) {
        javafx.application.Platform.runLater(() -> {
            if (notificationText != null) {
                notificationText.setText(message);
            }
        });
    }

    private void startLobby(Stage stage) {
        lobbyHandler.startGame();
        startGame(stage);
    }

    private void startGame(Stage stage) {
        gameState = gameController.initializeGameState(lobbyHandler.getNrOfPlayers());
        savedState = gameState;

        final Group root = new Group();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        Constants.cleanActions.setPlayerID(lobbyHandler.getPlayerID());
        keyHandler = new KeyHandler(lobbyHandler.getLobbyID(), lobbyHandler.getPlayerID());

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
        long startTime = 0;

        @Override
        public void handle(long time) {
            if (startTime == 0) {
                startTime = time;
            }
            if (time - (startTime + Constants.timeOffset) < (Constants.clock + Constants.COUNTDOWN_DURATION_TICKS)
                    * (1000000000 / TARGET_FPS)) {
                return;
            }
            if (Constants.clock < 0) {
                draw(time);
                drawCountdown();
                Constants.clock++;
                return;
            }

            List<Action> ActionOfClock = Constants.cleanActions.getActions(Constants.clock);
            if (Constants.cleanActions.missedAction()) {
                gameState = gameController.updateGameStateFor(savedState, Constants.clock);
                Constants.cleanActions.fixedMissedAction();
            } else {
                if (!ActionOfClock.isEmpty())
                    savedState = gameState;
                gameState = gameController.updateGameState(gameState, ActionOfClock);
            }

            // Proof that action is sent to game controller
            /*
             * if(ActionOfClock.size() != 0){
             * for (Action a : ActionOfClock) {
             * System.out.println(a.getMove() +" "+ a.getClock());
             * }
             * }
             */

            draw(time);

            Constants.clock++;
        }

        private void draw(long time) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            drawMap();

            drawPlayers(time);

            drawGhosts(time);

            if (gameState.winner() == null) {
                drawPoints();
            } else {
                drawEndscreen();
            }
        }

        private void drawEndscreen() {
            gc.setFill(Color.GRAY);
            int padding = 150;
            gc.fillRect(
                Constants.INIT_SCREEN_WIDTH/2-padding,
                Constants.INIT_SCREEN_HEIGHT/2-padding,
                padding*2,
                padding*2
            );
            gc.setFont(new javafx.scene.text.Font(24));
            List<Player> players = gameState.players()
                    .stream()
                    .sorted(Comparator.comparing((Player p) -> p.getPoints()))
                        .collect(Collectors.toList());
            Collections.reverse(players);
            for (int i = 0; i < players.size(); i++) {
                gc.setFill(players.get(i).getColor());
                gc.fillText("Score: " + players.get(i).getPoints(), 
                    Constants.INIT_SCREEN_WIDTH/2-padding+110, 
                    Constants.INIT_SCREEN_HEIGHT/2-padding+(i+2)*40
                );
            }
        }

        private void drawCountdown() {
            Color playerColor = gameState.players().get(lobbyHandler.getPlayerID()).getColor();
            float seconds = -1 * (float) (Constants.clock) / Constants.TARGET_FPS;

            gc.setFill(playerColor);
            gc.setFont(new javafx.scene.text.Font(80));
            gc.fillText((int)seconds + "", Constants.INIT_SCREEN_WIDTH / 2 - 20, Constants.INIT_SCREEN_HEIGHT / 2 - 40);
            gc.setFont(new javafx.scene.text.Font(80));
            gc.fillText((int)seconds + "", Constants.INIT_SCREEN_WIDTH / 2 - 20, Constants.INIT_SCREEN_HEIGHT / 2 - 40);

            gc.fillRect(0, 0, Constants.INIT_SCREEN_WIDTH, 4);
            gc.fillRect(0, 0, 4, Constants.INIT_SCREEN_HEIGHT);
            gc.fillRect(Constants.INIT_SCREEN_WIDTH-4, 0, 4, Constants.INIT_SCREEN_HEIGHT);
            gc.fillRect(0, Constants.INIT_SCREEN_HEIGHT-4, Constants.INIT_SCREEN_WIDTH, 4);
        }

        private void drawPoints() {
            List<Player> players = gameState.players();
            for (int i = 0; i < players.size(); i++) {
                gc.setFill(players.get(i).getColor());
                gc.setFont(new javafx.scene.text.Font(20));

                StringBuilder hearts = new StringBuilder("");
                for (int j = 0; j < players.get(i).getLives(); j++) {
                    hearts.append("♡ ");
                }
                for (int j = 0; j < 5 - players.get(i).getLives(); j++) {
                    hearts.append("   ");
                }

                gc.fillText(hearts + " Score: " + players.get(i).getPoints(), 10, (i + 1) * 20);
            }
        }

        private void drawMap() {
            TileType[][] tiles = gameState.tiles();
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[0].length; j++) {
                    switch (tiles[i][j]) {
                        case EMPTY:
                            // gc.setFill(Color.BLACK);
                            // gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case WALL:
                            drawWall(i, j);
                            break;
                        case PAC_DOT:
                            double pacDotSize = TILE_SIZE / 8.0;
                            gc.setFill(Color.YELLOW);
                            gc.fillRect(i * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0,
                                    j * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0, pacDotSize, pacDotSize);
                            break;
                        case CHERRY:
                            gc.drawImage(spriteSheet, 600, 0, 50, 50, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case STRAWBERRY:
                            gc.drawImage(spriteSheet, 600, 50, 50, 50, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case ORANGE:
                            gc.drawImage(spriteSheet, 600, 100, 50, 50, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case APPLE:
                            gc.drawImage(spriteSheet, 600, 150, 50, 50, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case MELON:
                            gc.drawImage(spriteSheet, 600, 200, 50, 50, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case ENERGIZER:
                            gc.drawImage(spriteSheet, 415, 415, 25, 25, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;

                    }

                    // Show which tile the player is on
                    /*
                     * int finalI = x;
                     * int finalJ = y;
                     * gameState.players().forEach(player -> {
                     * Pair<Integer, Integer> playerGridPosition =
                     * player.getPosition().ToGridPosition();
                     * System.out.println(playerGridPosition.getKey() + " " +
                     * playerGridPosition.getValue());
                     * if (playerGridPosition.getKey() == finalI && playerGridPosition.getValue() ==
                     * finalJ) {
                     * gc.setFill(Color.DARKRED);
                     * gc.fillRect(finalI * TILE_SIZE, finalJ * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                     * }
                     * });
                     */
                }
            }
        }

        private void drawPlayers(long time) {
            gameState.players().forEach(player -> {
                int sy = switch (player.getDirection()) {
                    case WEST -> 50 * 6;
                    case NORTH -> 50 * 9;
                    case SOUTH -> 50 * 3;
                    default -> 0;
                };

                int pacmanFrame = (int) (time / 75000000) % 4;
                sy = switch (pacmanFrame) {
                    case 0 -> sy;
                    case 2 -> sy + 50 * 2;
                    default -> sy + 50;
                };

                Image coloredPlayer = colorPlayer(player.getColor());
                Position playerTilePos = player.getPosition();
                gc.drawImage(coloredPlayer, 850, sy, 50, 50, playerTilePos.x, playerTilePos.y, TILE_SIZE, TILE_SIZE);
            });
        }

        // Modified function from:
        // https://stackoverflow.com/questions/18124364/how-to-change-color-of-image-in-javafx
        public Image colorPlayer(Color color) {
            // public Image colorPlayer(int nr, int ng, int nb) {
            int W = (int) spriteSheet.getWidth();
            int H = (int) spriteSheet.getHeight();
            WritableImage outputImage = new WritableImage(W, H);
            PixelReader reader = spriteSheet.getPixelReader();
            PixelWriter writer = outputImage.getPixelWriter();
            int nr = (int) (color.getRed() * 255);
            int ng = (int) (color.getGreen() * 255);
            int nb = (int) (color.getBlue() * 255);
            // Yellow (the player)
            int or = 255;
            int og = 241;
            int ob = 0;
            for (int y = 0; y < H; y++) {
                for (int x = 850; x < 900; x++) {
                    int argb = reader.getArgb(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    if (g == og && r == or && b == ob) {
                        r = nr;
                        g = ng;
                        b = nb;
                    }
                    argb = (a << 24) | (r << 16) | (g << 8) | b;
                    writer.setArgb(x, y, argb);
                }
            }
            return outputImage;
        }

        private void drawGhosts(long time) {
            gameState.ghosts().forEach(ghost -> {
                int sy = 0, sx = 0;
                switch (ghost.getDirection()) {
                    case WEST:
                        sy += 50 * 4;
                        break;
                    case NORTH:
                        sy += 50 * 6;
                        break;
                    case SOUTH:
                        sy += 50 * 2;
                        break;
                }

                sx = switch (ghost.getType()) {
                    case RED -> 0; // ("Blinky"),
                    case PINK -> 50; // ("Pinky"),
                    case CYAN -> 100; // ("Inky"),
                    case ORANGE -> 150; // ("Clyde"),
                    case PURPLE -> 250; // ("Sue");
                };

                double fTimer = ghost.getFrightenedTimerSec();

                if (fTimer > 0) {
                    sy += 50 * 11;
                    sx = 0;
                }

                int ghostFrame = (int) (time / 300000000) % 2;
                if (ghostFrame == 1) {
                    sy += 50;
                    if (fTimer > 0 && fTimer < 2.0)
                        sx += 50;
                }

                Position ghostTilePos = ghost.getPosition();
                gc.drawImage(spriteSheet, sx, sy, 50, 50, ghostTilePos.x, ghostTilePos.y, TILE_SIZE, TILE_SIZE);
            });
        }

        private void drawWall(int i, int j) {
            TileType[][] tiles = gameState.tiles();
            final int N = tiles.length;
            final int M = tiles[0].length;

            boolean nWall = 0 > j - 1 || tiles[i][j - 1] != TileType.WALL;
            boolean wWall = 0 > i - 1 || tiles[i - 1][j] != TileType.WALL;
            boolean sWall = M <= j + 1 || tiles[i][j + 1] != TileType.WALL;
            boolean eWall = N <= i + 1 || tiles[i + 1][j] != TileType.WALL;

            boolean neWall = 0 > j - 1 || N <= i + 1 || tiles[i + 1][j - 1] != TileType.WALL;
            boolean nwWall = 0 > j - 1 || 0 > i - 1 || tiles[i - 1][j - 1] != TileType.WALL;
            boolean seWall = M <= j + 1 || N <= i + 1 || tiles[i + 1][j + 1] != TileType.WALL;
            boolean swWall = M <= j + 1 || 0 > i - 1 || tiles[i - 1][j + 1] != TileType.WALL;

            if (wWall) {
                if (sWall) {
                    if (eWall) {
                        gc.drawImage(wallSpriteSheet, 32 * 6, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                TILE_SIZE);
                    } else {
                        if (nWall) {
                            gc.drawImage(wallSpriteSheet, 32 * 7, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE);
                        } else {
                            if (neWall) {
                                gc.drawImage(wallSpriteSheet, 32 * 3, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            } else {
                                gc.drawImage(wallSpriteSheet, 32 * 0, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            }
                        }
                    }
                } else {
                    if (eWall) {
                        if (nWall) {
                            gc.drawImage(wallSpriteSheet, 32 * 6, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSpriteSheet, 32 * 6, 32, 32, 32, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                        }
                    } else {
                        if (nWall) {
                            if (seWall) {
                                gc.drawImage(wallSpriteSheet, 32 * 3, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            } else {
                                gc.drawImage(wallSpriteSheet, 32 * 0, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            }
                        } else {
                            gc.drawImage(wallSpriteSheet, 32 * 0, 32, 32, 32, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                        }
                    }
                }
            } else {
                if (eWall) {
                    if (nWall) {
                        if (sWall) {
                            gc.drawImage(wallSpriteSheet, 32 * 9, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE);
                        } else {
                            if (swWall) {
                                gc.drawImage(wallSpriteSheet, 32 * 5, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            } else {
                                gc.drawImage(wallSpriteSheet, 32 * 2, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            }
                        }
                    } else {
                        if (sWall) {
                            if (nwWall) {
                                gc.drawImage(wallSpriteSheet, 32 * 5, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            } else {
                                gc.drawImage(wallSpriteSheet, 32 * 2, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                        TILE_SIZE, TILE_SIZE);
                            }
                        } else {
                            gc.drawImage(wallSpriteSheet, 32 * 2, 32, 32, 32, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                        }
                    }
                } else {
                    if (nWall) {
                        if (sWall) {
                            gc.drawImage(wallSpriteSheet, 32 * 8, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE);
                        } else {
                            gc.drawImage(wallSpriteSheet, 32 * 4, 32 * 0, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE);
                        }
                    } else {
                        if (sWall) {
                            gc.drawImage(wallSpriteSheet, 32, 32 * 2, 32, 32, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                        } else {
                            if (neWall) {
                                if (seWall) {
                                    gc.drawImage(wallSpriteSheet, 32 * 1, 32 * 1, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                            TILE_SIZE, TILE_SIZE);
                                    gc.drawImage(wallSpriteSheet, 32 * 3 + 16, 32 * 1, 32 / 2.0, 32,
                                            i * TILE_SIZE + TILE_SIZE / 2.0, j * TILE_SIZE, TILE_SIZE - TILE_SIZE / 2.0,
                                            TILE_SIZE);
                                } else {
                                    gc.drawImage(wallSpriteSheet, 32 * 1, 32 * 1, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                            TILE_SIZE, TILE_SIZE);
                                    gc.drawImage(wallSpriteSheet, 32 * 7, 32 * 1 + 16, 32 / 2.0, 32 / 2.0,
                                            i * TILE_SIZE + TILE_SIZE / 2.0, j * TILE_SIZE, TILE_SIZE - TILE_SIZE / 2.0,
                                            TILE_SIZE - TILE_SIZE / 2.0);
                                }
                            } else if (nwWall) {
                                if (swWall) {
                                    gc.drawImage(wallSpriteSheet, 32 * 1, 32 * 1, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                            TILE_SIZE, TILE_SIZE);
                                    gc.drawImage(wallSpriteSheet, 32 * 5, 32 * 1, 32 / 2.0, 32, i * TILE_SIZE,
                                            j * TILE_SIZE, TILE_SIZE - TILE_SIZE / 2.0, TILE_SIZE);
                                } else {
                                    gc.drawImage(wallSpriteSheet, 32 * 1, 32 * 1, 32, 32, i * TILE_SIZE, j * TILE_SIZE,
                                            TILE_SIZE, TILE_SIZE);
                                    gc.drawImage(wallSpriteSheet, 32 * 7 + 16, 32 * 1 + 16, 32 / 2.0, 32 / 2.0,
                                            i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE - TILE_SIZE / 2.0,
                                            TILE_SIZE - TILE_SIZE / 2.0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawTileFromTileset(
            GraphicsContext gc,
            Image tileset,
            int tileX,
            int tileY,
            double destX,
            double destY) {
        gc.drawImage(
                tileset,
                tileX * 32,
                tileY * 32,
                32,
                32,
                destX,
                destY,
                Constants.TILE_SIZE,
                Constants.TILE_SIZE);
    }

    private void drawRectangle(GraphicsContext gc, int x, int y, int width, int height) {
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                TilePos tileTilePos;

                if (width == 1 && height == 1) {
                    // Single tile
                    tileTilePos = new TilePos(7, 1);
                } else if (width == 1) {
                    // Single column
                    if (i == y) {
                        // Top
                        tileTilePos = new TilePos(6, 0);
                    } else if (i == y + height - 1) {
                        // Bottom
                        tileTilePos = new TilePos(6, 2);
                    } else {
                        // Middle
                        tileTilePos = new TilePos(6, 1);
                    }
                } else if (height == 1) {
                    // Single row
                    if (j == x) {
                        // Left
                        tileTilePos = new TilePos(7, 0);
                    } else if (j == x + width - 1) {
                        // Right
                        tileTilePos = new TilePos(9, 0);
                    } else {
                        // Middle
                        tileTilePos = new TilePos(8, 0);
                    }
                } else {
                    // Standard 2×2+ rectangles
                    if (j == x && i == y) {
                        // Top-left corner
                        tileTilePos = new TilePos(0, 0);
                    } else if (j == x + width - 1 && i == y) {
                        // Top-right corner
                        tileTilePos = new TilePos(2, 0);
                    } else if (j == x && i == y + height - 1) {
                        // Bottom-left corner
                        tileTilePos = new TilePos(0, 2);
                    } else if (j == x + width - 1 && i == y + height - 1) {
                        // Bottom-right corner
                        tileTilePos = new TilePos(2, 2);
                    } else if (i == y) {
                        // Top edge
                        tileTilePos = new TilePos(1, 0);
                    } else if (i == y + height - 1) {
                        // Bottom edge
                        tileTilePos = new TilePos(1, 2);
                    } else if (j == x) {
                        // Left edge
                        tileTilePos = new TilePos(0, 1);
                    } else if (j == x + width - 1) {
                        // Right edge
                        tileTilePos = new TilePos(2, 1);
                    } else {
                        // Center
                        tileTilePos = new TilePos(1, 1);
                    }
                }

                drawTileFromTileset(gc, wallSpriteSheet, tileTilePos.x, tileTilePos.y, j * TILE_SIZE, i * TILE_SIZE);
            }
        }
    }

    private Button createTiledButton(String text, int tilesWide, int tilesTall) {
        double width = tilesWide * TILE_SIZE;
        double height = tilesTall * TILE_SIZE;

        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setGraphic(createTiledImageView(width, height));
        button.setContentDisplay(ContentDisplay.CENTER);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px;");

        return button;
    }

    private ImageView createTiledImageView(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, width, height);

        int tilesWide = (int) Math.ceil(width / TILE_SIZE);
        int tilesTall = (int) Math.ceil(height / TILE_SIZE);

        drawRectangle(gc, 0, 0, tilesWide, tilesTall);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setViewport(new javafx.geometry.Rectangle2D(0, 0, width, height));
        WritableImage snapshot = canvas.snapshot(params, null);

        ImageView imageView = new ImageView(snapshot);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        return imageView;
    }
}
