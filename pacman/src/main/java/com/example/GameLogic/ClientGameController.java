package com.example.GameLogic;

import java.util.ArrayList;
import java.util.List;

import com.example.model.*;

import static com.example.model.Constants.PLAYER_SPEED;
import static com.example.model.Constants.TILE_SIZE;

import com.example.model.Maps;
import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;
import javafx.util.Pair;
import lombok.Getter;

public class ClientGameController extends GameController {

    @Getter
    private Player localPlayer;

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
            if (d != null && d != player.getDirection()) {
                // Check if there's a wall in the direction the player wants to turn
                Position pos = player.getPosition();
                Pair<Integer, Integer> gridPos = pos.ToGridPosition();
                int gridX = gridPos.getKey();
                int gridY = gridPos.getValue();

                // Calculate next grid position in the new direction
                int nextGridX = gridX;
                int nextGridY = gridY;
                switch (d) {
                    case WEST -> nextGridX--;
                    case EAST -> nextGridX++;
                    case NORTH -> nextGridY--;
                    case SOUTH -> nextGridY++;
                }

                TileType[][] tiles = gameState.tiles();

                // Check if next tile is a wall
                if (nextGridX >= 0 && nextGridX < tiles.length &&
                    nextGridY >= 0 && nextGridY < tiles[0].length &&
                    tiles[nextGridX][nextGridY] != TileType.WALL) {

                    int diff = Math.abs(d.ordinal() - player.getDirection().ordinal());
                    // 90-degree turn: difference is 1 or 3 (not 2 which is 180-degree)
                    if (diff == 1 || diff == 3) {
                        // Snap player to center of current tile when turning
                        pos.x = gridX * TILE_SIZE;
                        pos.y = gridY * TILE_SIZE;
                        player.setPosition(pos);
                    }

                    player.setDirection(d);
                }
            }
        }
    }

    private void stepMovement(GameState gameState) {
        gameState.players().forEach(player -> {
            int dx = 0;
            int dy = 0;

            switch (player.getDirection()) {
                case WEST -> dx = -1;
                case EAST -> dx = 1;
                case NORTH -> dy = -1;
                case SOUTH -> dy = 1;
            }

            Position pos = player.getPosition();
            double oldX = pos.x;
            double oldY = pos.y;

            pos.x += dx * PLAYER_SPEED;
            pos.y += dy * PLAYER_SPEED;

            TileType[][] tiles = gameState.tiles();

            double margin = 0.1; // Small margin to avoid checking exactly on tile boundaries

            // Check collision with player bounding box
            if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {
                // Player collided with wall, restore previous position
                pos.x = oldX;
                pos.y = oldY;
            }

            // Teleport player to the other side
            double mapWidth = tiles[0].length * TILE_SIZE;
            double mapHeight = tiles.length * TILE_SIZE;

            if (pos.x < 0) {
                pos.x = mapWidth - TILE_SIZE;
            } else if (pos.x >= mapWidth) {
                pos.x = 0;
            }

            if (pos.y < 0) {
                pos.y = mapHeight - TILE_SIZE;
            } else if (pos.y >= mapHeight) {
                pos.y = 0;
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

        // Check bounds
        if (gridX < 0 || gridX >= tiles.length || gridY < 0 || gridY >= tiles[0].length) {
            return true; // Treat out of bounds as walls
        }

        return tiles[gridX][gridY] == TileType.WALL;
    }
}
