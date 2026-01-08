package com.example.GameLogic;

import java.util.List;

import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Player;
import com.example.model.Position;

public class ClientGameController extends GameController {

    // Controls movement speed (lower = faster)
    private static final long DT_DIVISOR = 4_000_000L;

    private final Player player;
    private long lastTime = 0;

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

    /**
     * Main entry point for the client.
     * - Applies actions
     * - Advances movement using delta time
     * - Computes render frame for UI
     */
    public RenderFrame tick(long timeNanos, GameState gameState, List<Action> actions) {
        updateGameState(gameState, actions);

        long deltaTime = (lastTime == 0) ? 0 : (timeNanos - lastTime);
        stepMovement(deltaTime);

        RenderFrame frame = computeRenderFrame(timeNanos);

        lastTime = timeNanos;
        return frame;
    }

    /**
     * Applies direction changes from actions.
     * Movement itself is NOT handled here.
     */
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

    /**
     * Advances player position based on direction and elapsed time.
     */
    private void stepMovement(long deltaTime) {
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

    /**
     * Computes sprite-sheet coordinates and screen destination.
     * UI must render exactly what this returns.
     */
    private RenderFrame computeRenderFrame(long timeNanos) {
        Position pos = player.getPosition();

        // Sprite sheet constants (Pac-Man)
        int sx = 757;
        int sw = 32;
        int sh = 32;

        int syBase = 42;
        int sy = syBase;

        switch (player.getDirection()) {
            case WEST  -> sy = syBase + 43 * 6;
            case EAST  -> sy = syBase;
            case NORTH -> sy = syBase + 43 * 9;
            case SOUTH -> sy = syBase + 43 * 3;
        }

        long pacmanFrame = (timeNanos / 200_000_000L) % 4;

        double dx = pos.x;
        double dy = pos.y;
        double dw = 100;
        double dh = 100;

        if (pacmanFrame == 0) {
            return new RenderFrame(sx, syBase, sw, sh, dx, dy, dw, dh);
        } else if (pacmanFrame == 1 || pacmanFrame == 3) {
            return new RenderFrame(sx, sy + 43, sw, sh, dx, dy, dw, dh);
        } else {
            return new RenderFrame(sx, sy + 43 * 2, sw, sh, dx, dy, dw, dh);
        }
    }

    /**
     * Maps Action.move → Direction
     */
    private Direction directionFromMove(int move) {
        return switch (move) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.EAST;
            case 3 -> Direction.NORTH;
            case 4 -> Direction.SOUTH;
            default -> null;
        };
    }

    /**
     * Immutable render payload for UI.
     */
    public static final class RenderFrame {
        public final int sx, sy, sw, sh;
        public final double dx, dy, dw, dh;

        public RenderFrame(
                int sx, int sy, int sw, int sh,
                double dx, double dy, double dw, double dh) {
            this.sx = sx;
            this.sy = sy;
            this.sw = sw;
            this.sh = sh;
            this.dx = dx;
            this.dy = dy;
            this.dw = dw;
            this.dh = dh;
        }
    }
}
