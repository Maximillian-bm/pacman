package com.example.GameLogic;

import static com.example.model.Constants.CENTER_EPS_PX;
import static com.example.model.Constants.FRIGHTENED_DURATION_SEC;
import static com.example.model.Constants.GHOST_RESPAWN_DELAY_SEC;
import static com.example.model.Constants.PLAYER_LIVES;
import static com.example.model.Constants.PLAYER_RESPAWN_DELAY_SEC;
import static com.example.model.Constants.PLAYER_SPEED;
import static com.example.model.Constants.TARGET_FPS;
import static com.example.model.Constants.TILE_SIZE;

import com.example.model.Action;
import com.example.model.Constants;
import com.example.model.Direction;
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

public class ClientGameController extends GameController {

    public GameState updateGameStateFor(GameState gameState, int targetClock) {
        int clock = gameState.clock();
        while (clock < targetClock) {
            int currentClock = ++clock;
            List<Action> ActionOfClock = Constants.cleanActions.getActions(currentClock);
            gameState = updateGameState(gameState, ActionOfClock);
        }
        return gameState;
    }

    public GameState updateGameState(GameState gameState, List<Action> actions) {

        updateRespawnTimers(gameState);
        updatePlayerPowerTimers(gameState);
        if (Ghost.getFrightenedTimerSec() > 0.0) {
            Ghost.setFrightenedTimerSec(Ghost.getFrightenedTimerSec() - (1.0 / TARGET_FPS));
            if (Ghost.getFrightenedTimerSec() < 0.0) {
                Ghost.setFrightenedTimerSec(0.0);
            }
        }

        handleActions(gameState, actions);
        stepMovement(gameState);
        handlePvPcollitions(gameState);
        GhostMovement(gameState);
        handleGhostPlayerCollisions(gameState);
        handlePlayerGridPosition(gameState);
        
        GameState newGameState = new GameState(
            Constants.clock,
            gameState.players(),
            gameState.ghosts(),
            gameState.tiles(),
            gameState.winner()
        );

        return newGameState;
    }

