package com.example.GameLogic;

import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.EntityTracker;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.GhostType;
import com.example.model.Maps;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

import static com.example.model.Constants.*;

public class ClientGameController extends GameController {

    public GameState updateGameStateFor(GameState gameState, int tarclock) {
        // Create a deep copy to avoid mutating the original state
        gameState = deepCopyGameState(gameState);

        while (gameState.clock() < tarclock) {
            gameState = updateGameState(gameState, Constants.cleanActions.getActions(gameState.clock() + 1));
        }
        return gameState;
    }

    public GameState deepCopyGameState(GameState state) {
        List<Player> copiedPlayers = new ArrayList<>();
        for (Player p : state.players()) {
            copiedPlayers.add(p.copy());
        }

        List<Ghost> copiedGhosts = new ArrayList<>();
        for (Ghost g : state.ghosts()) {
            copiedGhosts.add(g.copy());
        }

        // Deep copy tiles
        TileType[][] originalTiles = state.tiles();
        TileType[][] copiedTiles = new TileType[originalTiles.length][originalTiles[0].length];
        for (int i = 0; i < originalTiles.length; i++) {
            System.arraycopy(originalTiles[i], 0, copiedTiles[i], 0, originalTiles[i].length);
        }

        return new GameState(state.clock(), copiedPlayers, copiedGhosts, copiedTiles, state.winner(), state.entityTracker().copy());
    }

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        gameState = deepCopyGameState(gameState);

        EntityTracker entityTracker = gameState.entityTracker();

        int newClock = gameState.clock() + 1;

        updateRespawnTimers(gameState);
        updatePlayerPowerTimers(gameState);
        updateInvulnerabilityTimers(gameState);

        if (entityTracker.getFrightenedTimerSec() > 0.0) {
            entityTracker.setFrightenedTimerSec(entityTracker.getFrightenedTimerSec() - (1.0 / TARGET_FPS));
            if (entityTracker.getFrightenedTimerSec() < 0.0) {
                entityTracker.setFrightenedTimerSec(0.0);
            }
        }

        handleActions(gameState, actions);
        stepMovement(gameState);
        handlePvPcollitions(gameState);
        GhostMovement(gameState);
        handleGhostPlayerCollisions(gameState);
        handlePlayerGridPosition(gameState);

        Player winner = gameState.winner();
        if (winner == null) {
            if (allPlayersDead(gameState) || allPointsGathered(gameState)) {
                winner = getWinner(gameState);
            }
        }
        TileType[][] tiles = gameState.tiles();
        // Handle fruit spawning based on pellets eaten
        handleFruitSpawning(gameState);

        // Handle fruit spawning based on pellets eaten
        handleFruitSpawning(gameState);

        GameState newGameState = new GameState(
            newClock,
            gameState.players(),
            gameState.ghosts(),
            tiles,
            winner,
            gameState.entityTracker().copy()
        );

