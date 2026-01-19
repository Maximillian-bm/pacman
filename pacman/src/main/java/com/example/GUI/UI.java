package com.example.GUI;

import static com.example.model.Constants.TARGET_FPS;
import static com.example.model.Constants.TILE_SIZE;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.KeyHandler;
import com.example.GameLogic.ClientGameController;
import com.example.model.*;

import java.util.Collections;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.text.Font;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.stream.Collectors;

public class UI extends Application {

    private final SoundEngine soundEngine = new SoundEngine();

    private final ConnectToLobby lobbyHandler = new ConnectToLobby();

    private final ClientGameController gameController = new ClientGameController();
    private GameState gameState;
    private GameState savedState;

    private GraphicsContext gc;
    private Canvas canvas;


    private KeyHandler keyHandler;

    private Runnable createLobby;

    private Text notificationText;

    private boolean eatingDot = false;

    private long animationTimeNanos = 0;

    record TilePos(int x, int y) { }

    public static final String FONT_FAMILY = "Pixelated Elegance Regular";

    @Override
    public void stop() {
        System.exit(0);
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.setTitle("Pacman");
            System.out.println("REMOTE_PUBLIC_URI: " + Constants.REMOTE_PUBLIC_URI + ", LOCAL_GATE: " + Constants.LOCAL_GATE);
            initializeMainMenu(stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initializeMainMenu(Stage stage) {
        Canvas backgroundCanvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        GraphicsContext bgGc = backgroundCanvas.getGraphicsContext2D();
        bgGc.setFill(Color.rgb(10, 10, 50));
        bgGc.fillRect(0, 0, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        drawRectangle(bgGc, 0, 0, Constants.TILES_WIDE, Constants.TILES_TALL);

        Button joinLobbyButton = createTiledButton("Join Lobby", 10, 3);

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
                playerCountChoices
        );
        createLobbyH.setAlignment(Pos.CENTER);

        Button createLobbyButton = createTiledButton("Create Lobby", 10, 3);
        VBox createLobbyV = new VBox(
                createLobbyH,
                createLobbyButton
        );
        createLobbyV.setAlignment(Pos.CENTER);

        Button startButton = createTiledButton("Start Game", 10, 3);

        Text LobbyIDText = new Text("Lobby ID: ");
        LobbyIDText.setFill(Color.WHITE);
        LobbyIDText.setStyle("-fx-font-size: 14px;");
        TextField lobbyIDInput = new TextField();
        lobbyIDInput.setMaxWidth(6 * TILE_SIZE);
        HBox joinLobbyH = new HBox(
                LobbyIDText,
                lobbyIDInput
        );
        joinLobbyH.setAlignment(Pos.CENTER);

        VBox joinLobbyV = new VBox(
                joinLobbyH,
                joinLobbyButton,
                errorText
        );
        joinLobbyV.setAlignment(Pos.CENTER);

        Text header = new Text("Pacman");
        header.setFill(Color.WHITE);
        header.setStyle("-fx-font-size: 96px;");

        // Volume slider
        Text volumeLabel = new Text("Volume:");
        volumeLabel.setFill(Color.WHITE);
        volumeLabel.setStyle("-fx-font-size: 14px;");

        Slider volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(150);
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);

        Text volumeValueText = new Text("50%");
        volumeValueText.setFill(Color.WHITE);
        volumeValueText.setStyle("-fx-font-size: 14px;");

        // Set initial volume to 50%
        soundEngine.setVolume(0.5);

        // Update volume when slider changes
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue() / 100.0;
            soundEngine.setVolume(volume);
            volumeValueText.setText(String.format("%.0f%%", newValue.doubleValue()));
        });

