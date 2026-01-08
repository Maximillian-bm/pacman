package com.example.GameLogic;

import java.util.ArrayList;
import java.util.List;

import com.example.model.*;

import static com.example.model.Constants.TILE_SIZE;

import com.example.model.Maps;
import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;
import lombok.Getter;

public class ClientGameController extends GameController {

    private static final long DT_DIVISOR = 4_000_000L;

    @Getter
    private final Player player;

    public ClientGameController(int playerId, double startX, double startY, Direction startDir) {
        this.player = new Player(playerId);

        Position pos = new Position();
        pos.x = startX;
        pos.y = startY;

        player.setPosition(pos);
        player.setDirection(startDir);
    }

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        // Initialize game and return early
        if (gameState == null) return initializeGameState();

        // Actual update loop
        if (actions != null) {
            for (Action a : actions) {
                if (a == null) continue;
                if (a.getPlayerId() != player.getId()) continue;

                Direction d = directionFromMove(a.getMove());
                if (d != null) {
                    player.setDirection(d);
                }
            }
        }

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
        Player testPlayer = new Player(1);
        testPlayer.setPosition(new Position(
            5 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        ));
        players.add(testPlayer);

        // Create test ghosts
        Ghost ghost1 = new Ghost();
        ghost1.type = GhostType.RED;
        ghost1.position = new Position(
            3 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        );
        ghosts.add(ghost1);

        Ghost ghost2 = new Ghost();
        ghost2.type = GhostType.PINK;
        ghost1.position = new Position(
            2 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
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

    public void stepMovement(long deltaTime) {
        if (deltaTime <= 0) return;

        int dx = 0;
        int dy = 0;

        switch (player.getDirection()) {
            case WEST  -> dx = -1;
            case EAST  -> dx = 1;
            case NORTH -> dy = -1;
            case SOUTH -> dy = 1;
        }

        Position pos = player.getPosition();
        pos.x += dx * (deltaTime / (double) DT_DIVISOR);
        pos.y += dy * (deltaTime / (double) DT_DIVISOR);
        player.setPosition(pos);
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
}
