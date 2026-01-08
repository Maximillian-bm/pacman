package com.example.GameLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.model.*;

import com.example.model.Maps;
import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;
import javafx.util.Pair;
import lombok.Getter;

import static com.example.model.Constants.*;

public class ClientGameController extends GameController {

    @Getter
    private Player localPlayer;

    // Store the last intended direction for each player
    private Map<Integer, Direction> intendedDirections = new HashMap<>();

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        // Initialize game and return early
        if (gameState == null) return initializeGameState();

        // Actual update loop
        handleActions(gameState, actions);
        stepMovement(gameState);
        handlePlayerGridPosition(gameState);

        GameState newGameState = new GameState(
            ClientMain.clock,
            gameState.players(),
            gameState.ghosts(),
            gameState.tiles(),
            gameState.winner()
        );

        return newGameState;
    }

    private GameState initializeGameState() {
        List<Player> players = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        TileType[][] tiles = Maps.getMap1();

        // Create test player
        localPlayer = new Player(0);
        localPlayer.setPosition(new Position(
            3 * TILE_SIZE,
            3 * TILE_SIZE
        ));
        players.add(localPlayer);

        // Create test ghosts
        Ghost ghost1 = new Ghost();
        ghost1.type = GhostType.RED;
        ghost1.position = new Position(
            3 * TILE_SIZE,
            TILE_SIZE
        );
        ghosts.add(ghost1);

        Ghost ghost2 = new Ghost();
        ghost2.type = GhostType.PINK;
        ghost1.position = new Position(
            2 * TILE_SIZE,
            TILE_SIZE
        );
        ghosts.add(ghost2);

        return new GameState(
            ClientMain.clock,
            players,
            ghosts,
            tiles,
            null
        );
    }

    private void handleActions(GameState gameState, List<Action> actions) {
        if (actions == null) return;

        for (Action a : actions) {
            if (a == null) continue;

            // Find the player that this action belongs to
            Player player = gameState.players().stream()
                .filter(p -> p.getId() == a.getPlayerId())
                .findFirst()
                .orElse(null);

            if (player == null) continue;

            Direction d = directionFromMove(a.getMove());
            if (d != null) {
                // Save the intended direction for this player
                intendedDirections.put(player.getId(), d);
            }
        }
    }

    private void stepMovement(GameState gameState) {
        gameState.players().forEach(player -> {
            Position pos = player.getPosition();

            double movementPerFrame = PLAYER_SPEED / TARGET_FPS;

            // Check if player wants to turn
            Direction intendedDir = intendedDirections.get(player.getId());

            if (intendedDir != null && intendedDir != player.getDirection()) {
                Pair<Integer, Integer> gridPos = pos.ToGridPosition();
                int gridX = gridPos.getKey();
                int gridY = gridPos.getValue();
                double gridCenterX = gridX * TILE_SIZE;
                double gridCenterY = gridY * TILE_SIZE;

                int diff = Math.abs(intendedDir.ordinal() - player.getDirection().ordinal());
                boolean is90DegreeTurn = (diff == 1 || diff == 3);

                // For 90-degree turns, check if we would cross or are near the grid center
                if (is90DegreeTurn) {
                    boolean shouldTurn = false;
                    double distanceToCenter = 0;

                    // Check based on current direction
                    switch (player.getDirection()) {
                        case WEST, EAST -> {
                            // Moving horizontally, check if we're near or would cross the vertical center line
                            distanceToCenter = Math.abs(pos.x - gridCenterX);
                            double nextX = pos.x + (player.getDirection() == Direction.EAST ? movementPerFrame : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.x <= gridCenterX && nextX >= gridCenterX) ||
                                                      (pos.x >= gridCenterX && nextX <= gridCenterX) ||
                                                      distanceToCenter <= movementPerFrame;
                            shouldTurn = wouldCrossCenter;
                        }
                        case NORTH, SOUTH -> {
                            // Moving vertically, check if we're near or would cross the horizontal center line
                            distanceToCenter = Math.abs(pos.y - gridCenterY);
                            double nextY = pos.y + (player.getDirection() == Direction.SOUTH ? movementPerFrame : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.y <= gridCenterY && nextY >= gridCenterY) ||
                                                      (pos.y >= gridCenterY && nextY <= gridCenterY) ||
                                                      distanceToCenter <= movementPerFrame;
                            shouldTurn = wouldCrossCenter;
                        }
                    }

                    if (shouldTurn) {
                        // Check if turn is valid (no wall in intended direction)
                        int nextGridX = gridX;
                        int nextGridY = gridY;
                        switch (intendedDir) {
                            case WEST -> nextGridX--;
                            case EAST -> nextGridX++;
                            case NORTH -> nextGridY--;
                            case SOUTH -> nextGridY++;
                        }

                        TileType[][] tiles = gameState.tiles();
                        if (nextGridX >= 0 && nextGridX < tiles.length &&
                            nextGridY >= 0 && nextGridY < tiles[0].length &&
                            tiles[nextGridX][nextGridY] != TileType.WALL) {

                            // Execute the turn: snap to grid center and change direction
                            pos.x = gridCenterX;
                            pos.y = gridCenterY;
                            player.setDirection(intendedDir);
                        }
                    }
                } else {
                    // 180-degree turn or same direction - just change direction without snapping
                    player.setDirection(intendedDir);
                }
            }

            // Now move in the (possibly new) direction
            int dx = 0;
            int dy = 0;

            switch (player.getDirection()) {
                case WEST -> dx = -1;
                case EAST -> dx = 1;
                case NORTH -> dy = -1;
                case SOUTH -> dy = 1;
            }

            pos.x += dx * movementPerFrame;
            pos.y += dy * movementPerFrame;

            TileType[][] tiles = gameState.tiles();

            // Teleport player to the other side (check this before collision)
            double mapWidth = tiles.length * TILE_SIZE;
            double mapHeight = tiles[0].length * TILE_SIZE;

            // Check when player center has crossed the boundary
            if (pos.x + TILE_SIZE / 2.0 <= 0) {
                // Center crossed left edge, appear on right
                pos.x = mapWidth - TILE_SIZE / 2.0;
            } else if (pos.x + TILE_SIZE / 2.0 >= mapWidth) {
                // Center crossed right edge, appear on left
                pos.x = -TILE_SIZE / 2.0;
            }

            if (pos.y + TILE_SIZE / 2.0 <= 0) {
                // Center crossed top edge, appear on bottom
                pos.y = mapHeight - TILE_SIZE / 2.0;
            } else if (pos.y + TILE_SIZE / 2.0 >= mapHeight) {
                // Center crossed bottom edge, appear on top
                pos.y = -TILE_SIZE / 2.0;
            }

            double margin = 0.1; // Small margin to avoid checking exactly on tile boundaries

            // Check collision with player bounding box
            if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {

                // Find the tile right before the wall in the direction of movement
                Pair<Integer, Integer> currentGridPos = pos.ToGridPosition();
                int targetGridX = currentGridPos.getKey();
                int targetGridY = currentGridPos.getValue();

                // Step back in the opposite direction until we find a non-wall tile
                while (true) {
                    // Check if current tile is valid and not a wall
                    if (targetGridX >= 0 && targetGridX < tiles.length &&
                        targetGridY >= 0 && targetGridY < tiles[0].length &&
                        tiles[targetGridX][targetGridY] != TileType.WALL) {
                        break; // Found a non-wall tile
                    }
                    // Step back in opposite direction of movement
                    targetGridX -= dx;
                    targetGridY -= dy;
                }

                // Snap to the center of the tile before the wall
                pos.x = targetGridX * TILE_SIZE;
                pos.y = targetGridY * TILE_SIZE;
            }

            player.setPosition(pos);
        });
    }

    private void handlePlayerGridPosition(GameState gameState) {
        gameState.players().forEach(player -> {
            Pair<Integer, Integer> playerGridPosition = player.getPosition().ToGridPosition();

            TileType[][] tiles = gameState.tiles();

            int tileX = playerGridPosition.getKey();
            int tileY = playerGridPosition.getValue();

            TileType tileType = tiles[tileX][tileY];

            player.addPoints(tileType.points);

            switch (tileType) {
                case EMPTY -> {
                }
                case WALL -> {
                }
                default -> tiles[tileX][tileY] = TileType.EMPTY;
            }
        });
    }

    private Direction directionFromMove(int move) {
        return switch (move) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.EAST;
            case 3 -> Direction.NORTH;
            case 4 -> Direction.SOUTH;
            default -> null;
        };
    }

    private boolean isWall(TileType[][] tiles, double x, double y) {
        int gridX = (int)(x / TILE_SIZE);
        int gridY = (int)(y / TILE_SIZE);

        // Check bounds - out of bounds is not a wall (allows teleportation)
        if (gridX < 0 || gridX >= tiles.length || gridY < 0 || gridY >= tiles[0].length) {
            return false;
        }

        return tiles[gridX][gridY] == TileType.WALL;
    }
}
