package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.common.BaseTest;
import com.example.model.Action;
import com.example.model.ActionList;
import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.GhostType;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Client Game Controller Logic and State Management Tests")
public class ClientGameControllerTest extends BaseTest {

    private ClientGameController controller;
    private GameState initialState;

    @Override
    protected long getTimeoutSeconds() {
        return 2;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 200;
    }

    @BeforeEach
    public void setUp() {
        controller = new ClientGameController();

        Ghost.setFrightenedTimerSec(0.0);
        Ghost.setGhostChaseTimer(0.0);
        Ghost.setGhostScatterMode(true);
        Constants.clock = 0;

        initialState = controller.initializeGameState(1);

    }

    @Test
    @DisplayName("Initial game state should correctly place players and ghosts at their spawn positions")
    public void testInitializeGameState() {
        GameState state = controller.initializeGameState(1);
        assertNotNull(state);
        assertEquals(1, state.players().size());
        assertEquals(5, state.ghosts().size());

        Player player = state.players().getFirst();
        assertNotNull(player.getSpawnPosition(), "Player should have a spawn position");
        assertEquals(player.getSpawnPosition().x, player.getPosition().x, 0.01, "Player should start at spawn X");
        assertEquals(player.getSpawnPosition().y, player.getPosition().y, 0.01, "Player should start at spawn Y");

        for (Ghost ghost : state.ghosts()) {
            assertNotNull(ghost.getSpawnPosition(), "Ghost should have a spawn position");
            assertEquals(ghost.getSpawnPosition().x, ghost.getPosition().x, 0.01, "Ghost should start at spawn X");
            assertEquals(ghost.getSpawnPosition().y, ghost.getPosition().y, 0.01, "Ghost should start at spawn Y");
        }
    }

    @Test
    @DisplayName("Player should move freely in an empty corridor")
    public void testPlayerMovementFree() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        player.setDirection(Direction.EAST);
        player.setAlive(true);
        player.setRespawnTimer(0.0);

        double startX = player.getPosition().x;
        controller.updateGameState(initialState, new ArrayList<>());
        double endX = player.getPosition().x;