        HBox volumeBox = new HBox(10, volumeLabel, volumeSlider, volumeValueText);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);
        volumeBox.setPadding(new javafx.geometry.Insets(25, 25, 0, 0));
        volumeBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox startRoot = new VBox(
                header,
                joinLobbyV,
                createLobbyV,
                notificationText
        );
        startRoot.setAlignment(Pos.CENTER);
        startRoot.setSpacing(48);

        Text statusText = new Text("");
        statusText.setFill(Color.YELLOW);
        statusText.setStyle("-fx-font-size: 14px;");
        statusText.setVisible(false);
        startRoot.getChildren().add(statusText);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundCanvas, startRoot, volumeBox);
        StackPane.setAlignment(volumeBox, Pos.TOP_RIGHT);

        Scene startScene = new Scene(
                root,
                Constants.INIT_SCREEN_WIDTH,
                Constants.INIT_SCREEN_HEIGHT
        );
        startScene.getStylesheets().add("style.css");

        joinLobbyButton.setOnAction(e -> {
            soundEngine.play(Sound.EAT_FRUIT);
            String input = lobbyIDInput.getText();
            System.out.println("Connecting to: " + input);
            errorText.setText("");
            joinLobbyButton.setDisable(true);

            statusText.setText("Joining lobby...");
            statusText.setVisible(true);

            Thread joinThread = new Thread(() -> {
                try {
                    lobbyHandler.joinLobby(input);
                    javafx.application.Platform.runLater(() -> {
                        startRoot.getChildren().remove(joinLobbyV);
                        startRoot.getChildren().remove(createLobbyV);
                        statusText.setVisible(false);
                        joinedLobbyText.setText("Joined lobby with ID: " + input);
                        startRoot.getChildren().add(startButton);
                        startRoot.getChildren().add(joinedLobbyText);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorText.setText(ex.getMessage());
                        joinLobbyButton.setDisable(false);
                        statusText.setVisible(false);
                        System.err.println("--- Join Failed ---");
                        System.err.println(ex.getMessage());
                    });
                }
            });
            joinThread.setDaemon(true);
            joinThread.start();
        });

        createLobby = () -> {
            if (playerCountChoices.getValue() == null) return;

            String playerCount = playerCountChoices.getValue().toString();

            System.out.println("Creating lobby with " + playerCount + " number of player");

            createLobbyButton.setDisable(true);

            statusText.setText("Creating lobby...");
            statusText.setVisible(true);

            Thread createThread = new Thread(() -> {
                try {
                    lobbyHandler.createLobby(Integer.parseInt(playerCount));

                    javafx.application.Platform.runLater(() -> {
                        startRoot.getChildren().remove(joinLobbyV);
                        startRoot.getChildren().remove(createLobbyV);
                        statusText.setVisible(false);

                        joinedLobbyText.setText("Joined lobby with ID: " + lobbyHandler.getLobbyID());
                        startRoot.getChildren().add(startButton);
                        startRoot.getChildren().add(joinedLobbyText);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorText.setText(ex.getMessage());
                        createLobbyButton.setDisable(false);
                        statusText.setVisible(false);
                        System.err.println("--- Create Failed ---");
                        System.err.println(ex.getMessage());
                    });
                }
            });
            createThread.setDaemon(true);
            createThread.start();
        };

        createLobbyButton.setOnAction(e -> {
            soundEngine.play(Sound.EAT_FRUIT);
            createLobby.run();
        });

        startButton.setOnAction(e -> {
            startButton.setDisable(true);

            statusText.setText("Waiting for server to register all players");
            statusText.setVisible(true);

            Thread startThread = new Thread(() -> {
                try {
                    lobbyHandler.startGame();
                    javafx.application.Platform.runLater(() -> {
                        statusText.setVisible(false);
                        startGame(stage);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorText.setText("Failed to start game: " + ex.getMessage());
                        startButton.setDisable(false);
                        statusText.setVisible(false);
                        System.err.println("--- Start Game Failed ---");
                        System.err.println(ex.getMessage());
                    });
                }
                soundEngine.play(Sound.START_MUSIC);
            });
            startThread.setDaemon(true);
            startThread.start();
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
        savedState = gameController.deepCopyGameState(gameState);

        final StackPane root = new StackPane();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        Constants.cleanActions.setPlayerID(lobbyHandler.getPlayerID());
        keyHandler = new KeyHandler(lobbyHandler.getLobbyID(), lobbyHandler.getPlayerID());

        scene.setOnKeyPressed(e -> keyHandler.move(e.getCode()));

        stage.setScene(scene);

        Button quitButton = createTiledButton("QUIT", 6, 2);
        quitButton.setPrefSize(200, 100);
     
        StackPane.setAlignment(quitButton, Pos.CENTER);
        quitButton.translateYProperty().bind(scene.heightProperty().multiply(0.15));
        
        quitButton.setVisible(false);

        final GameAnimator gameAnimator = new GameAnimator(quitButton);
        gameAnimator.start();

        stage.setOnCloseRequest(event -> {
            lobbyHandler.quit();
            gameAnimator.stop();
            System.exit(0);
        });

        quitButton.setOnAction(e -> {
            lobbyHandler.quit();
            gameAnimator.stop();
            stage.close();
            System.exit(0);
        });

        canvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        root.getChildren().add(canvas);
        root.getChildren().add(quitButton);

        gc = canvas.getGraphicsContext2D();

        stage.show();
    }

    private class GameAnimator extends AnimationTimer {
        private Button restartButton;
        private long startTime;

        public GameAnimator(Button button) {
            restartButton = button;
            startTime = 0;
        }

        @Override
        public void handle(long time) {
            if (startTime == 0) {
                startTime = time;
            }

            animationTimeNanos = time - startTime;

            draw();

            long tickDurationNanos = 1_000_000_000 / TARGET_FPS;
            long elapsedSinceStart = time - (startTime + Constants.timeOffset);
            long expectedTicksElapsed = Constants.clock + Constants.COUNTDOWN_DURATION_TICKS;
            if (elapsedSinceStart < expectedTicksElapsed * tickDurationNanos) {
                return; // Not time for next game tick yet
            }

            if (Constants.clock < 0) {
                Constants.clock++;
                return;
            }

            if (Constants.cleanActions.missedAction(Constants.clock)) {
                gameState = gameController.updateGameStateFor(savedState, Constants.clock);
            } else {
                List<Action> ActionOfClock = Constants.cleanActions.getActions(Constants.clock);
                if (!ActionOfClock.isEmpty())
                    savedState = gameController.deepCopyGameState(gameState);
                gameState = gameController.updateGameState(gameState, ActionOfClock);
            }

            playSounds();
            Constants.clock++;
        }

        public void resetTime() {
            startTime = 0;
        }

        private void draw() {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            drawMap();

            drawPlayers();

            drawGhosts();

            if (gameState.winner() == null) {
                if (Constants.clock < 0) drawCountdown();
                drawPoints();
                restartButton.setVisible(false);
            } else {
                drawEndscreen();
                restartButton.setVisible(true);
            }
        }

        private void drawEndscreen() {
            gc.setFill(Color.color(0.2, 0.2, 0.2, 0.9));
            int padding = 200;
            gc.fillRect(
                Constants.INIT_SCREEN_WIDTH/2-padding,
                Constants.INIT_SCREEN_HEIGHT/2-padding,
                padding*2,
                padding*2
            );
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(4);
            gc.strokeRect(
                Constants.INIT_SCREEN_WIDTH/2-padding,
                Constants.INIT_SCREEN_HEIGHT/2-padding,
                padding*2,
                padding*2
            );
            gc.setFont(new Font(FONT_FAMILY, 32));
            gc.setFill(Color.YELLOW);
            gc.fillText("GAME OVER",
                Constants.INIT_SCREEN_WIDTH/2-padding+50,
                Constants.INIT_SCREEN_HEIGHT/2-padding+50
            );
            List<Player> players = gameState.players()
                    .stream()
                    .sorted(Comparator.comparing((Player p) -> p.getPoints()))
                        .collect(Collectors.toList());
            Collections.reverse(players);
            gc.setFont(new Font(FONT_FAMILY, 20));
            for (int i = 0; i < players.size(); i++) {
                gc.setFill(players.get(i).getColor());
                gc.fillText("Score: " + players.get(i).getPoints(),
                    Constants.INIT_SCREEN_WIDTH/2-padding+40,
                    Constants.INIT_SCREEN_HEIGHT/2-padding+100+i*50
                );
            }
        }

        private void playSounds(){
            Player localPlayer = gameState.players().get(lobbyHandler.getPlayerID());
            if(localPlayer.isLostHeart()){
                soundEngine.play(Sound.FAIL);
                localPlayer.setLostHeart(false);
            }
            if(localPlayer.isAteFruit()){
                soundEngine.play(Sound.EAT_FRUIT);
                localPlayer.setAteFruit(false);
            }
            if(localPlayer.isAteGhost()){
                soundEngine.play(Sound.EAT_GHOST);
                soundEngine.play(Sound.GHOST_HOME);
                localPlayer.setAteGhost(false);
            }
            for (Player p : gameState.players()) {
                if(p.isAtePowerUp()){
                    soundEngine.play(Sound.GHOST_BLUE);
                    p.setAtePowerUp(false);
                }
            }

            Pair<Integer, Integer> pos = localPlayer.getPosition().ToGridPosition();
            int xOffset = 0;
            int yOffset = 0;
            switch (localPlayer.getDirection()) {
                case Direction.NORTH:
                    yOffset = -1;
                    break;
                case Direction.SOUTH:
                    yOffset = 1;
                    break;
                case Direction.EAST:
                    xOffset = 1;
                    break;
                case Direction.WEST:
                    xOffset = -1;
                    break;
                default:
                    break;
            }
            if (!eatingDot &&
                gameState.tiles()[Math.floorMod(pos.getValue() + yOffset, Constants.TILES_TALL)][Math.floorMod(pos.getKey() + xOffset, Constants.TILES_WIDE)] == TileType.PAC_DOT) {
                eatingDot = true;
                soundEngine.play(Sound.EAT_DOT);
            } else if (eatingDot &&
                gameState.tiles()[Math.floorMod(pos.getValue() + yOffset, Constants.TILES_TALL)][Math.floorMod(pos.getKey() + xOffset, Constants.TILES_WIDE)] != TileType.PAC_DOT) {
                eatingDot = false;
                soundEngine.stop(Sound.EAT_DOT);
            }
        }

        private void drawCountdown() {
            Color playerColor = gameState.players().get(lobbyHandler.getPlayerID()).getColor();
            float seconds = -1 * (float) (Constants.clock) / Constants.TARGET_FPS;

            gc.setFill(playerColor);
            gc.setFont(new Font(FONT_FAMILY, 80));
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
                gc.setFont(new Font(FONT_FAMILY, 20));

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
            for (int y = 0; y < tiles.length; y++) {
                for (int x = 0; x < tiles[0].length; x++) {
                    switch (tiles[y][x]) {
                        case EMPTY:
                            // gc.setFill(Color.BLACK);
                            // gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            break;
                        case WALL:
                            drawWall(y, x);
                            break;
                        case PAC_DOT:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 8, 8, x, y);
                            break;
                        case ENERGIZER:
                            if (gameState.entityTracker().isAnyPowerActive())
                                gc.setGlobalAlpha(0.33);
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 8, 9, x, y, 1.5);
                            gc.setGlobalAlpha(1.0);
                            break;
                        case CHERRY:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 0, x, y, 1.5);
                            break;
                        case STRAWBERRY:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 1, x, y, 1.5);
                            break;
                        case ORANGE:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 2, x, y, 1.5);
                            break;
                        case APPLE:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 3, x, y, 1.5);
                            break;
                        case MELON:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 4, x, y, 1.5);
                            break;
                        case GALAXIAN:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 7, x, y, 1.5);
                            break;
                        case BELL:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 8, x, y, 1.5);
                            break;
                        case KEY:
                            drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, 12, 9, x, y, 1.5);
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

        private void drawPlayers() {
            EntityTracker entityTracker = gameState.entityTracker();
            int pacmanFrameCount = 4;
            long pacmanNanosPerFrame = 75_000_000L;
            long powerupBlinkNanosPerUnit = 300_000_000L;
            long invulnBlinkNanosPerUnit = 500_000_000L;

            gameState.players().forEach(player -> {
                if(!player.isDeadWithNoHearts()){
                    int tileY = switch (player.getDirection()) {
                        case WEST -> 6;
                        case NORTH -> 9;
                        case SOUTH -> 3;
                        default -> 0;
                    };

                    int pacmanFrame = (int) ((animationTimeNanos / pacmanNanosPerFrame) % pacmanFrameCount);
                    tileY = switch (pacmanFrame) {
                        case 0 -> tileY;
                        case 2 -> tileY + 2;
                        default -> tileY + 1;
                    };

                SpriteSheet playerSheet = SpriteSheet.getPlayerSheet(player.getColor());

                boolean hasPowerUp = entityTracker.isPowerOwner(player);
                if (player.isInvulnerable() || hasPowerUp) {
                    int blinkFrame = 0;
                    if (hasPowerUp) {
                        blinkFrame = getBlinkFrame(powerupBlinkNanosPerUnit, player.getPowerUpTimer() / Constants.FRIGHTENED_DURATION_SEC, 0.65, Constants.FRIGHTENED_DURATION_SEC);
                    } else {
                        blinkFrame = getBlinkFrame(invulnBlinkNanosPerUnit, player.getInvulnerableTimer() / Constants.PLAYER_SPAWN_PROTECT_SEC, 0.5, Constants.PLAYER_SPAWN_PROTECT_SEC);
                    }
                    if (blinkFrame == 1) {
                        playerSheet = SpriteSheet.getPlayerSheet(Color.WHITE);
                    }
                }

                Position playerTilePos = player.getPosition();

                    double rsTimer = player.getRespawnTimer();
                    if (rsTimer <= 0) {
                        drawSpriteFromSheet(gc, playerSheet, 17, tileY, playerTilePos.x, playerTilePos.y, 1.75);
                    } else {
                        double rsFrameInterval = Constants.PLAYER_RESPAWN_DELAY_SEC/11;
                        int respawnTileY = 0;
                        for (int i = 1; i < 11; i++) {
                            if (rsTimer < rsFrameInterval*i) {
                                respawnTileY = 11-i;
                                break;
                            }
                        }

                        drawSpriteFromSheet(gc, playerSheet, 7, respawnTileY, playerTilePos.x, playerTilePos.y, 1.75);
                    }
                }
            });
        }

        private int getBlinkFrame(long nanosPerUnit, double remainingRatio, double endDelayRatio, double effectDurationSec) {
            remainingRatio = Math.max(0.0, Math.min(1.0, remainingRatio));
            long effectElapsedNanos = (long) ((1.0 - remainingRatio) * effectDurationSec * 1_000_000_000L);
            double t = 1.0 - remainingRatio;
            double adjustedRemainingRatio = 1.0 - t * t * t;
            double halfPeriodUnits = endDelayRatio + (1.0 - endDelayRatio) * adjustedRemainingRatio;
            long halfPeriodNanos = Math.max(1_000_000L, (long) (halfPeriodUnits * nanosPerUnit));
            return ((effectElapsedNanos / halfPeriodNanos) % 2 == 0) ? 0 : 1;
        }

        private void drawGhosts() {
            int ghostFrameCount = 2;
            long ghostNanosPerFrame = 300_000_000L;

            gameState.ghosts().forEach(ghost -> {
                int tileY = 0, tileX = 0;
                if (ghost.getRespawnTimer() <= 0) {
                    tileX = switch (ghost.getType()) {
                        case RED -> 0; // ("Blinky"),
                        case PINK -> 1; // ("Pinky"),
                        case CYAN -> 2; // ("Inky"),
                        case ORANGE -> 3; // ("Clyde"),
                        case PURPLE -> 5; // ("Sue");
                    };
                    tileY = switch (ghost.getDirection()) {
                        case WEST -> 4;
                        case NORTH -> 6;
                        case SOUTH -> 2;
                        default -> tileY;
                    };
                    double fTimer = gameState.entityTracker().getFrightenedTimerSec();

                    if (fTimer > 0) {
                        tileY += 11;
                        tileX = 0;
                    }

                    int ghostFrame = (int) ((animationTimeNanos / ghostNanosPerFrame) % ghostFrameCount);
                    if (ghostFrame == 1) {
                        tileY += 1;
                        if (fTimer > 0 && fTimer < 2.0)
                            tileX += 1;
                    }
                } else {
                    tileX = 6;
                    tileY = switch (ghost.getDirection()) {
                        case WEST -> 7;
                        case NORTH -> 8;
                        case SOUTH -> 6;
                        default -> 5;
                    };
                }

                Position ghostTilePos = ghost.getPosition();
                drawSpriteFromSheet(gc, SpriteSheet.OBJECT_SHEET, tileX, tileY, ghostTilePos.x, ghostTilePos.y, 1.75);
            });
        }

        private boolean isWall(TileType[][] tiles, int y, int x) {
            return y >= 0 && y < tiles.length && x >= 0 && x < tiles[0].length
                    && tiles[y][x] == TileType.WALL;
        }

        private void drawWall(int y, int x) {
            TileType[][] tiles = gameState.tiles();

            boolean n = isWall(tiles, y - 1, x);
            boolean s = isWall(tiles, y + 1, x);
            boolean e = isWall(tiles, y, x + 1);
            boolean w = isWall(tiles, y, x - 1);

            boolean ne = isWall(tiles, y - 1, x + 1);
            boolean nw = isWall(tiles, y - 1, x - 1);
            boolean se = isWall(tiles, y + 1, x + 1);
            boolean sw = isWall(tiles, y + 1, x - 1);

            // Encode cardinal directions as bitmask: N=1, E=2, S=4, W=8
            int mask = (n ? 1 : 0) | (e ? 2 : 0) | (s ? 4 : 0) | (w ? 8 : 0);

            switch (mask) {
                case 0 -> // isolated
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 8, 2, x, y);
                case 1 -> // N
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 3, x, y); // Vertical
                case 2 -> // E
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 4, x, y); // Horizontal
                case 3 -> // N+E
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 4, x, y);
                case 4 -> // S
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 3, x, y); // Vertical
                case 5 -> // N+S
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 3, x, y); // Vertical
                case 6 -> // S+E
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 3, x, y);
                case 7 -> { // N+S+E
                    if (!ne) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 4, x, y);
                    else if (!se) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 3, x, y);
                    else drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 3, x, y); // Vertical
                }
                case 8 -> // W
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 4, x, y); // Horizontal
                case 9 -> // N+W
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 4, x, y);
                case 10 -> // E+W
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 4, x, y); // Horizontal
                case 11 -> { // N+E+W
                    if (!ne) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 4, x, y);
                    else if (!nw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 4 , x, y);
                    else drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 4, x, y); // Horizontal
                }
                case 12 -> // S+W
                    drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 3, x, y);
                case 13 -> { // N+S+W
                    if (!nw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 4, x, y);
                    else if (!sw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 3, x, y);
                    else drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 3, x, y); // Vertical
                }
                case 14 -> { // S+E+W
                    if (!se) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 3, x, y);
                    else if (!sw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 3, x, y);
                    else drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 2, 4, x, y); // Horizontal
                }
                case 15 -> { // N+S+E+W
                    if (!se) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 3, x, y);
                    else if (!sw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 3, x, y);
                    else if (!ne) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 0, 4, x, y);
                    else if (!nw) drawSpriteFromSheet(gc, SpriteSheet.WALL_COLORED, 1, 4, x, y);
                }
            }
        }
    }

    private void drawSpriteFromSheet(
        GraphicsContext gc,
        SpriteSheet spriteSheet,
        int srcTileX,
        int srcTileY,
        int destTileX,
        int destTileY
    ) {
        drawSpriteFromSheet(gc, spriteSheet, srcTileX, srcTileY, destTileX, destTileY, 1.0);
    }

    private void drawSpriteFromSheet(
        GraphicsContext gc,
        SpriteSheet spriteSheet,
        int srcTileX,
        int srcTileY,
        int destTileX,
        int destTileY,
        double scaleFactor
    ) {
        drawSpriteFromSheet(gc, spriteSheet, srcTileX, srcTileY, (double) (destTileX * TILE_SIZE), (double) (destTileY * TILE_SIZE), scaleFactor);
    }

    private void drawSpriteFromSheet(
        GraphicsContext gc,
        SpriteSheet spriteSheet,
        int srcTileX,
        int srcTileY,
        double destPixelX,
        double destPixelY
    ) {
        drawSpriteFromSheet(gc, spriteSheet, srcTileX, srcTileY, destPixelX, destPixelY, 1.0);
    }

    private void drawSpriteFromSheet(
        GraphicsContext gc,
        SpriteSheet spriteSheet,
        int srcTileX,
        int srcTileY,
        double destPixelX,
        double destPixelY,
        double scaleFactor
    ) {
        double finalWidth = TILE_SIZE * scaleFactor;
        double finalHeight = TILE_SIZE * scaleFactor;
        double offsetX = (finalWidth - TILE_SIZE) / 2.0;
        double offsetY = (finalHeight - TILE_SIZE) / 2.0;
        int pixelsPerTile = spriteSheet.pixelsPerTile;
        gc.drawImage(
            spriteSheet.image,
            srcTileX * pixelsPerTile,
            srcTileY * pixelsPerTile,
            pixelsPerTile,
            pixelsPerTile,
            destPixelX - offsetX,
            destPixelY - offsetY,
            finalWidth,
            finalHeight
        );
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

                drawSpriteFromSheet(gc, SpriteSheet.WALL_SHEET, tileTilePos.x, tileTilePos.y, j, i);
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