        return newGameState;
    }

    private void handleFruitSpawning(GameState gameState) {
        // Count total points earned (approximation for pellets eaten)
        int totalPoints = 0;
        for (Player p : gameState.players()) {
            totalPoints += p.getPoints();
        }

        // Spawn cherry after ~70 pellets (700 points)
        if (totalPoints >= 700) {
            TileType[][] tiles = gameState.tiles();
            boolean hasFruit = false;
            for (TileType[] row : tiles) {
                for (TileType t : row) {
                    if (t == TileType.CHERRY || t == TileType.STRAWBERRY) {
                        hasFruit = true;
                        break;
                    }
                }
                if (hasFruit) break;
            }

            if (!hasFruit) {
                // Find an empty tile near center to spawn fruit
                int centerX = tiles[0].length / 2;
                int centerY = tiles.length / 2;
                for (int dx = 0; dx < tiles[0].length; dx++) {
                    for (int dy = 0; dy < tiles.length; dy++) {
                        int x = (centerX + dx) % tiles[0].length;
                        int y = (centerY + dy) % tiles.length;
                        if (tiles[y][x] == TileType.EMPTY) {
                            tiles[y][x] = TileType.CHERRY;
                            return;
                        }
                    }
                }
            }
        }
    }

    public GameState initializeGameState(int nrOfPlayers) {
        List<Player> players = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        TileType[][] tiles = Maps.getCurrentLevelTiles();

        for (int i = 0; i < nrOfPlayers; i++) {
            players.add(new Player(i));
        }

        for (GhostType type : GhostType.values()) {
            ghosts.add(new Ghost(type));
        }

        return new GameState(
            -1,
            players,
            ghosts,
            tiles,
            null,
            new EntityTracker()
        );
    }

    private void handleActions(GameState gameState, List<Action> actions) {
        if (actions == null) {
            return;
        }

        for (Action a : actions) {
            if (a == null) {
                continue;
            }

            Player player = gameState.players().stream()
                .filter(p -> p.getId() == a.getPlayerId())
                .findFirst()
                .orElse(null);

            if (player == null) {
                continue;
            }

            Direction d = directionFromMove(a.getMove());
            if (d != null) {
                player.setIntendedDirection(d);
            }

        }
    }

    private void stepMovement(GameState gameState) {
        EntityTracker entityTracker = gameState.entityTracker();

        gameState.players().forEach(player -> {
            if (!player.isAlive()) {
                return;
            }
            Position pos = player.getPosition();

            boolean isFrightened = entityTracker.isAnyPowerActive() && !entityTracker.isPowerOwner(player);

            double movementPerFrame = (isFrightened ? PLAYER_FRIGHTENED_SPEED : PLAYER_SPEED) / TARGET_FPS;

            Direction intendedDir = player.getIntendedDirection();

            if (intendedDir != null && intendedDir != player.getDirection()) {
                Pair<Integer, Integer> gridPos = pos.ToGridPosition();
                int gridX = gridPos.getKey();
                int gridY = gridPos.getValue();
                double gridCenterX = gridX * TILE_SIZE;
                double gridCenterY = gridY * TILE_SIZE;

                int diff = Math.abs(intendedDir.ordinal() - player.getDirection().ordinal());
                boolean is90DegreeTurn = (diff == 1 || diff == 3);

                if (is90DegreeTurn) {
                    boolean shouldTurn = false;

                    switch (player.getDirection()) {
                        case WEST, EAST -> {
                            double nextX = pos.x + (player.getDirection() == Direction.EAST ? movementPerFrame
                                : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.x <= gridCenterX && nextX >= gridCenterX) ||
                                (pos.x >= gridCenterX && nextX <= gridCenterX);
                            shouldTurn = wouldCrossCenter;
                        }
                        case NORTH, SOUTH -> {
                            double nextY = pos.y + (player.getDirection() == Direction.SOUTH ? movementPerFrame
                                : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.y <= gridCenterY && nextY >= gridCenterY) ||
                                (pos.y >= gridCenterY && nextY <= gridCenterY);
                            shouldTurn = wouldCrossCenter;
                        }
                    }

                    if (shouldTurn) {
                        int nextGridX = gridX;
                        int nextGridY = gridY;
                        switch (intendedDir) {
                            case WEST -> nextGridX--;
                            case EAST -> nextGridX++;
                            case NORTH -> nextGridY--;
                            case SOUTH -> nextGridY++;
                        }

                        TileType[][] tiles = gameState.tiles();
                        if (nextGridX >= 0 && nextGridX < tiles[0].length &&
                            nextGridY >= 0 && nextGridY < tiles.length &&
                            tiles[nextGridY][nextGridX] != TileType.WALL) {

                            pos.x = gridCenterX;
                            pos.y = gridCenterY;
                            player.setDirection(intendedDir);
                        }
                    }
                } else {
                    player.setDirection(intendedDir);
                }
            }

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

            double mapWidth = tiles[0].length * TILE_SIZE;
            double mapHeight = tiles.length * TILE_SIZE;

            // Wrap around at map boundaries
            if (pos.x < 0) {
                pos.x += mapWidth;
            } else if (pos.x >= mapWidth) {
                pos.x -= mapWidth;
            }

            if (pos.y < 0) {
                pos.y += mapHeight;
            } else if (pos.y >= mapHeight) {
                pos.y -= mapHeight;
            }

            // Skip wall collision check if player is in wrap-around zone (at the edges)
            // A small margin helps avoid getting stuck on walls exactly at the boundary
            boolean inWrapZone = pos.x < 1.0 || pos.y < 1.0 ||
                pos.x >= mapWidth - 1.0 ||
                pos.y >= mapHeight - 1.0;

            if (!inWrapZone) {
                double margin = 0.1;

                if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                    isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                    isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                    isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {

                    Pair<Integer, Integer> currentGridPos = pos.ToGridPosition();
                    int targetGridX = currentGridPos.getKey();
                    int targetGridY = currentGridPos.getValue();

                    int maxIterations = tiles.length + tiles[0].length;
                    int iterations = 0;
                    while (iterations < maxIterations) {
                        if (targetGridX >= 0 && targetGridX < tiles[0].length &&
                            targetGridY >= 0 && targetGridY < tiles.length &&
                            tiles[targetGridY][targetGridX] != TileType.WALL) {
                            break;
                        }
                        targetGridX -= dx;
                        targetGridY -= dy;
                        iterations++;
                    }

                    if (iterations < maxIterations) {
                        pos.x = targetGridX * TILE_SIZE;
                        pos.y = targetGridY * TILE_SIZE;
                    }
                }
            }

            player.setPosition(pos);
        });
    }

    private void handlePlayerGridPosition(GameState gameState) {
        EntityTracker entityTracker = gameState.entityTracker();
        gameState.players().forEach(player -> {
            if (player == null || !player.isAlive() || player.getRespawnTimer() > 0.0) return;

            Pair<Integer, Integer> gp = player.getPosition().ToGridPosition();
            TileType[][] tiles = gameState.tiles();
            int tileX = gp.getKey();
            int tileY = gp.getValue();

            if (tileY < 0 || tileY >= tiles.length || tileX < 0 || tileX >= tiles[0].length) return;

            TileType tileType = tiles[tileY][tileX];
            if (tileType == TileType.CHERRY || tileType == TileType.STRAWBERRY || tileType == TileType.ORANGE || tileType == TileType.APPLE || tileType == TileType.MELON)
                player.setAteFruit(true);

            if (isPowerup(tileType) && !entityTracker.isAnyPowerActive()) {
                entityTracker.assignPowerTo(player);

                for (Player other : gameState.players()) {
                    if (other != null && other.getId() != player.getId()) {
                        other.setPowerUpTimer(0.0);
                    }
                }

                player.setAtePowerUp(true);
                player.setPowerUpTimer(FRIGHTENED_DURATION_SEC);
                entityTracker.setFrightenedTimerSec(FRIGHTENED_DURATION_SEC);

                for (Ghost g : gameState.ghosts()) {
                    g.setDirection(oppositeDir(getGhostDir(g)));
                }
            }

            player.addPoints(tileType.points);

            switch (tileType) {
                case EMPTY, WALL -> { }
                default -> tiles[tileY][tileX] = TileType.EMPTY;
            }
        });
    }

    private boolean isPowerup(TileType t) {
        return t == TileType.ENERGIZER;
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
        int gridX = (int) (x / TILE_SIZE);
        int gridY = (int) (y / TILE_SIZE);

        if (gridX < 0 || gridX >= tiles[0].length || gridY < 0 || gridY >= tiles.length) {
            return false;
        }

        return tiles[gridY][gridX] == TileType.WALL;
    }

    private Direction getGhostDir(Ghost ghost) {
        Direction d = ghost.getDirection();
        return (d != null) ? d : Direction.WEST;
    }

    private Direction oppositeDir(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    private int countWalkableNeighbors(TileType[][] tiles, int x, int y) {
        int count = 0;
        if (isWalkableInDir(tiles, x, y, Direction.NORTH)) {
            count++;
        }
        if (isWalkableInDir(tiles, x, y, Direction.SOUTH)) {
            count++;
        }
        if (isWalkableInDir(tiles, x, y, Direction.EAST)) {
            count++;
        }
        if (isWalkableInDir(tiles, x, y, Direction.WEST)) {
            count++;
        }
        return count;
    }

    private boolean isWalkable(TileType[][] tiles, int x, int y) {
        if (x < 0 || x >= tiles[0].length || y < 0 || y >= tiles.length) {
            return true;
        }
        return tiles[y][x] != TileType.WALL;
    }

    private boolean isWalkableInDir(TileType[][] tiles, int x, int y, Direction d) {
        return switch (d) {
            case WEST -> isWalkable(tiles, x - 1, y);
            case EAST -> isWalkable(tiles, x + 1, y);
            case NORTH -> isWalkable(tiles, x, y - 1);
            case SOUTH -> isWalkable(tiles, x, y + 1);
        };
    }

    private Direction chooseBestDirTowardTarget(TileType[][] tiles, int gx, int gy, Direction currentDir,
                                                int targetX, int targetY) {
        List<Direction> candidates = new ArrayList<>(4);

        for (Direction d : Direction.values()) {
            if (!isWalkableInDir(tiles, gx, gy, d)) {
                continue;
            }
            if (d == oppositeDir(currentDir)) {
                continue;
            }
            candidates.add(d);
        }

        if (candidates.isEmpty()) {
            for (Direction d : Direction.values()) {
                if (isWalkableInDir(tiles, gx, gy, d)) {
                    candidates.add(d);
                }
            }
        }

        if (candidates.isEmpty()) {
            return currentDir;
        }

        List<Direction> tieBreak = List.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);

        Direction best = candidates.getFirst();
        int bestDist = Integer.MAX_VALUE;

        for (Direction d : tieBreak) {
            if (!candidates.contains(d)) {
                continue;
            }

            int nx = gx + (d == Direction.EAST ? 1 : d == Direction.WEST ? -1 : 0);
            int ny = gy + (d == Direction.SOUTH ? 1 : d == Direction.NORTH ? -1 : 0);

            int dx = targetX - nx;
            int dy = targetY - ny;
            int dist = dx * dx + dy * dy;

            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }

        return best;
    }

    private Direction chooseBestDirAwayFromPlayer(TileType[][] tiles, int gx, int gy, Direction currentDir,
                                                  int playerX, int playerY) {
        List<Direction> candidates = new ArrayList<>(4);

        for (Direction d : Direction.values()) {
            if (!isWalkableInDir(tiles, gx, gy, d)) {
                continue;
            }
            if (d == oppositeDir(currentDir)) {
                continue;
            }
            candidates.add(d);
        }

        if (candidates.isEmpty()) {
            for (Direction d : Direction.values()) {
                if (isWalkableInDir(tiles, gx, gy, d)) {
                    candidates.add(d);
                }
            }
        }

        if (candidates.isEmpty()) {
            return currentDir;
        }

        List<Direction> tieBreak = List.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);

        Direction best = candidates.getFirst();
        int bestDist = Integer.MIN_VALUE;

        for (Direction d : tieBreak) {
            if (!candidates.contains(d)) {
                continue;
            }

            int nx = gx + (d == Direction.EAST ? 1 : d == Direction.WEST ? -1 : 0);
            int ny = gy + (d == Direction.SOUTH ? 1 : d == Direction.NORTH ? -1 : 0);

            int dx = playerX - nx;
            int dy = playerY - ny;
            int dist = dx * dx + dy * dy;

            if (dist > bestDist) {
                bestDist = dist;
                best = d;
            }
        }

        return best;
    }

    private Player findNearestPlayer(GameState gameState, Ghost ghost) {
        if (gameState.players() == null || gameState.players().isEmpty()) {
            return null;
        }
        if (ghost == null || ghost.getPosition() == null) {
            return null;
        }

        Pair<Integer, Integer> g = ghost.getPosition().ToGridPosition();
        int gx = g.getKey();
        int gy = g.getValue();

        Player best = null;
        int bestDist2 = Integer.MAX_VALUE;

        for (Player p : gameState.players()) {
            if (p == null || p.getPosition() == null) {
                continue;
            }

            if (!p.isAlive() || p.getRespawnTimer() > 0.0) {
                continue;
            }

            Pair<Integer, Integer> pt = p.getPosition().ToGridPosition();
            int px = pt.getKey();
            int py = pt.getValue();

            int dx = px - gx;
            int dy = py - gy;
            int dist2 = dx * dx + dy * dy;

            if (dist2 < bestDist2 || (dist2 == bestDist2 && best != null && p.getId() < best.getId())) {
                bestDist2 = dist2;
                best = p;
            }
            if (best == null) {
                best = p;
                bestDist2 = dist2;
            }
        }

        return best;
    }

    private Pair<Integer, Integer> getScatterCorner(GameState gameState, Ghost ghost) {
        int maxX = gameState.tiles()[0].length - 1;
        int maxY = gameState.tiles().length - 1;

        return switch (ghost.getType()) {
            case RED -> new Pair<>(maxX, 0);
            case PINK -> new Pair<>(0, 0);
            case CYAN -> new Pair<>(maxX, maxY);
            case ORANGE -> new Pair<>(0, maxY);
            case PURPLE -> new Pair<>(maxX / 2, maxY / 2);
            default -> new Pair<>(maxX, 0);
        };
    }

    private Pair<Integer, Integer> computeGhostTargetTile(GameState gameState, Ghost ghost, Player pac, Ghost blinky) {
        Pair<Integer, Integer> pacGrid = pac.getPosition().ToGridPosition();
        int px = pacGrid.getKey();
        int py = pacGrid.getValue();
        Direction pDir = pac.getDirection();

        int maxX = gameState.tiles()[0].length - 1;
        int maxY = gameState.tiles().length - 1;

        Pair<Integer, Integer> redCorner = new Pair<>(maxX, 0);
        Pair<Integer, Integer> pinkCorner = new Pair<>(0, 0);
        Pair<Integer, Integer> blueCorner = new Pair<>(maxX, maxY);
        Pair<Integer, Integer> orangeCorner = new Pair<>(0, maxY);
        Pair<Integer, Integer> purpleCorner = new Pair<>(maxX / 2, maxY / 2);

        if (gameState.entityTracker().isGhostScatterMode()) {
            return switch (ghost.getType()) {
                case RED -> redCorner;
                case PINK -> pinkCorner;
                case CYAN -> blueCorner;
                case ORANGE -> orangeCorner;
                case PURPLE -> purpleCorner;
                default -> redCorner;
            };
        }

        return switch (ghost.getType()) {
            case RED -> new Pair<>(px, py);

            case PINK -> {
                int tx = px + 4 * (pDir == Direction.EAST ? 1 : pDir == Direction.WEST ? -1 : 0);
                int ty = py + 4 * (pDir == Direction.SOUTH ? 1 : pDir == Direction.NORTH ? -1 : 0);
                yield new Pair<>(tx, ty);
            }

            case CYAN -> {
                int p2x = px + 2 * (pDir == Direction.EAST ? 1 : pDir == Direction.WEST ? -1 : 0);
                int p2y = py + 2 * (pDir == Direction.SOUTH ? 1 : pDir == Direction.NORTH ? -1 : 0);

                if (blinky == null || blinky.getPosition() == null) {
                    yield new Pair<>(p2x, p2y);
                }

                Pair<Integer, Integer> blGrid = blinky.getPosition().ToGridPosition();
                int bx = blGrid.getKey();
                int by = blGrid.getValue();

                int vx = p2x - bx;
                int vy = p2y - by;

                yield new Pair<>(p2x + vx, p2y + vy);
            }

            case ORANGE -> {
                Pair<Integer, Integer> gGrid = ghost.getPosition().ToGridPosition();
                int gx = gGrid.getKey();
                int gy = gGrid.getValue();

                int dx = px - gx;
                int dy = py - gy;
                int dist2 = dx * dx + dy * dy;

                if (dist2 > 8 * 8) {
                    yield new Pair<>(px, py);
                } else {
                    yield orangeCorner;
                }
            }
            case PURPLE -> {
                int tx = px + -1 * (pDir == Direction.EAST ? 1 : pDir == Direction.WEST ? -1 : 0);
                int ty = py + -1 * (pDir == Direction.SOUTH ? 1 : pDir == Direction.NORTH ? -1 : 0);
                yield new Pair<>(tx, ty);
            }

            default -> new Pair<>(px, py);
        };
    }

    private void GhostMovement(GameState gameState) {

        EntityTracker entityTracker = gameState.entityTracker();

        if (gameState.ghosts() == null || gameState.ghosts().isEmpty()) {
            return;
        }
        if (gameState.players() == null || gameState.players().isEmpty()) {
            return;
        }

        TileType[][] tiles = gameState.tiles();
        if (tiles == null) {
            return;
        }

        boolean frightened = entityTracker.getFrightenedTimerSec() > 0.0;

        if (!frightened) {
            entityTracker.setGhostChaseTimer(entityTracker.getGhostChaseTimer() + (1.0 / TARGET_FPS));
            if (entityTracker.isGhostScatterMode() && entityTracker.getGhostChaseTimer() >= 7.0) {
                entityTracker.setGhostScatterMode(false);
                entityTracker.setGhostChaseTimer(0.0);
                for (Ghost g : gameState.ghosts()) {
                    g.setDirection(oppositeDir(getGhostDir(g)));

                }
            } else if (!entityTracker.isGhostScatterMode() && entityTracker.getGhostChaseTimer() >= 20.0) {
                entityTracker.setGhostScatterMode(true);
                entityTracker.setGhostChaseTimer(0.0);
                for (Ghost g : gameState.ghosts()) {
                    g.setDirection(oppositeDir(getGhostDir(g)));
                }
            }
        }

        Ghost blinky = gameState.ghosts().stream()
            .filter(g -> g.getType() == GhostType.RED)
            .findFirst()
            .orElse(null);

        double speed = frightened ? (entityTracker.getGhostSpeed() * 0.75) : entityTracker.getGhostSpeed();
        double movePerFrame = speed / TARGET_FPS;

        for (Ghost ghost : gameState.ghosts()) {
            if (ghost.getRespawnTimer() > 0.0) {
                continue;
            }
            if (ghost == null || ghost.getPosition() == null) {
                continue;
            }
            Player targetPlayer = findNearestPlayer(gameState, ghost);

            Position pos = ghost.getPosition();
            Direction dir = getGhostDir(ghost);

            double mapWidth = tiles[0].length * TILE_SIZE;
            double mapHeight = tiles.length * TILE_SIZE;

            Pair<Integer, Integer> gridPos = pos.ToGridPosition();
            int gx = gridPos.getKey();
            int gy = gridPos.getValue();

            double centerX = gx * TILE_SIZE;
            double centerY = gy * TILE_SIZE;

            boolean nearCenter =
                Math.abs(pos.x - centerX) < CENTER_EPS_PX && Math.abs(pos.y - centerY) < CENTER_EPS_PX;
            if (nearCenter) {
                pos.x = centerX;
                pos.y = centerY;

                boolean blockedAhead = !isWalkableInDir(tiles, gx, gy, dir);
                boolean atIntersection = countWalkableNeighbors(tiles, gx, gy) >= 3;

                if (blockedAhead || atIntersection) {
                    if (targetPlayer != null && frightened) {
                        Pair<Integer, Integer> pGrid = targetPlayer.getPosition().ToGridPosition();
                        dir = chooseBestDirAwayFromPlayer(tiles, gx, gy, dir, pGrid.getKey(), pGrid.getValue());
                    } else {
                        Pair<Integer, Integer> targetTile;
                        if (targetPlayer == null) {
                            targetTile = getScatterCorner(gameState, ghost);
                        } else {
                            targetTile = computeGhostTargetTile(gameState, ghost, targetPlayer, blinky);
                        }
                        dir = chooseBestDirTowardTarget(tiles, gx, gy, dir, targetTile.getKey(),
                            targetTile.getValue());
                    }
                    ghost.setDirection(dir);
                }
            } else {
                if (!isWalkableInDir(tiles, gx, gy, dir)) {
                    pos.x = centerX;
                    pos.y = centerY;
                }
            }

            int dx = 0, dy = 0;
            switch (dir) {
                case WEST -> dx = -1;
                case EAST -> dx = 1;
                case NORTH -> dy = -1;
                case SOUTH -> dy = 1;
            }

            pos.x += dx * movePerFrame;
            pos.y += dy * movePerFrame;

            if (pos.x < 0) {
                pos.x += mapWidth;
            } else if (pos.x >= mapWidth) {
                pos.x -= mapWidth;
            }
            if (pos.y < 0) {
                pos.y += mapHeight;
            } else if (pos.y >= mapHeight) {
                pos.y -= mapHeight;
            }

            // Skip wall collision check if ghost is in wrap-around zone
            boolean inWrapZone = pos.x < 1.0 || pos.y < 1.0 ||
                pos.x >= mapWidth - 1.0 ||
                pos.y >= mapHeight - 1.0;

            if (!inWrapZone) {
                double margin = 0.1;
                if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                    isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                    isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                    isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {

                    Pair<Integer, Integer> currentGridPos = pos.ToGridPosition();
                    int targetGridX = currentGridPos.getKey();
                    int targetGridY = currentGridPos.getValue();

                    int maxIterations = tiles.length + tiles[0].length;
                    int iterations = 0;
                    while (iterations < maxIterations) {
                        if (targetGridX >= 0 && targetGridX < tiles[0].length &&
                            targetGridY >= 0 && targetGridY < tiles.length &&
                            tiles[targetGridY][targetGridX] != TileType.WALL) {
                            break;
                        }
                        targetGridX -= dx;
                        targetGridY -= dy;
                        iterations++;
                    }

                    if (iterations < maxIterations) {
                        pos.x = targetGridX * TILE_SIZE;
                        pos.y = targetGridY * TILE_SIZE;
                    }
                }
            }

            ghost.setPosition(pos);
        }
    }

    private void handleGhostPlayerCollisions(GameState gameState) {
        EntityTracker entityTracker = gameState.entityTracker();
        if (gameState.players() == null || gameState.ghosts() == null) return;

        boolean frightened = entityTracker.getFrightenedTimerSec() > 0.0;

        for (Player player : gameState.players()) {
            if (player == null || player.getPosition() == null) continue;
            if (!player.isAlive() || player.getRespawnTimer() > 0.0) continue;

            // spawn protection
            if (isInvulnerable(player)) continue;

            for (Ghost ghost : gameState.ghosts()) {
                if (ghost == null || ghost.getPosition() == null) continue;
                if (ghost.getRespawnTimer() > 0.0) continue;

                if (player.distanceTo(ghost) > Constants.COLLISION_DISTANCE_PVG) continue;

                if (frightened) {
                    if (entityTracker.isPowerOwner(player)) {
                        player.eatGhost();
                        ghost.setRespawnTimer(GHOST_RESPAWN_DELAY_SEC);
                        ghost.setPosition(new Position(-1000, -1000));
                    }
                    continue;
                }

                int livesLeft = player.loseLife();

                if (livesLeft <= 0) {
                    player.setAlive(false);
                    player.setIntendedDirection(null);
                    player.setPosition(new Position(-1000, -1000));
                    break;
                }

                player.setAlive(false);
                player.setRespawnTimer(PLAYER_RESPAWN_DELAY_SEC);
                player.setPosition(new Position(-1000, -1000));
                player.setIntendedDirection(null);
                break;
            }
        }
    }

    private void updateRespawnTimers(GameState gameState) {
        double dt = 1.0 / TARGET_FPS;

        for (Player p : gameState.players()) {
            if (p == null) {
                continue;
            }
            if (p.getRespawnTimer() > 0.0) {
                p.setRespawnTimer(Math.max(0.0, p.getRespawnTimer() - dt));

                if (p.getRespawnTimer() == 0.0 && p.getLives() > 0) {
                    Position sp = p.getSpawnPosition();
                    if (sp != null) {
                        p.setPosition(new Position(sp.x, sp.y));
                    }
                    p.setAlive(true);
                    p.setDirection(Direction.WEST);
                    p.setIntendedDirection(null);

                    // Check if spawning directly on a ghost (unsafe respawn)
                    boolean unsafeRespawn = false;
                    if (sp != null && gameState.entityTracker().getFrightenedTimerSec() <= 0.0) {
                        for (Ghost g : gameState.ghosts()) {
                            if (g == null || g.getRespawnTimer() > 0.0) continue;
                            Position gp = g.getPosition();
                            if (gp == null) continue;
                            double dist = Math.sqrt(Math.pow(sp.x - gp.x, 2) + Math.pow(sp.y - gp.y, 2));
                            if (dist <= Constants.COLLISION_DISTANCE_PVG) {
                                unsafeRespawn = true;
                                break;
                            }
                        }
                    }

                    if (unsafeRespawn) {
                        // Die immediately on unsafe respawn
                        int livesLeft = Math.max(0, p.getLives() - 1);
                        p.setLives(livesLeft);
                        p.setAlive(false);
                        p.setRespawnTimer(livesLeft > 0 ? PLAYER_RESPAWN_DELAY_SEC : 0.0);
                        p.setPosition(new Position(-1000, -1000));
                        p.setIntendedDirection(null);
                    } else {
                        p.setInvulnerableTimer(Constants.PLAYER_SPAWN_PROTECT_SEC);
                    }
                }
            }
        }

        for (Ghost g : gameState.ghosts()) {
            if (g == null) {
                continue;
            }

            if (g.getRespawnTimer() > 0.0) {
                g.setRespawnTimer(Math.max(0.0, g.getRespawnTimer() - dt));

                if (g.getRespawnTimer() == 0.0) {
                    Position sp = g.getSpawnPosition();
                    if (sp != null) {
                        g.setPosition(new Position(sp.x, sp.y));
                    }
                    g.setDirection(Direction.WEST);
                }
            }
        }
    }

    private void updatePlayerPowerTimers(GameState gameState) {
        EntityTracker entityTracker = gameState.entityTracker();
        double dt = 1.0 / TARGET_FPS;

        for (Player p : gameState.players()) {
            if (p == null) continue;

            if (p.getPowerUpTimer() > 0.0) {
                p.setPowerUpTimer(Math.max(0.0, p.getPowerUpTimer() - dt));
            }
        }

        boolean cleared = entityTracker.clearPowerIfOwnerInvalid(gameState.players());
        if (cleared) {
            entityTracker.setFrightenedTimerSec(0.0);
        }
    }

    private boolean isPowered(Player p, EntityTracker entityTracker) {
        return entityTracker.isPowerOwner(p);
    }

    private void handlePvPcollitions(GameState gameState) {
        EntityTracker entityTracker = gameState.entityTracker();
        List<Player> players = gameState.players();
        if (players == null || players.size() < 2) return;

        for (int i = 0; i < players.size(); i++) {
            Player a = players.get(i);
            if (!isPlayerCollidable(a)) continue;

            for (int j = i + 1; j < players.size(); j++) {
                Player b = players.get(j);
                if (!isPlayerCollidable(b)) continue;

                if (a.distanceTo(b) > Constants.COLLISION_DISTANCE_PVP) continue;

                boolean aPow = isPowered(a, entityTracker);
                boolean bPow = isPowered(b, entityTracker);

                if (aPow ^ bPow) {
                    Player eater = aPow ? a : b;
                    Player victim = aPow ? b : a;

                    eatPlayer(gameState, eater, victim);
                    break;
                }
                resolvePlayerOverlap(a, b);
            }
        }
    }

    private boolean isPlayerCollidable(Player p) {
        return p != null
            && p.getPosition() != null
            && p.isAlive()
            && p.getRespawnTimer() <= 0.0
            && !isInvulnerable(p);
    }


    private void eatPlayer(GameState gameState, Player eater, Player victim) {
        eater.addPoints(500);

        int livesLeft = Math.max(0, victim.getLives() - 1);
        victim.setLives(livesLeft);

        if (livesLeft <= 0) {
            victim.setAlive(false);
            victim.setIntendedDirection(null);
            victim.setPosition(new Position(-1000, -1000));
            return;
        }

        victim.setAlive(false);
        victim.setRespawnTimer(PLAYER_RESPAWN_DELAY_SEC);
        victim.setPosition(new Position(-1000, -1000));
        victim.setIntendedDirection(null);

        victim.setPowerUpTimer(0.0);
    }

    private void resolvePlayerOverlap(Player a, Player b) {
        Position pa = a.getPosition();
        Position pb = b.getPosition();

        double overlapX = Math.min(pa.x + TILE_SIZE, pb.x + TILE_SIZE) - Math.max(pa.x, pb.x);
        double overlapY = Math.min(pa.y + TILE_SIZE, pb.y + TILE_SIZE) - Math.max(pa.y, pb.y);

        if (overlapX <= 0 || overlapY <= 0) return;

        if (overlapX < overlapY) {
            double push = overlapX / 2.0;
            if (pa.x < pb.x) {
                pa.x -= push;
                pb.x += push;
            } else {
                pa.x += push;
                pb.x -= push;
            }
        } else {
            double push = overlapY / 2.0;
            if (pa.y < pb.y) {
                pa.y -= push;
                pb.y += push;
            } else {
                pa.y += push;
                pb.y -= push;
            }
        }

        a.setPosition(pa);
        b.setPosition(pb);
    }

    private void updateInvulnerabilityTimers(GameState gameState) {
        double dt = 1.0 / TARGET_FPS;

        for (Player p : gameState.players()) {
            if (p == null) continue;

            if (p.getInvulnerableTimer() > 0.0) {
                p.setInvulnerableTimer(Math.max(0.0, p.getInvulnerableTimer() - dt));
            }
        }
    }

    private boolean isInvulnerable(Player p) {
        return p != null && p.getInvulnerableTimer() > 0.0;
    }

    public boolean allPlayersDead(GameState gameState) {
        for (Player player : gameState.players()) {
            if (0 < player.getLives()) {
                return false;
            }
        }
        return true;
    }

    public boolean allPointsGathered(GameState gameState) {
        TileType[][] tiles = gameState.tiles();
        for (TileType[] row : tiles) {
            for (int x = 0; x < tiles[0].length; x++) {
                switch (row[x]) {
                    case TileType.PAC_DOT:
                        return false;
                    case TileType.ENERGIZER:
                        return false;
                    case TileType.CHERRY:
                        return false;
                    case TileType.STRAWBERRY:
                        return false;
                    case TileType.ORANGE:
                        return false;
                    case TileType.APPLE:
                        return false;
                    case TileType.MELON:
                        return false;
                    case TileType.GALAXIAN:
                        return false;
                    case TileType.BELL:
                        return false;
                    case TileType.KEY:
                        return false;
                }
            }
        }
        return true;
    }

    private Player getWinner(GameState gameState) {
        List<Player> players = gameState.players();
        Player winner = players.get(0);
        for (Player player : players) {
            if (winner.getPoints() < player.getPoints()) {
                winner = player;
            }
        }
        return winner;
    }
}