        assertTrue(endX > startX, "Player should have moved East");
        assertEquals(startX + (Constants.PLAYER_SPEED / Constants.TARGET_FPS), endX, 0.001);
    }

    @Test
    @DisplayName("Player should be stopped by wall collisions")
    public void testPlayerWallCollision() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();

        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());
        Position pos = player.getPosition();

        assertEquals(TILE_SIZE, pos.x, 0.1, "Player should be stopped by wall at x=" + TILE_SIZE);
    }

    @Test
    @DisplayName("Player should ignore turn requests into walls and continue forward")
    public void testPlayerCannotTurnIntoWall() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();

        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);
        player.setIntendedDirection(Direction.NORTH);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(Direction.EAST, player.getDirection(), "Player should ignore invalid turn and keep facing East");
        assertTrue(player.getPosition().x > TILE_SIZE, "Player should keep moving East");
    }

    @Test
    @DisplayName("Player should wrap around to the opposite side of the map when exiting boundaries")
    public void testMapWrapAround() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();
        double mapWidth = tiles.length * TILE_SIZE;

        player.setPosition(new Position(-20, 8 * TILE_SIZE));
        player.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());
        Position pos = player.getPosition();

        assertTrue(pos.x > mapWidth / 2, "Player should wrap to the right side, x=" + pos.x);
    }

    @Test
    @DisplayName("Player should consume pellets and gain points")
    public void testPelletConsumption() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.PAC_DOT;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        int initialPoints = player.getPoints();

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(player.getPoints() > initialPoints, "Points should increase");
        assertEquals(TileType.EMPTY, tiles[3][3], "Tile should become EMPTY");
    }

    @Test
    @DisplayName("Consuming an energizer should trigger frightened mode for ghosts")
    public void testPowerPelletConsumption() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.ENERGIZER;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(Ghost.getFrightenedTimerSec() > 0, "Frightened timer should be active");
        assertEquals(TileType.EMPTY, tiles[3][3], "Tile should become EMPTY");
    }

    @Test
    @DisplayName("Consuming another energizer while already powered up should reset the duration")
    public void testPowerUpExtension() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.ENERGIZER;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        Ghost.setFrightenedTimerSec(Constants.FRIGHTENED_DURATION_SEC - 2.0);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(Constants.FRIGHTENED_DURATION_SEC,
            Ghost.getFrightenedTimerSec(), 0.1, "Frightened timer should be reset to full duration");
    }

    @Test
    @DisplayName("Game should declare a winner when all pellets are consumed")
    public void testWinCondition() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                if (tiles[y][x] == TileType.PAC_DOT || tiles[y][x] == TileType.ENERGIZER) {
                    tiles[y][x] = TileType.EMPTY;
                }
            }
        }

        tiles[5][5] = TileType.PAC_DOT;
        player.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull(nextState.winner(), "Winner should be declared when all dots are eaten");
        assertEquals(0, nextState.winner().getId(), "Player 0 should be the winner");
    }

    @Test
    @DisplayName("Fruit should spawn after a certain number of dots are consumed")
    public void testFruitSpawning() {

        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        boolean foundFruit = false;
        for (TileType[] row : tiles) {
            for (int x = 0; x < tiles[0].length; x++) {
                if (row[x] == TileType.CHERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }
        assertFalse(foundFruit, "Fruit (Cherry) should NOT be on the map initially");

        int dotsEaten = 0;
        for (int y = 0; y < tiles.length && dotsEaten < 70; y++) {
            for (int x = 0; x < tiles[0].length && dotsEaten < 70; x++) {
                if (tiles[y][x] == TileType.PAC_DOT) {
                    tiles[y][x] = TileType.EMPTY;
                    player.addPoints(10);
                    dotsEaten++;
                }
            }
        }

        controller.updateGameState(initialState, new ArrayList<>());

        for (TileType[] row : tiles) {
            for (int x = 0; x < tiles[0].length; x++) {
                if (row[x] == TileType.CHERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }
        assertTrue(foundFruit, "Fruit (Cherry) should spawn after eating dots");
    }

    @Test
    @DisplayName("Map and players should reset correctly upon winning a level")
    public void testMapResetOnWin() {

        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                tiles[y][x] = TileType.EMPTY;
            }
        }

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        boolean foundPellet = false;
        for (TileType[] row : nextState.tiles()) {
            for (TileType tile : row) {
                if (tile == TileType.PAC_DOT) {
                    foundPellet = true;
                    break;
                }
            }
        }
        assertTrue(foundPellet, "Map should be refilled with pellets after winning");

        Player nextPlayer = nextState.players().getFirst();
        assertEquals(player.getSpawnPosition().x,
            nextPlayer.getPosition().x, 0.1, "Player should be reset to spawn position on map reset");
        assertEquals(player.getSpawnPosition().y,
            nextPlayer.getPosition().y, 0.1, "Player should be reset to spawn position on map reset");
    }

    @Test
    @DisplayName("Ghost speed should increase when progressing to higher levels")
    public void testGhostSpeedIncreaseOnLevelUp() {

        double initialSpeed = Ghost.getGHOSTSPEED();

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                tiles[y][x] = TileType.EMPTY;
            }
        }

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(Ghost.getGHOSTSPEED() > initialSpeed, "Ghost speed should increase on level up");
    }

    @Test
    @DisplayName("Game should advance to the next level when all pellets are cleared")
    public void testLevelProgression() {

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                tiles[y][x] = TileType.EMPTY;
            }
        }

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull(nextState.winner(), "Winner should be set");
        
        boolean foundPellet = false;
        for (TileType[] row : nextState.tiles()) {
            for (TileType tile : row) {
                if (tile == TileType.PAC_DOT || tile == TileType.ENERGIZER) {
                    foundPellet = true;
                    break;
                }
            }
        }
        assertTrue(foundPellet, "Map should be refilled with pellets on level progression");
        assertEquals(1, nextState.clock(), "Clock should reset on new level");
    }

    @Test
    @DisplayName("Collision with a normal ghost should result in losing a life")
    public void testGhostPlayerCollisionNormal() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setAlive(true);
        player.setRespawnTimer(0.0);
        player.setLives(3);

        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setRespawnTimer(0.0);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(2, player.getLives(), "Player should lose a life");
        assertFalse(player.isAlive(), "Player should be waiting for respawn");
    }

    @Test
    @DisplayName("Simultaneous collisions with multiple ghosts should only consume one life")
    public void testSimultaneousGhostCollision() {
        initialState.ghosts().clear();
        Ghost g1 = new Ghost(GhostType.RED);
        Ghost g2 = new Ghost(GhostType.PINK);
        initialState.ghosts().add(g1);
        initialState.ghosts().add(g2);

        Player player = initialState.players().getFirst();
        player.setLives(3);
        player.setAlive(true);
        player.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));

        g1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        g2.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(2, player.getLives(), "Player should lose only 1 life for simultaneous collision");
        assertFalse(player.isAlive(), "Player should be waiting for respawn");
    }

    @Test
    @DisplayName("Collision with a frightened ghost should eat the ghost instead of killing the player")
    public void testGhostPlayerCollisionFrightened() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setAlive(true);
        player.setRespawnTimer(0.0);
        player.setLives(3);
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setRespawnTimer(0.0);

        Ghost.setFrightenedTimerSec(10.0);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(3, player.getLives(), "Player should NOT lose a life");
        assertTrue(ghost.getRespawnTimer() > 0, "Ghost should be on respawn timer");
    }

    @Test
    @DisplayName("Player should be immune to ghost collisions while respawning")
    public void testRespawnImmunity() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setLives(3);
        player.setAlive(false);
        player.setRespawnTimer(2.0);
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(3, player.getLives(), "Player lives should not decrease while respawning");
        assertTrue(player.getRespawnTimer() > 0.0, "Respawn timer should still be active");
    }

    @Test
    @DisplayName("Player should lose a life if respawning directly on top of a ghost")
    public void testRespawnOnGhost() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setLives(3);
        player.setAlive(false);
        player.setRespawnTimer(0.01);

        Position spawnPos = new Position(3 * TILE_SIZE, 3 * TILE_SIZE);
        player.setSpawnPosition(spawnPos);

        ghost.setPosition(new Position(spawnPos.x, spawnPos.y));

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(2, player.getLives(), "Player should lose a life immediately upon unsafe respawn");
        assertFalse(player.isAlive(), "Player should be dead again");
    }

    @Test
    @DisplayName("Ghosts should be able to pass through each other")
    public void testGhostPassThroughGhost() {
        initialState.ghosts().clear();
        Ghost g1 = new Ghost(GhostType.RED);
        Ghost g2 = new Ghost(GhostType.PINK);

        g1.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        g1.setDirection(Direction.EAST);

        g2.setPosition(new Position(4 * TILE_SIZE, 3 * TILE_SIZE));
        g2.setDirection(Direction.WEST);

        initialState.ghosts().add(g1);
        initialState.ghosts().add(g2);

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(g1.getPosition().x > 3 * TILE_SIZE, "Ghost 1 should move East");
        assertTrue(g2.getPosition().x < 4 * TILE_SIZE, "Ghost 2 should move West");
    }

    @Test
    @DisplayName("Ghosts should switch between scatter and chase modes based on timers")
    public void testScatterChaseModeSwitch() {
        Ghost.setGhostScatterMode(true);
        Ghost.setGhostChaseTimer(6.99);

        controller.updateGameState(initialState, new ArrayList<>());

        assertFalse(Ghost.isGhostScatterMode(), "Should have switched to Chase mode");
        assertEquals(0.0, Ghost.getGhostChaseTimer(), 0.1, "Timer should reset");
    }

    @Test
    @DisplayName("Game should end when player lives reach zero")
    public void testGameOver() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setAlive(true);
        player.setRespawnTimer(0.0);
        player.setLives(1);
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setRespawnTimer(0.0);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals(0, player.getLives(), "Lives should be 0");
        assertFalse(player.isAlive(), "Player should be dead");
    }

    @Test
    @DisplayName("Ghost should reappear at its spawn position after being eaten")
    public void testGhostRespawnAtSpawnPosition() {
        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        Position spawnPos = new Position(5 * TILE_SIZE, 5 * TILE_SIZE);
        ghost.setSpawnPosition(spawnPos);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        ghost.setPosition(new Position(TILE_SIZE, TILE_SIZE));

        Ghost.setFrightenedTimerSec(10.0);
        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(ghost.getRespawnTimer() > 0, "Ghost should be on respawn timer");
        assertEquals(-1000, ghost.getPosition().x, 0.1, "Ghost should be hidden");

        ghost.setRespawnTimer(0.001);

        double originalSpeed = Ghost.getGHOSTSPEED();
        Ghost.setGHOSTSPEED(0.0);
        try {
            controller.updateGameState(initialState, new ArrayList<>());
        } finally {
            Ghost.setGHOSTSPEED(originalSpeed);
        }

        assertEquals(spawnPos.x, ghost.getPosition().x, 0.1, "Ghost should be at spawn position after respawn");
        assertEquals(spawnPos.y, ghost.getPosition().y, 0.1, "Ghost should be at spawn position after respawn");
        assertEquals(0.0, ghost.getRespawnTimer(), 0.001, "Respawn timer should be 0");
    }

    @Test
    @DisplayName("Game engine should not modify the existing state objects in place (Immutability check)")
    public void testStateImmutability() {

        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        ghost.setPosition(new Position(100, 100));
        initialState.ghosts().add(ghost);

        double startX = ghost.getPosition().x;

        controller.updateGameState(initialState, new ArrayList<>());

        assertNotEquals(startX,
            ghost.getPosition().x, 0.001, "Warning: State is mutable. Ghost object was updated in place.");
    }

    @Test
    @DisplayName("Ghost collision should take precedence over consuming an energizer in the same tick")
    public void testSimultaneousEnergizerAndGhostCollision() {

        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        initialState.ghosts().add(ghost);

        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[2][1] = TileType.ENERGIZER;

        player.setPosition(new Position(1.5 * TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);

        ghost.setPosition(new Position(2 * TILE_SIZE, TILE_SIZE));
        ghost.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());

        assertFalse(player.isAlive(), "Player should be dead (Collision priority over Powerup)");
        assertEquals(TileType.ENERGIZER,
            tiles[2][1], "Energizer should theoretically remain if player died before eating");
    }

    @Test
    @DisplayName("Player should be able to reverse direction 180 degrees instantly")
    public void testPlayerReversingDirection() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);

        controller.updateGameState(initialState, new ArrayList<>());
        double x1 = player.getPosition().x;
        assertTrue(x1 > TILE_SIZE);

        ArrayList<Action> actions = new ArrayList<>();
        actions.add(new Action(0, 0, 1));

        controller.updateGameState(initialState, actions);

        assertEquals(Direction.WEST, player.getDirection(), "Player should face West immediately");
        assertTrue(player.getPosition().x < x1, "Player should move West");
    }

    @Test
    @DisplayName("Game controller should handle null or empty action lists without crashing")
    public void testNullActionHandling() {

        GameState state = controller.updateGameState(initialState, null);
        assertNotNull(state);

        List<Action> listWithNulls = new ArrayList<>();
        listWithNulls.add(null);
        listWithNulls.add(new Action(0, 0, 1));

        state = controller.updateGameState(state, listWithNulls);
        assertNotNull(state);
    }

    @Test
    @DisplayName("Clock should handle potential integer overflow during state updates")
    public void testIntegerOverflowClock() {

        GameState highClockState = new GameState(
            Integer.MAX_VALUE - 5,
            initialState.players(),
            initialState.ghosts(),
            initialState.tiles(),
            null
        );

        GameState nextState = controller.updateGameState(highClockState, new ArrayList<>());

        assertTrue(nextState.clock() != 0, "Clock should handle potential high values gracefully");
    }

    @Test
    @DisplayName("Winner should be declared correctly based on map state")
    public void testWinConditionLogic() {

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                tiles[y][x] = TileType.EMPTY;
            }
        }

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull(nextState.winner(), "Winner should be declared when map is empty");
    }

    @Test
    @DisplayName("Fruits should spawn correctly when pellet count reaches specific thresholds")
    public void testFruitSpawnLogic() {

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();

        int pellets = 0;
        for (TileType[] row : tiles) {
            for (TileType t : row) {
                if (t == TileType.PAC_DOT) {
                    pellets++;
                }
            }
        }

        int threshold = 70;
        Player p = initialState.players().getFirst();
        p.addPoints(threshold * 10);

        int removed = 0;
        for (int y = 0; y < tiles.length && removed < threshold; y++) {
            for (int x = 0; x < tiles[0].length && removed < threshold; x++) {
                if (tiles[y][x] == TileType.PAC_DOT) {
                    tiles[y][x] = TileType.EMPTY;
                    removed++;
                }
            }
        }

        controller.updateGameState(initialState, new ArrayList<>());

        int remainingPellets = 0;
        for (TileType[] row : initialState.tiles()) {
            for (TileType t : row) {
                if (t == TileType.PAC_DOT) {
                    remainingPellets++;
                }
            }
        }
        assertEquals(pellets - removed, remainingPellets, "Pellets should be removed from map");

        boolean foundFruit = false;
        for (TileType[] row : initialState.tiles()) {
            for (TileType t : row) {
                if (t == TileType.CHERRY || t == TileType.STRAWBERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }

        assertTrue(foundFruit, "Fruit should spawn after eating pellets");
    }

    @Test
    @DisplayName("Input buffering should maintain intended direction until the player reaches a tile center")
    public void testInputBufferingPrecision() {

        Player p = initialState.players().getFirst();
        double startX = TILE_SIZE - 1.0;
        p.setPosition(new Position(startX, TILE_SIZE));
        p.setDirection(Direction.EAST);
        p.setIntendedDirection(Direction.SOUTH);

        controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull(p.getIntendedDirection(), "Player should maintain an intended direction until turn is executed");
        assertEquals(Direction.EAST, p.getDirection(), "Player should still be facing East if not yet at center of tile");
        assertTrue(p.getPosition().x > startX, "Player should have moved forward East");
        assertEquals(TILE_SIZE, p.getPosition().y, 0.001, "Player Y should not have changed yet");
    }

    @Test
    @DisplayName("Player should correctly teleport across boundary tunnels")
    public void testTeleportationBoundary() {

        Player p = initialState.players().getFirst();
        double mapWidth = initialState.tiles().length * TILE_SIZE;
        double moveAmount = Constants.PLAYER_SPEED / Constants.TARGET_FPS;

        // Position such that one move puts us 0.1 past the map width
        p.setPosition(new Position(mapWidth - moveAmount + 0.1, TILE_SIZE));
        p.setDirection(Direction.EAST);

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue(p.getPosition().x < TILE_SIZE, "Player should have teleported to left");
        assertEquals(0.1, p.getPosition().x, 0.5, "Player should be at the far left after wrapping");
    }

    @Test
    @DisplayName("Resimulation should correctly update player position for a given clock target")
    public void testUpdateGameStateForBasicMovement() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);
        
        // At clock 1, move EAST
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0)); // 2 = EAST
        
        GameState finalState = controller.updateGameStateFor(initialState, 5);
        
        assertEquals(5, finalState.clock());
        assertTrue(finalState.players().getFirst().getPosition().x > TILE_SIZE, "Player should have moved EAST from resimulation");
        double expectedX = TILE_SIZE + 4 * (Constants.PLAYER_SPEED / Constants.TARGET_FPS);
        assertEquals(expectedX, finalState.players().getFirst().getPosition().x, 0.001, "Player should be at expected X after 4 ticks of movement");
    }

    @Test
    @DisplayName("Resimulation should handle multiple sequential actions correctly")
    public void testUpdateGameStateForMultipleActions() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);

        // Clock 1: EAST (already moving East)
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));
        // Clock 3: SOUTH (at TILE_SIZE, TILE_SIZE it should be able to turn if it's an intersection, 
        // but let's assume it's moving and we want to see it change direction)
        // We'll place it at an intersection for easier testing
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        Constants.cleanActions.addAction(new Action(0, 3, 4, 1)); // 4 = SOUTH

        GameState finalState = controller.updateGameStateFor(initialState, 6);

        assertEquals(6, finalState.clock());
        Player finalPlayer = finalState.players().getFirst();
        assertEquals(Direction.SOUTH, finalPlayer.getDirection(), "Player should be facing SOUTH after resimulation");
        assertTrue(finalPlayer.getPosition().y > 3 * TILE_SIZE, "Player should have moved SOUTH");
    }

    @Test
    @DisplayName("Resimulation should correctly detect ghost collisions during the resimulated window")
    public void testUpdateGameStateForGhostCollision() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        player.setLives(3);

        Ghost ghost = initialState.ghosts().getFirst();
        ghost.setRespawnTimer(0.0);
        ghost.setPosition(new Position(5 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setDirection(Direction.WEST);
        
        // Player moves EAST towards ghost
        player.setDirection(Direction.EAST);
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));

        // Resimulate long enough for them to collide
        GameState finalState = controller.updateGameStateFor(initialState, 20);

        assertTrue(finalState.players().getFirst().getLives() < 3, "Player should have lost a life during resimulation");
        assertFalse(finalState.players().getFirst().isAlive(), "Player should be dead or respawning");
    }

    @Test
    @DisplayName("Resimulation should correctly process energizer effects and point gains")
    public void testUpdateGameStateForEnergizerEffect() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(2 * TILE_SIZE, 3 * TILE_SIZE));
        player.setDirection(Direction.EAST);

        TileType[][] tiles = initialState.tiles();
        tiles[3][3] = TileType.ENERGIZER;
        
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));

        // Resimulate over eating the energizer
        GameState finalState = controller.updateGameStateFor(initialState, 15);

        assertNotNull(finalState, "Final state should not be null");
        assertEquals(TileType.EMPTY, finalState.tiles()[3][3], "Energizer should be eaten in final state");
        assertTrue(Ghost.getFrightenedTimerSec() > 0, "Ghosts should be frightened after eating energizer in resimulation");
        assertTrue(finalState.players().getFirst().getPoints() > 0, "Player should have more points in final state");
    }

    @Test
    @DisplayName("Resimulation engine should not modify the initial state provided (Immutability check)")
    public void testUpdateGameStateForImmutability() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        int initialClock = initialState.clock();
        Position initialPos = new Position(initialState.players().getFirst().getPosition().x, initialState.players().getFirst().getPosition().y);

        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));
        
        GameState finalState = controller.updateGameStateFor(initialState, 10);

        assertNotEquals(initialClock, finalState.clock(), "Final state should be different from initial state");
        assertEquals(initialClock, initialState.clock(), "Initial state clock should NOT have changed");
        assertEquals(initialPos.x, initialState.players().getFirst().getPosition().x, 0.001, "Initial state position should NOT have changed");
    }

    @Test
    @DisplayName("Iterative updates and batch resimulation should produce consistent results")
    public void testUpdateGameStateForConsistency() {
        Constants.cleanActions = new ActionList();
        GameState state1 = controller.initializeGameState(1);
        GameState state2 = controller.initializeGameState(1);
        
        // Manually update state1 5 times with no actions
        for(int i = 1; i <= 5; i++) {
            state1 = controller.updateGameState(state1, new ArrayList<>());
        }
        
        // Resimulate state2 for 5 ticks
        state2 = controller.updateGameStateFor(state2, 5);
        
        assertEquals(state1.clock(), state2.clock(), "States should have same clock");
        assertEquals(state1.players().getFirst().getPosition().x, state2.players().getFirst().getPosition().x, 0.001, "Player X should be same");
        assertEquals(state1.players().getFirst().getPosition().y, state2.players().getFirst().getPosition().y, 0.001, "Player Y should be same");
    }

    @Test
    @DisplayName("Resimulation should handle large clock jumps efficiently")
    public void testUpdateGameStateForLargeClockJump() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        
        // Resimulate 100 ticks
        GameState finalState = controller.updateGameStateFor(initialState, 100);
        
        assertEquals(100, finalState.clock());
    }

    @Test
    @DisplayName("Resimulation should not allow the game clock to regress")
    public void testUpdateGameStateForClockRegression() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        GameState futureState = new GameState(10, initialState.players(), initialState.ghosts(), initialState.tiles(), null);
        
        // targetClock (5) < futureState.clock() (10)
        GameState resultState = controller.updateGameStateFor(futureState, 5);
        
        assertEquals(10, resultState.clock(), "Clock should not regress and state should remain unchanged");
    }

    @Test
    @DisplayName("Resimulation should ignore actions that occur after the target clock")
    public void testUpdateGameStateForFutureActionsIgnored() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);

        // Action at clock 10 (beyond target clock 5)
        Constants.cleanActions.addAction(new Action(0, 10, 4, 0)); // 4 = SOUTH
        
        GameState finalState = controller.updateGameStateFor(initialState, 5);
        
        assertEquals(5, finalState.clock());
        assertEquals(Direction.EAST, finalState.players().getFirst().getDirection(), "Direction should still be EAST");
    }

    @Test
    @DisplayName("Resimulation should handle overlapping actions from multiple players correctly")
    public void testUpdateGameStateForMultiplePlayersOverlappingActions() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(2);
        Player p0 = initialState.players().getFirst();
        Player p1 = initialState.players().get(1);
        
        p0.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        p1.setPosition(new Position(10 * TILE_SIZE, 3 * TILE_SIZE));
        
        // Both move at clock 1
        Constants.cleanActions.addAction(new Action(0, 1, 1, 0)); // P0 WEST
        Constants.cleanActions.addAction(new Action(1, 1, 2, 1)); // P1 EAST
        
        GameState finalState = controller.updateGameStateFor(initialState, 5);
        
        assertTrue(finalState.players().getFirst().getPosition().x < 3 * TILE_SIZE, "P0 should have moved WEST");
        assertTrue(finalState.players().get(1).getPosition().x > 10 * TILE_SIZE, "P1 should have moved EAST");
    }

    @Test
    @DisplayName("Resimulation engine should detect if intermediate actions were missed")
    public void testUpdateGameStateForMissedActionDetection() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        
        // Add action with index 0 at clock 1
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));
        // Add action with index 5 at clock 2 (indices 1, 2, 3, 4 are missing)
        Constants.cleanActions.addAction(new Action(0, 2, 2, 5));
        
        Constants.cleanActions.fixedMissedAction();
        assertFalse(Constants.cleanActions.missedAction());
        
        controller.updateGameStateFor(initialState, 3);
        
        assertTrue(Constants.cleanActions.missedAction(), "Missed action should be detected during resimulation");
    }

    @Test
    @DisplayName("Frightened mode should expire correctly during resimulation")
    public void testUpdateGameStateForEnergizerExpiration() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        
        // Set frightened timer to very low value (e.g., 2 frames worth)
        Ghost.setFrightenedTimerSec(2.0 / Constants.TARGET_FPS);
        assertTrue(Ghost.getFrightenedTimerSec() > 0);
        
        // Resimulate 5 ticks
        GameState finalState = controller.updateGameStateFor(initialState, 5);
        
        assertNotNull(finalState, "Final state should not be null");
        assertEquals(5, finalState.clock(), "Clock should be at target clock");
        assertEquals(0.0, Ghost.getFrightenedTimerSec(), 0.001, "Frightened timer should have expired in the final state context");
    }

    @Test
    @DisplayName("Multi-client state consistency should be maintained regardless of processing batch size")
    public void testMultiClientResyncConsistency() {
        // Shared sequence of actions for 2 players
        List<Action> sharedActionsList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            sharedActionsList.add(new Action(0, i, 2, (i - 1) * 2));     // P0 moves EAST
            sharedActionsList.add(new Action(1, i, 4, (i - 1) * 2 + 1)); // P1 moves SOUTH
        }

        // --- "Client 1" View: Processes all 20 ticks at once ---
        Constants.cleanActions = new ActionList();
        sharedActionsList.forEach(Constants.cleanActions::addAction);
        GameState stateClient1 = controller.initializeGameState(2);
        stateClient1 = controller.updateGameStateFor(stateClient1, 20);

        // --- "Client 2" View: Processes in chunks (simulating lag/bursts) ---
        // Reset static state for fresh simulation
        Ghost.setFrightenedTimerSec(0.0);
        Ghost.setGhostChaseTimer(0.0);
        Ghost.setGhostScatterMode(true);
        
        Constants.cleanActions = new ActionList();
        sharedActionsList.forEach(Constants.cleanActions::addAction);
        GameState stateClient2 = controller.initializeGameState(2);
        
        stateClient2 = controller.updateGameStateFor(stateClient2, 5);
        stateClient2 = controller.updateGameStateFor(stateClient2, 15);
        stateClient2 = controller.updateGameStateFor(stateClient2, 20);

        // --- Assertions: Both clients should have identical positions for all entities ---
        assertEquals(stateClient1.clock(), stateClient2.clock(), "Clocks should match");
        
        for (int i = 0; i < 2; i++) {
            Player p1 = stateClient1.players().get(i);
            Player p2 = stateClient2.players().get(i);
            assertEquals(p1.getPosition().x, p2.getPosition().x, 0.001, "Player " + i + " X should match");
            assertEquals(p1.getPosition().y, p2.getPosition().y, 0.001, "Player " + i + " Y should match");
            assertEquals(p1.getDirection(), p2.getDirection(), "Player " + i + " direction should match");
        }

        for (int i = 0; i < stateClient1.ghosts().size(); i++) {
            Ghost g1 = stateClient1.ghosts().get(i);
            Ghost g2 = stateClient2.ghosts().get(i);
            assertEquals(g1.getPosition().x, g2.getPosition().x, 0.001, "Ghost " + i + " X should match");
            assertEquals(g1.getPosition().y, g2.getPosition().y, 0.001, "Ghost " + i + " Y should match");
        }
    }

    @Test
    @DisplayName("Late action discovery should correctly trigger a rollback and resimulation to ensure state consistency")
    public void testResyncAfterRollback() {
        // Scenario: Client is at clock 15, then discovers an action from another player happened at clock 10.
        // It must resimulate from the last known good state (clock 9) up to 15.
        
        Constants.cleanActions = new ActionList();
        // Initial actions (only P0 moves)
        for (int i = 1; i <= 15; i++) {
            Constants.cleanActions.addAction(new Action(0, i, 2, i)); // P0 moves EAST
        }
        
        GameState stateAtClock9 = controller.initializeGameState(2);
        stateAtClock9 = controller.updateGameStateFor(stateAtClock9, 9);
        
        // Client mistakenly proceeds to 15 without knowing about P1's action at 10
        GameState stateWrong = controller.updateGameStateFor(stateAtClock9, 15);
        
        // Now "discover" the late action for P1 at clock 10
        Constants.cleanActions.addAction(new Action(1, 10, 4, 100)); // P1 moves SOUTH at clock 10
        
        // Resync from the last good state (9) to 15
        GameState stateCorrected = controller.updateGameStateFor(stateAtClock9, 15);
        
        // Assertions
        assertNotEquals(stateWrong.players().get(1).getPosition().y, 
            stateCorrected.players().get(1).getPosition().y, 0.001, "P1 position should be different after corrected resimulation");
        
        assertTrue(stateCorrected.players().get(1).getPosition().y > stateWrong.players().get(1).getPosition().y, "P1 should have moved SOUTH in the corrected state");
            
        assertEquals(stateWrong.players().get(0).getPosition().x, 
            stateCorrected.players().get(0).getPosition().x, 0.001, "P0 position should still be consistent (independent movement)");
    }
}