    public GameState initializeGameState(int nrOfPlayers) {
        List<Player> players = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        TileType[][] tiles = Maps.getMap1();

        for (int i = 0; i < nrOfPlayers; i++) {
            Player player = new Player(i);

            Position spawnPosition;
            switch (i) {
                case 0:
                    spawnPosition = new Position(3 * TILE_SIZE, 3 * TILE_SIZE);
                    break;
                case 1:
                    spawnPosition = new Position(10 * TILE_SIZE, 3 * TILE_SIZE);
                    break;
                case 2:
                    spawnPosition = new Position(3 * TILE_SIZE, 10 * TILE_SIZE);
                    break;
                case 3:
                    spawnPosition = new Position(10 * TILE_SIZE, 10 * TILE_SIZE);
                    break;
                default:
                    spawnPosition = new Position(3 * TILE_SIZE, 3 * TILE_SIZE);
                    break;
            }
            player.setSpawnPosition(spawnPosition);
            player.setPosition(new Position(spawnPosition.x, spawnPosition.y));
            player.setLives(PLAYER_LIVES);
            player.setAlive(true);
            player.setDirection(Direction.EAST);
            player.setIntendedDirection(null);
            player.setRespawnTimer(0.0);
            players.add(player);
        }

        Ghost ghost1 = new Ghost(GhostType.RED);
        ghost1.setPosition(
            new Position(
                3 * TILE_SIZE,
                TILE_SIZE
            ));
        ghosts.add(ghost1);

        Ghost ghost2 = new Ghost(GhostType.PINK);
        ghost2.setPosition(
            new Position(
                2 * TILE_SIZE,
                TILE_SIZE
            ));
        ghosts.add(ghost2);

        Ghost ghost3 = new Ghost(GhostType.CYAN);
        ghost3.setPosition(
            new Position(
                2 * TILE_SIZE,
                TILE_SIZE
            ));
        ghosts.add(ghost3);

        Ghost ghost4 = new Ghost(GhostType.ORANGE);
        ghost4.setPosition(
            new Position(
                2 * TILE_SIZE,
                TILE_SIZE
            ));
        ghosts.add(ghost4);

        Ghost ghost5 = new Ghost(GhostType.PURPLE);
        ghost5.setPosition(
            new Position(
                2 * TILE_SIZE,
                TILE_SIZE
            ));
        ghosts.add(ghost5);

        ghost1.setSpawnPosition(new Position(3 * TILE_SIZE, TILE_SIZE));
        ghost2.setSpawnPosition(new Position(2 * TILE_SIZE, TILE_SIZE));
        ghost3.setSpawnPosition(new Position(2 * TILE_SIZE, TILE_SIZE));
        ghost4.setSpawnPosition(new Position(2 * TILE_SIZE, TILE_SIZE));
        ghost5.setSpawnPosition(new Position(2 * TILE_SIZE, TILE_SIZE));

        ghost1.setRespawnTimer(0.0);
        ghost2.setRespawnTimer(0.0);
        ghost3.setRespawnTimer(0.0);
        ghost4.setRespawnTimer(0.0);
        ghost5.setRespawnTimer(0.0);

        ghost1.setDirection(Direction.WEST);
        ghost2.setDirection(Direction.WEST);
        ghost3.setDirection(Direction.WEST);
        ghost4.setDirection(Direction.WEST);
        ghost5.setDirection(Direction.WEST);

        return new GameState(
            Constants.clock,
            players,
            ghosts,
            tiles,
            null
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
        gameState.players().forEach(player -> {
            if (!player.isAlive()) {
                return;
            }
            Position pos = player.getPosition();

            double movementPerFrame = PLAYER_SPEED / TARGET_FPS;

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
                            double distanceToCenter = Math.abs(pos.x - gridCenterX);
                            double nextX = pos.x + (player.getDirection() == Direction.EAST ? movementPerFrame
                                : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.x <= gridCenterX && nextX >= gridCenterX) ||
                                (pos.x >= gridCenterX && nextX <= gridCenterX) ||
                                distanceToCenter <= movementPerFrame;
                            shouldTurn = wouldCrossCenter;
                        }
                        case NORTH, SOUTH -> {
                            double distanceToCenter = Math.abs(pos.y - gridCenterY);
                            double nextY = pos.y + (player.getDirection() == Direction.SOUTH ? movementPerFrame
                                : -movementPerFrame);
                            boolean wouldCrossCenter = (pos.y <= gridCenterY && nextY >= gridCenterY) ||
                                (pos.y >= gridCenterY && nextY <= gridCenterY) ||
                                distanceToCenter <= movementPerFrame;
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
                        if (nextGridX >= 0 && nextGridX < tiles.length &&
                            nextGridY >= 0 && nextGridY < tiles[0].length &&
                            tiles[nextGridX][nextGridY] != TileType.WALL) {

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

            double mapWidth = tiles.length * TILE_SIZE;
            double mapHeight = tiles[0].length * TILE_SIZE;

            if (pos.x + TILE_SIZE / 2.0 <= 0) {
                pos.x = mapWidth - TILE_SIZE / 2.0;
            } else if (pos.x + TILE_SIZE / 2.0 >= mapWidth) {
                pos.x = -TILE_SIZE / 2.0;
            }

            if (pos.y + TILE_SIZE / 2.0 <= 0) {
                pos.y = mapHeight - TILE_SIZE / 2.0;
            } else if (pos.y + TILE_SIZE / 2.0 >= mapHeight) {
                pos.y = -TILE_SIZE / 2.0;
            }

            double margin = 0.1;

            if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {

                Pair<Integer, Integer> currentGridPos = pos.ToGridPosition();
                int targetGridX = currentGridPos.getKey();
                int targetGridY = currentGridPos.getValue();

                while (true) {
                    if (targetGridX >= 0 && targetGridX < tiles.length &&
                        targetGridY >= 0 && targetGridY < tiles[0].length &&
                        tiles[targetGridX][targetGridY] != TileType.WALL) {
                        break;
                    }
                    targetGridX -= dx;
                    targetGridY -= dy;
                }

                pos.x = targetGridX * TILE_SIZE;
                pos.y = targetGridY * TILE_SIZE;
            }

            player.setPosition(pos);
        });
    }

    private void handlePlayerGridPosition(GameState gameState) {
        gameState.players().forEach(player -> {
            if (player == null || !player.isAlive() || player.getRespawnTimer() > 0.0) {
                return;
            }
            Pair<Integer, Integer> playerGridPosition = player.getPosition().ToGridPosition();

            TileType[][] tiles = gameState.tiles();
            int tileX = playerGridPosition.getKey();
            int tileY = playerGridPosition.getValue();

            TileType tileType = tiles[tileX][tileY];
            player.addPoints(tileType.points);

        if (isPowerup(tileType)) {
            player.setPowerUpTimer(FRIGHTENED_DURATION_SEC);
            Ghost.setFrightenedTimerSec(FRIGHTENED_DURATION_SEC);

        for (Ghost g : gameState.ghosts()) {
            g.setDirection(oppositeDir(getGhostDir(g)));
        }

            tiles[tileX][tileY] = TileType.EMPTY;
            return;
        }
            switch (tileType) {
                case EMPTY -> {
                }
                case WALL -> {
                }
                default -> tiles[tileX][tileY] = TileType.EMPTY;
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

        if (gridX < 0 || gridX >= tiles.length || gridY < 0 || gridY >= tiles[0].length) {
            return false;
        }

        return tiles[gridX][gridY] == TileType.WALL;
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
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            return true;
        }
        return tiles[x][y] != TileType.WALL;
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

        List<Direction> tieBreak = List.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);

        Direction best = candidates.get(0);
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

        List<Direction> tieBreak = List.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);

        Direction best = candidates.get(0);
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

    private Pair<Integer, Integer> computeGhostTargetTile(GameState gameState, Ghost ghost, Player pac,
        Ghost blinky) {
        Pair<Integer, Integer> pacGrid = pac.getPosition().ToGridPosition();
        int px = pacGrid.getKey();
        int py = pacGrid.getValue();
        Direction pDir = pac.getDirection();

        int maxX = gameState.tiles().length - 1;
        int maxY = gameState.tiles()[0].length - 1;

        Pair<Integer, Integer> redCorner = new Pair<>(maxX, 0);
        Pair<Integer, Integer> pinkCorner = new Pair<>(0, 0);
        Pair<Integer, Integer> blueCorner = new Pair<>(maxX, maxY);
        Pair<Integer, Integer> orangeCorner = new Pair<>(0, maxY);
        Pair<Integer, Integer> purpleCorner = new Pair<>(maxX / 2, maxY / 2);

        if (Ghost.isGhostScatterMode()) {
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

        boolean frightened = Ghost.getFrightenedTimerSec() > 0.0;

        if (!frightened) {
            Ghost.setGhostChaseTimer(Ghost.getGhostChaseTimer() + (1.0 / TARGET_FPS));
            if (Ghost.isGhostScatterMode() && Ghost.getGhostChaseTimer() >= 7.0) {
                Ghost.setGhostScatterMode(false);
                Ghost.setGhostChaseTimer(0.0);
                for (Ghost g : gameState.ghosts()) {
                    g.setDirection(oppositeDir(getGhostDir(g)));

                }
            } else if (!Ghost.isGhostScatterMode() && Ghost.getGhostChaseTimer() >= 20.0) {
                Ghost.setGhostScatterMode(true);
                Ghost.setGhostChaseTimer(0.0);
                for (Ghost g : gameState.ghosts()) {
                    g.setDirection(oppositeDir(getGhostDir(g)));
                }
            }
        }

        Ghost blinky = gameState.ghosts().stream()
            .filter(g -> g.getType() == GhostType.RED)
            .findFirst()
            .orElse(null);

        double speed = frightened ? (Ghost.getGHOSTSPEED() * 0.75) : Ghost.getGHOSTSPEED();
        double movePerFrame = speed / TARGET_FPS;

        for (Ghost ghost : gameState.ghosts()) {
            if (ghost.getRespawnTimer() > 0.0) {
                continue;
            }
            if (ghost == null || ghost.getPosition() == null) {
                continue;
            }
            Player targetPlayer = findNearestPlayer(gameState, ghost);
            if (targetPlayer == null) {
                continue;
            }

            Position pos = ghost.getPosition();
            Direction dir = getGhostDir(ghost);

            double mapWidth = tiles.length * TILE_SIZE;
            double mapHeight = tiles[0].length * TILE_SIZE;

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
                    if (frightened) {
                        Pair<Integer, Integer> pGrid = targetPlayer.getPosition().ToGridPosition();
                        dir = chooseBestDirAwayFromPlayer(tiles, gx, gy, dir, pGrid.getKey(), pGrid.getValue());
                    } else {
                        Pair<Integer, Integer> targetTile = computeGhostTargetTile(gameState, ghost, targetPlayer,
                            blinky);
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

            if (pos.x + TILE_SIZE / 2.0 <= 0) {
                pos.x = mapWidth - TILE_SIZE / 2.0;
            } else if (pos.x + TILE_SIZE / 2.0 >= mapWidth) {
                pos.x = -TILE_SIZE / 2.0;
            }
            if (pos.y + TILE_SIZE / 2.0 <= 0) {
                pos.y = mapHeight - TILE_SIZE / 2.0;
            } else if (pos.y + TILE_SIZE / 2.0 >= mapHeight) {
                pos.y = -TILE_SIZE / 2.0;
            }

            double margin = 0.1;
            if (isWall(tiles, pos.x + margin, pos.y + margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + margin) ||
                isWall(tiles, pos.x + margin, pos.y + TILE_SIZE - margin) ||
                isWall(tiles, pos.x + TILE_SIZE - margin, pos.y + TILE_SIZE - margin)) {

                Pair<Integer, Integer> currentGridPos = pos.ToGridPosition();
                int targetGridX = currentGridPos.getKey();
                int targetGridY = currentGridPos.getValue();

                while (true) {
                    if (targetGridX >= 0 && targetGridX < tiles.length &&
                        targetGridY >= 0 && targetGridY < tiles[0].length &&
                        tiles[targetGridX][targetGridY] != TileType.WALL) {
                        break;
                    }
                    targetGridX -= dx;
                    targetGridY -= dy;
                }

                pos.x = targetGridX * TILE_SIZE;
                pos.y = targetGridY * TILE_SIZE;
            }

            ghost.setPosition(pos);
        }
    }

    private boolean collision(Position pos1, Position pos2) {
        return pos1.x < pos2.x + TILE_SIZE &&
            pos1.x + TILE_SIZE > pos2.x &&
            pos1.y < pos2.y + TILE_SIZE &&
            pos1.y + TILE_SIZE > pos2.y;
    }

    private void handleGhostPlayerCollisions(GameState gameState) {
        if (gameState.players() == null || gameState.ghosts() == null) {
            return;
        }

        boolean frightened = Ghost.getFrightenedTimerSec() > 0.0;

        for (Player player : gameState.players()) {
            if (player == null || player.getPosition() == null) {
                continue;
            }
            if (!player.isAlive()) {
                continue;
            }
            if (player.getRespawnTimer() > 0.0) {
                continue;
            }

            for (Ghost ghost : gameState.ghosts()) {
                if (ghost == null || ghost.getPosition() == null) {
                    continue;
                }
                if (ghost.getRespawnTimer() > 0.0) {
                    continue;
                }

                if (!collision(player.getPosition(), ghost.getPosition())) {
                    continue;
                }

                if (frightened) {
                    // Player eats ghost (only this ghost is affected)
                    player.addPoints(200);
                    ghost.setRespawnTimer(GHOST_RESPAWN_DELAY_SEC);
                    ghost.setPosition(new Position(-1000, -1000));
                    return;
                } else {
                    // Ghost kills this player (only this player is affected)
                    int livesLeft = Math.max(0, player.getLives() - 1);
                    player.setLives(livesLeft);

                    if (livesLeft <= 0) {
                        // Dead forever (no respawn)
                        player.setAlive(false);
                        player.setIntendedDirection(null);
                        player.setPosition(new Position(-1000, -1000));
                        return;
                    }

                    // Start respawn timer for THIS player only
                    player.setAlive(false);
                    player.setRespawnTimer(PLAYER_RESPAWN_DELAY_SEC);
                    player.setPosition(new Position(-1000, -1000)); // hide during timer
                    player.setIntendedDirection(null);
                    return;
                }
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
    double dt = 1.0 / TARGET_FPS;

    for (Player p : gameState.players()) {
        if (p == null) continue;

        if (p.getPowerUpTimer() > 0.0) {
            p.setPowerUpTimer(Math.max(0.0, p.getPowerUpTimer() - dt));
        }
    }
}

private boolean isPowered(Player p) {
    return p != null && p.getPowerUpTimer() > 0.0;
}

private void handlePvPcollitions(GameState gameState) {
    List<Player> players = gameState.players();
    if (players == null || players.size() < 2) return;

    for (int i = 0; i < players.size(); i++) {
        Player a = players.get(i);
        if (!isPlayerCollidable(a)) continue;

        for (int j = i + 1; j < players.size(); j++) {
            Player b = players.get(j);
            if (!isPlayerCollidable(b)) continue;

            if (!collision(a.getPosition(), b.getPosition())) continue;

            boolean aPow = isPowered(a);
            boolean bPow = isPowered(b);

            if (aPow ^ bPow) {
                Player eater = aPow ? a : b;
                Player victim = aPow ? b : a;

                eatPlayer(gameState, eater, victim);
                return;
            }
            resolvePlayerOverlap(a, b);
        }
    }
}

private boolean isPlayerCollidable(Player p) {
    return p != null
        && p.getPosition() != null
        && p.isAlive()
        && p.getRespawnTimer() <= 0.0;
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
        if (pa.x < pb.x) { pa.x -= push; pb.x += push; }
        else            { pa.x += push; pb.x -= push; }
    } else {
        double push = overlapY / 2.0;
        if (pa.y < pb.y) { pa.y -= push; pb.y += push; }
        else            { pa.y += push; pb.y -= push; }
    }

    a.setPosition(pa);
    b.setPosition(pb);
}

}
