package com.example.GUI;

import static com.example.model.Constants.TARGET_FPS;
import static com.example.model.Constants.TILES_TALL;
import static com.example.model.Constants.TILES_WIDE;
import static com.example.model.Constants.TILE_SIZE;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.KeyHandler;
import com.example.GameLogic.ClientGameController;
import com.example.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.stream.Collectors;

public class UI extends Application {

    // ===== Tick-based animation constants =====
    // At TARGET_FPS=20, each tick = 50ms. Conversion: ticksPerFrame = round((nanos / 1e9) * TARGET_FPS)

    // Pacman animation: original 75_000_000 ns/frame → (75ms * 20 FPS / 1000) ≈ 1.5, rounded to 2 ticks
    private static final int PACMAN_FRAME_COUNT = 4;
    private static final int PACMAN_TICKS_PER_FRAME = 2;

    // Ghost animation: original 300_000_000 ns/frame → (300ms * 20 FPS / 1000) = 6 ticks
    private static final int GHOST_FRAME_COUNT = 2;
    private static final int GHOST_TICKS_PER_FRAME = 6;

    // Blink animation base rates (ticks per "unit" for variable-rate blinking)
    // Power-up blink: original 200_000_000 ns/unit → (200ms * 20 FPS / 1000) = 4 ticks
    private static final int POWERUP_BLINK_TICKS_PER_UNIT = 4;
    // Invulnerability blink: original 500_000_000 ns/unit → (500ms * 20 FPS / 1000) = 10 ticks
    private static final int INVULN_BLINK_TICKS_PER_UNIT = 10;

    private final SoundEngine soundEngine = new SoundEngine();

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
    private final Map<Color, Image> coloredPlayerCache = new HashMap<>();

    private KeyHandler keyHandler;

    private Runnable createLobby;

    private Text notificationText;

    private boolean eatingDot = false;

    record TilePos(int x, int y) {
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pacman");
        System.out.println("REMOTE_PUBLIC_URI: " + Constants.REMOTE_PUBLIC_URI + ", LOCAL_GATE: " + Constants.LOCAL_GATE);
        precomputePlayerColors();
        initializeMainMenu(stage);
    }

    private void precomputePlayerColors() {
        // Pre-compute colored player images for all 4 player colors
        Color[] playerColors = {
            Color.rgb(255, 241, 0),  // Player 0 - Yellow
            Color.rgb(255, 0, 0),    // Player 1 - Red
            Color.rgb(0, 255, 0),    // Player 2 - Green
            Color.rgb(0, 0, 255)     // Player 3 - Blue
        };
        for (Color color : playerColors) {
            coloredPlayerCache.put(color, createColoredPlayerImage(color));
        }
    }

    // Modified function from:
    // https://stackoverflow.com/questions/18124364/how-to-change-color-of-image-in-javafx
    private Image createColoredPlayerImage(Color color) {
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

    private void initializeMainMenu(Stage stage) {
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
            soundEngine.play(Sound.EAT_FRUIT);
            String input = lobbyIDInput.getText();
            System.out.println("Connecting to: " + input);
            errorText.setText("");
            joinLobbyButton.setDisable(true);

            Thread joinThread = new Thread(() -> {
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
            });
            joinThread.setDaemon(true);
            joinThread.start();
        });

