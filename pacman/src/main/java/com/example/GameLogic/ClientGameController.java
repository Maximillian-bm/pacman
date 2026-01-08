package com.example.GameLogic;

import java.util.List;

import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;

public class ClientGameController extends GameController {

    private static final long DT_DIVISOR = 4_000_000L;

    private final Player player;

    public ClientGameController(int playerId, double startX, double startY, Direction startDir) {
        this.player = new Player(playerId);

        Position pos = new Position();
        pos.x = startX;
        pos.y = startY;

        player.setPosition(pos);
        player.setDirection(startDir);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public GameState updateGameState(GameState gameState, List<Action> actions) {
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
        return gameState;
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
