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
import lombok.Getter;

public class ClientGameController extends GameController {

    @Getter
    private Player localPlayer;

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        // Initialize game and return early
        if (gameState == null) return initializeGameState();

        // Actual update loop
        if (actions != null) {
            for (Action a : actions) {
                if (a == null) continue;
                if (a.getPlayerId() != localPlayer.getId()) continue;

                Direction d = directionFromMove(a.getMove());
                if (d != null) {
                    localPlayer.setDirection(d);
                }
            }
        }

        stepMovement();

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
            5 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        ));
        players.add(localPlayer);

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

    private void stepMovement() {
        int dx = 0;
        int dy = 0;

        switch (localPlayer.getDirection()) {
            case WEST  -> dx = -1;
            case EAST  -> dx = 1;
            case NORTH -> dy = -1;
            case SOUTH -> dy = 1;
        }

        Position pos = localPlayer.getPosition();
        pos.x += dx * PLAYER_SPEED;
        pos.y += dy * PLAYER_SPEED;
        localPlayer.setPosition(pos);
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