        createLobby = () -> {
            if (playerCountChoices.getValue() == null) {
                return;
            }
            String playerCount = playerCountChoices.getValue().toString();

            System.out.println("Creating lobby with " + playerCount + " number of player");

            createLobbyButton.setDisable(true);

            Thread createThread = new Thread(() -> {
                try {
                    lobbyHandler.createLobby(Integer.parseInt(playerCount));

                    javafx.application.Platform.runLater(() -> {
                        startRoot.getChildren().remove(joinLobbyV);
                        startRoot.getChildren().remove(createLobbyV);

                        joinedLobbyText.setText("Joined lobby with ID: " + lobbyHandler.getLobbyID());
                        startRoot.getChildren().add(startButton);
                        startRoot.getChildren().add(joinedLobbyText);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorText.setText(ex.getMessage());
                        createLobbyButton.setDisable(false);
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
            startLobby(stage);
            soundEngine.play(Sound.START_MUSIC);
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

        // final Group root = new Group();
        final StackPane root = new StackPane();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);

        Constants.cleanActions.setPlayerID(lobbyHandler.getPlayerID());
        keyHandler = new KeyHandler(lobbyHandler.getLobbyID(), lobbyHandler.getPlayerID());

        scene.setOnKeyPressed(e -> keyHandler.move(e.getCode()));

        stage.setScene(scene);

        Button quitButton = new Button("QUIT");
        quitButton.setPrefSize(200, 100);
        quitButton.setTranslateX(Constants.INIT_SCREEN_WIDTH/100);
        quitButton.setTranslateY(Constants.INIT_SCREEN_HEIGHT/50);
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
            stage.close();
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

            // === Rendering (runs at full AnimationTimer rate, typically 60 FPS) ===
            draw();
            if (Constants.clock < 0) {
                drawCountdown();
            }

            // === Game logic tick check (runs at TARGET_FPS = 20 FPS) ===
            long tickDurationNanos = 1_000_000_000 / TARGET_FPS;
            long elapsedSinceStart = time - (startTime + Constants.timeOffset);
            long expectedTicksElapsed = Constants.clock + Constants.COUNTDOWN_DURATION_TICKS;
            if (elapsedSinceStart < expectedTicksElapsed * tickDurationNanos) {
                return; // Not time for next game tick yet
            }

            // === Game logic update (20 FPS) ===
            if (Constants.clock < 0) {
                // Countdown tick - just increment
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
                drawPoints();
                restartButton.setVisible(false);
            } else {
                drawEndscreen();
                restartButton.setVisible(true);
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
                    Constants.INIT_SCREEN_WIDTH/2-padding+100, 
                    Constants.INIT_SCREEN_HEIGHT/2-padding+(i+2)*40
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
            if(!eatingDot && gameState.tiles()[Math.floorMod(pos.getValue()+yOffset, Constants.TILES_TALL)][Math.floorMod(pos.getKey()+xOffset, Constants.TILES_WIDE)] == TileType.PAC_DOT){
                eatingDot = true;
                soundEngine.play(Sound.EAT_DOT);
            }else if(eatingDot && gameState.tiles()[Math.floorMod(pos.getValue()+yOffset, Constants.TILES_TALL)][Math.floorMod(pos.getKey()+xOffset, Constants.TILES_WIDE)] != TileType.PAC_DOT){
                eatingDot = false;
                soundEngine.stop(Sound.EAT_DOT);
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
                            double pacDotSize = TILE_SIZE / 8.0;
                            gc.setFill(Color.YELLOW);
                            gc.fillRect(x * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0,
                                    y * TILE_SIZE + TILE_SIZE / 2.0 - pacDotSize / 2.0, pacDotSize, pacDotSize);
                            break;
                        case CHERRY:
                            gc.drawImage(spriteSheet, 600, 0, 50, 50, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case STRAWBERRY:
                            gc.drawImage(spriteSheet, 600, 50, 50, 50, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case ORANGE:
                            gc.drawImage(spriteSheet, 600, 100, 50, 50, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case APPLE:
                            gc.drawImage(spriteSheet, 600, 150, 50, 50, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case MELON:
                            gc.drawImage(spriteSheet, 600, 200, 50, 50, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE);
                            break;
                        case ENERGIZER:
                            gc.drawImage(spriteSheet, 415, 415, 25, 25, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
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

        private void drawPlayers() {
            EntityTracker entityTracker = gameState.entityTracker();
            // Use absolute tick count (offset by countdown) for consistent animation across game states
            int ticksSinceStart = Constants.clock + Constants.COUNTDOWN_DURATION_TICKS;

            gameState.players().forEach(player -> {
                int sy = switch (player.getDirection()) {
                    case WEST -> 50 * 6;
                    case NORTH -> 50 * 9;
                    case SOUTH -> 50 * 3;
                    default -> 0;
                };

                // Tick-based pacman animation: cycles through 4 frames
                int pacmanFrame = Math.floorMod(ticksSinceStart / PACMAN_TICKS_PER_FRAME, PACMAN_FRAME_COUNT);
                sy = switch (pacmanFrame) {
                    case 0 -> sy;
                    case 2 -> sy + 50 * 2;
                    default -> sy + 50;
                };

                boolean hasPowerUp = entityTracker.isPowerOwner(player);

                if (player.isInvulnerable() || hasPowerUp) {
                    int blinkFrame;
                    if (hasPowerUp) {
                        blinkFrame = getBlinkFrame(POWERUP_BLINK_TICKS_PER_UNIT, player.getPowerUpTimer() / Constants.FRIGHTENED_DURATION_SEC, 0.8);
                    } else {
                        blinkFrame = getBlinkFrame(INVULN_BLINK_TICKS_PER_UNIT, player.getInvulnerableTimer() / Constants.PLAYER_SPAWN_PROTECT_SEC, 0.9);
                    }

                    if (blinkFrame == 1) sy = 50 * 12;
                }

                Image coloredPlayer = colorPlayer(player.getColor());
                Position playerTilePos = player.getPosition();
                gc.drawImage(coloredPlayer, 850, sy, 50, 50, playerTilePos.x, playerTilePos.y, TILE_SIZE, TILE_SIZE);
            });
        }

        /**
         * Tick-based blink frame calculation.
         * @param ticksPerUnit Base ticks per blink "unit" (converted from original nanoseconds)
         * @param remainingRatio Fraction of effect time remaining (0.0 to 1.0)
         * @param endDelayRatio Minimum blink period multiplier when time is almost out
         * @return 0 for normal sprite, 1 for blink sprite
         */
        private int getBlinkFrame(int ticksPerUnit, double remainingRatio, double endDelayRatio) {
            // Clamp remaining ratio to [0, 1]
            remainingRatio = Math.max(0.0, Math.min(1.0, remainingRatio));
            // Apply quadratic curve to make blinking accelerate as time runs out
            double t = 1.0 - remainingRatio;
            remainingRatio = 1.0 - t * t;
            // Blink period in "units" - ranges from endDelayRatio (fast) to endDelayRatio+1 (slow)
            double blinkPeriodUnits = endDelayRatio + remainingRatio;

            // Use tick count offset by countdown duration to get ticks since game start
            int ticksSinceStart = Constants.clock + Constants.COUNTDOWN_DURATION_TICKS;
            // Convert ticks to "units" for the blink calculation
            double tickUnits = (double) ticksSinceStart / ticksPerUnit;
            // Calculate position within blink cycle (full cycle = 2 * blinkPeriodUnits)
            double cycleTime = tickUnits % (blinkPeriodUnits * 2.0);
            return cycleTime < blinkPeriodUnits ? 0 : 1;
        }

        private Image colorPlayer(Color color) {
            return coloredPlayerCache.get(color);
        }

        private void drawGhosts() {
            // Use absolute tick count (offset by countdown) for consistent animation across game states
            int ticksSinceStart = Constants.clock + Constants.COUNTDOWN_DURATION_TICKS;

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

                double fTimer = gameState.entityTracker().getFrightenedTimerSec();

                if (fTimer > 0) {
                    sy += 50 * 11;
                    sx = 0;
                }

                // Tick-based ghost animation: cycles through 2 frames
                int ghostFrame = Math.floorMod(ticksSinceStart / GHOST_TICKS_PER_FRAME, GHOST_FRAME_COUNT);
                if (ghostFrame == 1) {
                    sy += 50;
                    if (fTimer > 0 && fTimer < 2.0)
                        sx += 50;
                }

                Position ghostTilePos = ghost.getPosition();
                gc.drawImage(spriteSheet, sx, sy, 50, 50, ghostTilePos.x, ghostTilePos.y, TILE_SIZE, TILE_SIZE);
            });
        }

        private boolean isWall(TileType[][] tiles, int y, int x) {
            return y >= 0 && y < tiles.length && x >= 0 && x < tiles[0].length
                    && tiles[y][x] == TileType.WALL;
        }

        private void drawWall(int y, int x) {
            TileType[][] tiles = gameState.tiles();

            // Check adjacent walls (cardinal directions)
            boolean n = isWall(tiles, y - 1, x);
            boolean s = isWall(tiles, y + 1, x);
            boolean e = isWall(tiles, y, x + 1);
            boolean w = isWall(tiles, y, x - 1);

            // Check diagonal walls
            boolean ne = isWall(tiles, y - 1, x + 1);
            boolean nw = isWall(tiles, y - 1, x - 1);
            boolean se = isWall(tiles, y + 1, x + 1);
            boolean sw = isWall(tiles, y + 1, x - 1);

            // Encode cardinal directions as bitmask: N=1, E=2, S=4, W=8
            int mask = (n ? 1 : 0) | (e ? 2 : 0) | (s ? 4 : 0) | (w ? 8 : 0);

            double destX = x * TILE_SIZE;
            double destY = y * TILE_SIZE;

            switch (mask) {
                case 0 -> // isolated
                    drawTileFromTileset(gc, wallSpriteSheet, 7, 1, destX, destY);
                case 1 -> // N only
                    drawTileFromTileset(gc, wallSpriteSheet, 6, 2, destX, destY);
                case 2 -> // E only
                    drawTileFromTileset(gc, wallSpriteSheet, 7, 0, destX, destY);
                case 3 -> // N+E
                    drawTileFromTileset(gc, wallSpriteSheet, ne ? 0 : 3, 2, destX, destY);
                case 4 -> // S only
                    drawTileFromTileset(gc, wallSpriteSheet, 6, 0, destX, destY);
                case 5 -> // N+S
                    drawTileFromTileset(gc, wallSpriteSheet, 6, 1, destX, destY);
                case 6 -> // E+S
                    drawTileFromTileset(gc, wallSpriteSheet, se ? 0 : 3, 0, destX, destY);
                case 7 -> // N+E+S
                    drawTileFromTileset(gc, wallSpriteSheet, 3, 1, destX, destY);
                case 8 -> // W only
                    drawTileFromTileset(gc, wallSpriteSheet, 9, 0, destX, destY);
                case 9 -> // N+W
                    drawTileFromTileset(gc, wallSpriteSheet, nw ? 2 : 5, 2, destX, destY);
                case 10 -> // E+W
                    drawTileFromTileset(gc, wallSpriteSheet, 8, 0, destX, destY);
                case 11 -> // N+E+W
                    drawTileFromTileset(gc, wallSpriteSheet, nw || ne ? 1 : 4, 2, destX, destY);
                case 12 -> // S+W
                    drawTileFromTileset(gc, wallSpriteSheet, sw ? 2 : 5, 0, destX, destY);
                case 13 -> // N+S+W
                    drawTileFromTileset(gc, wallSpriteSheet, 5, 1, destX, destY);
                case 14 -> // E+S+W
                    drawTileFromTileset(gc, wallSpriteSheet, sw || se ? 1 : 4, 0, destX, destY);
                case 15 -> // N+E+S+W (all cardinal directions)
                    drawCrossWall(destX, destY, ne, nw, se, sw);
            }
        }

        private void drawCrossWall(double destX, double destY, boolean ne, boolean nw, boolean se, boolean sw) {
            // Base: full cross
            drawTileFromTileset(gc, wallSpriteSheet, 1, 1, destX, destY);

            // Draw corner overlays for missing diagonal walls
            double half = TILE_SIZE / 2.0;

            if (!ne && !se) {
                // Right edge missing both corners
                drawTileFromTileset(gc, wallSpriteSheet, 3, 1, 16, 0, 16, 32, destX + half, destY);
            } else if (!ne) {
                // Top-right corner only
                drawTileFromTileset(gc, wallSpriteSheet, 7, 1, 0, 16, 16, 16, destX + half, destY);
            }

            if (!nw && !sw) {
                // Left edge missing both corners
                drawTileFromTileset(gc, wallSpriteSheet, 5, 1, 0, 0, 16, 32, destX, destY);
            } else if (!nw) {
                // Top-left corner only
                drawTileFromTileset(gc, wallSpriteSheet, 7, 1, 16, 16, 16, 16, destX, destY);
            }
        }
    }

    private void drawTileFromTileset(
            GraphicsContext gc,
            Image tileset,
            int tileX,
            int tileY,
            double destX,
            double destY
    ) {
        drawTileFromTileset(gc, tileset, tileX, tileY, 0, 0, 32, 32, destX, destY);
    }

    private void drawTileFromTileset(
            GraphicsContext gc,
            Image tileset,
            int tileX,
            int tileY,
            int srcOffsetX,
            int srcOffsetY,
            int srcWidth,
            int srcHeight,
            double destX,
            double destY) {
        double scale = Constants.TILE_SIZE / 32.0;
        gc.drawImage(
                tileset,
                tileX * 32 + srcOffsetX,
                tileY * 32 + srcOffsetY,
                srcWidth,
                srcHeight,
                destX,
                destY,
                srcWidth * scale,
                srcHeight * scale);
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
