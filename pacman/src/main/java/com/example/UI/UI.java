package com.example.UI;

import com.example.model.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.application.Application;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.example.model.Constants.TILE_SIZE;
import static com.example.model.Constants.TILE_COUNT;

public class UI extends Application {
    private Group tilesLayer;
    private Group entitiesLayer;
    private Group uiLayer;
    private AnimationTimer gameLoop;

    @Getter
    @Setter
    private GameState currentGameState;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        this.tilesLayer = new Group();
        this.entitiesLayer = new Group();
        this.uiLayer = new Group();

        root.getChildren().addAll(tilesLayer, entitiesLayer, uiLayer);

        Scene scene = new Scene(root, TILE_SIZE * TILE_COUNT, TILE_SIZE * TILE_COUNT);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("Pacman");
        stage.show();

        List<Player> players = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        TileType[][] tiles = new TileType[TILE_COUNT][TILE_COUNT];

        // Create test player
        Player testPlayer = new Player(1);
        testPlayer.setPosition(new Position(
            5 * TILE_SIZE + TILE_SIZE / 2.0,
            1 * TILE_SIZE + TILE_SIZE / 2.0
        ));
        players.add(testPlayer);

        // Create test ghosts
        Ghost ghost1 = new Ghost();
        ghost1.type = GhostType.RED;
        ghost1.position = new Position(
            3 * TILE_SIZE + TILE_SIZE / 2.0,
            1 * TILE_SIZE + TILE_SIZE / 2.0
        );
        ghosts.add(ghost1);

        Ghost ghost2 = new Ghost();
        ghost2.type = GhostType.PINK;
        ghost1.position = new Position(
            2 * TILE_SIZE + TILE_SIZE / 2.0,
            1 * TILE_SIZE + TILE_SIZE / 2.0
        );
        ghosts.add(ghost2);

        // Initialize tiles with a simple test level
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                // Border walls
                if (row == 0 || row == tiles.length - 1 || col == 0 || col == tiles[row].length - 1) {
                    tiles[row][col] = TileType.WALL;
                }
                // Some interior walls
                else if ((row == 3 || row == 7 || row == 11 || row == 15) && col % 3 == 0) {
                    tiles[row][col] = TileType.WALL;
                }
                // Energizers in corners
                else if ((row == 2 && col == 2) || (row == 2 && col == 17) ||
                         (row == 17 && col == 2) || (row == 17 && col == 17)) {
                    tiles[row][col] = TileType.ENERGIZER;
                }
                // Cherry in center
                else if (row == 10 && col == 10) {
                    tiles[row][col] = TileType.CHERRY;
                }
                // Pac-dots everywhere else
                else {
                    tiles[row][col] = TileType.PAC_DOT;
                }
            }
        }

        currentGameState = new GameState(
            0,
            players,
            ghosts,
            tiles,
            null
        );

        // Start game loop
        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (currentGameState != null) {
                    drawGameState(currentGameState);
                }
            }
        };
        gameLoop.start();
    }

    @Override
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void drawGameState(GameState gameState) {
        tilesLayer.getChildren().clear();
        entitiesLayer.getChildren().clear();
        uiLayer.getChildren().clear();

        drawTiles(gameState.tiles());
        gameState.ghosts().forEach(this::drawGhost);
        gameState.players().forEach(this::drawPlayer);
        drawUI(gameState);
    }

    private void drawTiles(TileType[][] tiles) {
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                TileType tile = tiles[row][col];
                if (tile == null) continue;

                double x = col * TILE_SIZE;
                double y = row * TILE_SIZE;

                switch (tile) {
                    case WALL -> {
                        Rectangle wall = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
                        wall.setFill(Color.BLUE);
                        wall.setStroke(Color.DARKBLUE);
                        tilesLayer.getChildren().add(wall);
                    }
                    case PAC_DOT -> {
                        Circle pellet = new Circle(x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0, 3);
                        pellet.setFill(Color.WHITE);
                        tilesLayer.getChildren().add(pellet);
                    }
                    case ENERGIZER -> {
                        Circle powerup = new Circle(x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0, 8);
                        powerup.setFill(Color.YELLOW);
                        tilesLayer.getChildren().add(powerup);
                    }
                    case CHERRY, STRAWBERRY, ORANGE, APPLE, MELON, GALAXIAN, BELL, KEY -> {
                        Rectangle fruit = new Rectangle(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                        fruit.setFill(getFruitColor(tile));
                        tilesLayer.getChildren().add(fruit);
                    }
                }
            }
        }
    }

    private Color getFruitColor(TileType fruit) {
        return switch (fruit) {
            case CHERRY -> Color.RED;
            case STRAWBERRY -> Color.PINK;
            case ORANGE -> Color.ORANGE;
            case APPLE -> Color.DARKRED;
            case MELON -> Color.GREEN;
            case GALAXIAN -> Color.CYAN;
            case BELL -> Color.GOLD;
            case KEY -> Color.SILVER;
            default -> Color.WHITE;
        };
    }

    private void drawGhost(Ghost ghost) {
        if (ghost.position == null) return;

        Circle ghostCircle = new Circle(ghost.position.x, ghost.position.y, TILE_SIZE / 2.0 - 2);
        ghostCircle.setFill(getGhostColor(ghost.type));
        entitiesLayer.getChildren().add(ghostCircle);
    }

    private Color getGhostColor(GhostType type) {
        if (type == null) return Color.WHITE;
        return switch (type) {
            case RED -> Color.RED;
            case PINK -> Color.PINK;
            case CYAN -> Color.CYAN;
            case ORANGE -> Color.ORANGE;
            case PURPLE -> Color.PURPLE;
        };
    }

    private void drawPlayer(Player player) {
        if (player.getPosition() == null) return;

        player.getImageView().setLayoutX(player.getPosition().x - TILE_SIZE / 2.0);
        player.getImageView().setLayoutY(player.getPosition().y - TILE_SIZE / 2.0);
        entitiesLayer.getChildren().add(player.getImageView());
    }

    private void drawUI(GameState gameState) {
        StringBuilder info = new StringBuilder();
        info.append("Clock: ").append(gameState.clock()).append("\n");

        if (gameState.players() != null && !gameState.players().isEmpty()) {
            Player player = gameState.players().get(0);
            info.append("Score: ").append(player.getPoints()).append("\n");
            info.append("Lives: ").append(player.getLives()).append("\n");
            info.append("Health: ").append(player.getHealth());
        }

        if (gameState.winner() != null) {
            info.append("\n\nWINNER: Player ").append(gameState.winner().getId());
        }

        Label infoLabel = new Label(info.toString());
        infoLabel.setLayoutX(10);
        infoLabel.setLayoutY(10);
        infoLabel.setTextFill(Color.WHITE);
        infoLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10;");
        uiLayer.getChildren().add(infoLabel);
    }
}
