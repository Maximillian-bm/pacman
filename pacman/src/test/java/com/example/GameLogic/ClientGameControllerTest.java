package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        controller = new ClientGameController();

        Ghost.setFrightenedTimerSec(0.0);
        Ghost.setGhostChaseTimer(0.0);
        Ghost.setGhostScatterMode(true);
        Constants.clock = 0;

        initialState = controller.initializeGameState(1);

    }

    @Test
    public void testInitializeGameState() {
        GameState state = controller.initializeGameState(1);
        assertNotNull(state);
        assertEquals(1, state.players().size());
        assertEquals(5, state.ghosts().size());

        Player player = state.players().getFirst();
        assertNotNull("Player should have a spawn position", player.getSpawnPosition());
        assertEquals("Player should start at spawn X", player.getSpawnPosition().x, player.getPosition().x, 0.01);
        assertEquals("Player should start at spawn Y", player.getSpawnPosition().y, player.getPosition().y, 0.01);

        for (Ghost ghost : state.ghosts()) {
            assertNotNull("Ghost should have a spawn position", ghost.getSpawnPosition());
            assertEquals("Ghost should start at spawn X", ghost.getSpawnPosition().x, ghost.getPosition().x, 0.01);
            assertEquals("Ghost should start at spawn Y", ghost.getSpawnPosition().y, ghost.getPosition().y, 0.01);
        }
    }

    @Test
    public void testPlayerMovementFree() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        player.setDirection(Direction.EAST);
        player.setAlive(true);
        player.setRespawnTimer(0.0);

        double startX = player.getPosition().x;
        controller.updateGameState(initialState, new ArrayList<>());
        double endX = player.getPosition().x;

        assertTrue("Player should have moved East", endX > startX);
        assertEquals(startX + (Constants.PLAYER_SPEED / Constants.TARGET_FPS), endX, 0.001);
    }

    @Test
    public void testPlayerWallCollision() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();

        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());
        Position pos = player.getPosition();

        assertEquals("Player should be stopped by wall at x=" + TILE_SIZE, TILE_SIZE, pos.x, 0.1);
    }

    @Test
    public void testPlayerCannotTurnIntoWall() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();

        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);
        player.setIntendedDirection(Direction.NORTH);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals("Player should ignore invalid turn and keep facing East", Direction.EAST,
            player.getDirection());
        assertTrue("Player should keep moving East", player.getPosition().x > TILE_SIZE);
    }

    @Test
    public void testMapWrapAround() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();
        double mapWidth = tiles.length * TILE_SIZE;

        player.setPosition(new Position(-20, 8 * TILE_SIZE));
        player.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());
        Position pos = player.getPosition();

        assertTrue("Player should wrap to the right side, x=" + pos.x, pos.x > mapWidth / 2);
    }

    @Test
    public void testPelletConsumption() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.PAC_DOT;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        int initialPoints = player.getPoints();

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue("Points should increase", player.getPoints() > initialPoints);
        assertEquals("Tile should become EMPTY", TileType.EMPTY, tiles[3][3]);
    }

    @Test
    public void testPowerPelletConsumption() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.ENERGIZER;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue("Frightened timer should be active", Ghost.getFrightenedTimerSec() > 0);
        assertEquals("Tile should become EMPTY", TileType.EMPTY, tiles[3][3]);
    }

    @Test
    public void testPowerUpExtension() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        tiles[3][3] = TileType.ENERGIZER;
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        Ghost.setFrightenedTimerSec(Constants.FRIGHTENED_DURATION_SEC - 2.0);

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals("Frightened timer should be reset to full duration", Constants.FRIGHTENED_DURATION_SEC,
            Ghost.getFrightenedTimerSec(), 0.1);
    }

    @Test
    public void testWinCondition() {
        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (tiles[x][y] == TileType.PAC_DOT || tiles[x][y] == TileType.ENERGIZER) {
                    tiles[x][y] = TileType.EMPTY;
                }
            }
        }

        tiles[5][5] = TileType.PAC_DOT;
        player.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull("Winner should be declared when all dots are eaten", nextState.winner());
        assertEquals("Player 0 should be the winner", 0, nextState.winner().getId());
    }

    @Test
    public void testFruitSpawning() {

        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        boolean foundFruit = false;
        for (TileType[] tile : tiles) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (tile[y] == TileType.CHERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }
        assertFalse("Fruit (Cherry) should NOT be on the map initially", foundFruit);

        int dotsEaten = 0;
        for (int x = 0; x < tiles.length && dotsEaten < 70; x++) {
            for (int y = 0; y < tiles[0].length && dotsEaten < 70; y++) {
                if (tiles[x][y] == TileType.PAC_DOT) {
                    tiles[x][y] = TileType.EMPTY;
                    player.addPoints(10);
                    dotsEaten++;
                }
            }
        }

        controller.updateGameState(initialState, new ArrayList<>());

        for (TileType[] tile : tiles) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (tile[y] == TileType.CHERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }
        assertTrue("Fruit (Cherry) should spawn after eating dots", foundFruit);
    }

    @Test
    public void testMapResetOnWin() {

        initialState.ghosts().clear();
        Player player = initialState.players().getFirst();
        TileType[][] tiles = initialState.tiles();

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = TileType.EMPTY;
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
        assertTrue("Map should be refilled with pellets after winning", foundPellet);

        Player nextPlayer = nextState.players().getFirst();
        assertEquals("Player should be reset to spawn position on map reset", player.getSpawnPosition().x,
            nextPlayer.getPosition().x, 0.1);
        assertEquals("Player should be reset to spawn position on map reset", player.getSpawnPosition().y,
            nextPlayer.getPosition().y, 0.1);
    }

    @Test
    public void testGhostSpeedIncreaseOnLevelUp() {

        double initialSpeed = Ghost.getGHOSTSPEED();

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = TileType.EMPTY;
            }
        }

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue("Ghost speed should increase on level up", Ghost.getGHOSTSPEED() > initialSpeed);
    }

    @Test
    public void testLevelProgression() {

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = TileType.EMPTY;
            }
        }

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull("Winner should be set", nextState.winner());
        
        boolean foundPellet = false;
        for (TileType[] row : nextState.tiles()) {
            for (TileType tile : row) {
                if (tile == TileType.PAC_DOT || tile == TileType.ENERGIZER) {
                    foundPellet = true;
                    break;
                }
            }
        }
        assertTrue("Map should be refilled with pellets on level progression", foundPellet);
        assertEquals("Clock should reset on new level", 1, nextState.clock());
    }

    @Test
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

        assertEquals("Player should lose a life", 2, player.getLives());
        assertFalse("Player should be waiting for respawn", player.isAlive());
    }

    @Test
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

        assertEquals("Player should lose only 1 life for simultaneous collision", 2, player.getLives());
        assertFalse("Player should be waiting for respawn", player.isAlive());
    }

    @Test
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

        assertEquals("Player should NOT lose a life", 3, player.getLives());
        assertTrue("Ghost should be on respawn timer", ghost.getRespawnTimer() > 0);
    }

    @Test
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

        assertEquals("Player lives should not decrease while respawning", 3, player.getLives());
        assertTrue("Respawn timer should still be active", player.getRespawnTimer() > 0.0);
    }

    @Test
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

        assertEquals("Player should lose a life immediately upon unsafe respawn", 2, player.getLives());
        assertFalse("Player should be dead again", player.isAlive());
    }

    @Test
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

        assertTrue("Ghost 1 should move East", g1.getPosition().x > 3 * TILE_SIZE);
        assertTrue("Ghost 2 should move West", g2.getPosition().x < 4 * TILE_SIZE);
    }

    @Test
    public void testScatterChaseModeSwitch() {
        Ghost.setGhostScatterMode(true);
        Ghost.setGhostChaseTimer(6.99);

        controller.updateGameState(initialState, new ArrayList<>());

        assertFalse("Should have switched to Chase mode", Ghost.isGhostScatterMode());
        assertEquals("Timer should reset", 0.0, Ghost.getGhostChaseTimer(), 0.1);
    }

    @Test
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

        assertEquals("Lives should be 0", 0, player.getLives());
        assertFalse("Player should be dead", player.isAlive());
    }

    @Test
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

        assertTrue("Ghost should be on respawn timer", ghost.getRespawnTimer() > 0);
        assertEquals("Ghost should be hidden", -1000, ghost.getPosition().x, 0.1);

        ghost.setRespawnTimer(0.001);

        double originalSpeed = Ghost.getGHOSTSPEED();
        Ghost.setGHOSTSPEED(0.0);
        try {
            controller.updateGameState(initialState, new ArrayList<>());
        } finally {
            Ghost.setGHOSTSPEED(originalSpeed);
        }

        assertEquals("Ghost should be at spawn position after respawn", spawnPos.x, ghost.getPosition().x, 0.1);
        assertEquals("Ghost should be at spawn position after respawn", spawnPos.y, ghost.getPosition().y, 0.1);
        assertEquals("Respawn timer should be 0", 0.0, ghost.getRespawnTimer(), 0.001);
    }

    @Test
    public void testStateImmutability() {

        initialState.ghosts().clear();
        Ghost ghost = new Ghost(GhostType.RED);
        ghost.setPosition(new Position(100, 100));
        initialState.ghosts().add(ghost);

        double startX = ghost.getPosition().x;

        controller.updateGameState(initialState, new ArrayList<>());

        assertNotEquals("Warning: State is mutable. Ghost object was updated in place.", startX,
            ghost.getPosition().x, 0.001);
    }

    @Test
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

        assertFalse("Player should be dead (Collision priority over Powerup)", player.isAlive());
        assertEquals("Energizer should theoretically remain if player died before eating", TileType.ENERGIZER,
            tiles[2][1]);
    }

    @Test
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

        assertEquals("Player should face West immediately", Direction.WEST, player.getDirection());
        assertTrue("Player should move West", player.getPosition().x < x1);
    }

    @Test
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
    public void testIntegerOverflowClock() {

        GameState highClockState = new GameState(
            Integer.MAX_VALUE - 5,
            initialState.players(),
            initialState.ghosts(),
            initialState.tiles(),
            null
        );

        GameState nextState = controller.updateGameState(highClockState, new ArrayList<>());

        assertTrue("Clock should handle potential high values gracefully", nextState.clock() != 0);
    }

    @Test
    public void testWinConditionLogic() {

        initialState.ghosts().clear();
        TileType[][] tiles = initialState.tiles();

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = TileType.EMPTY;
            }
        }

        GameState nextState = controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull("Winner should be declared when map is empty", nextState.winner());
    }

    @Test
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

        Player p = initialState.players().getFirst();
        p.addPoints(70 * 10);

        int removed = 0;
        for (int x = 0; x < tiles.length && removed < 70; x++) {
            for (int y = 0; y < tiles[0].length && removed < 70; y++) {
                if (tiles[x][y] == TileType.PAC_DOT) {
                    tiles[x][y] = TileType.EMPTY;
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
        assertEquals("Pellets should be removed from map", pellets - 127, remainingPellets);

        boolean foundFruit = false;
        for (TileType[] row : initialState.tiles()) {
            for (TileType t : row) {
                if (t == TileType.CHERRY || t == TileType.STRAWBERRY) {
                    foundFruit = true;
                    break;
                }
            }
        }

        assertTrue("Fruit should spawn after eating pellets", foundFruit);
    }

    @Test
    public void testInputBufferingPrecision() {

        Player p = initialState.players().getFirst();
        double startX = TILE_SIZE - 1.0;
        p.setPosition(new Position(startX, TILE_SIZE));
        p.setDirection(Direction.EAST);
        p.setIntendedDirection(Direction.SOUTH);

        controller.updateGameState(initialState, new ArrayList<>());

        assertNotNull("Player should maintain an intended direction until turn is executed",
            p.getIntendedDirection());
        assertEquals("Player should still be facing East if not yet at center of tile", Direction.EAST, p.getDirection());
        assertTrue("Player should have moved forward East", p.getPosition().x > startX);
        assertEquals("Player Y should not have changed yet", TILE_SIZE, p.getPosition().y, 0.001);
    }

    @Test
    public void testTeleportationBoundary() {

        Player p = initialState.players().getFirst();
        double mapWidth = initialState.tiles().length * TILE_SIZE;

        p.setPosition(new Position(mapWidth - (TILE_SIZE / 2.0) + 0.1, TILE_SIZE));
        p.setDirection(Direction.EAST);

        controller.updateGameState(initialState, new ArrayList<>());

        assertTrue("Player should have teleported to left", p.getPosition().x < TILE_SIZE);
        assertEquals("Player should be at the far left after wrapping", 0.1, p.getPosition().x, 0.5);
    }

    @Test
    public void testUpdateGameStateForBasicMovement() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(TILE_SIZE, TILE_SIZE));
        player.setDirection(Direction.EAST);
        
        // At clock 1, move EAST
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0)); // 2 = EAST
        
        GameState finalState = controller.updateGameStateFor(initialState, 5);
        
        assertEquals(5, finalState.clock());
        assertTrue("Player should have moved EAST from resimulation", finalState.players().getFirst().getPosition().x > TILE_SIZE);
        double expectedX = TILE_SIZE + 4 * (Constants.PLAYER_SPEED / Constants.TARGET_FPS);
        assertEquals("Player should be at expected X after 4 ticks of movement", expectedX, finalState.players().getFirst().getPosition().x, 0.001);
    }

    @Test
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
        assertEquals("Player should be facing SOUTH after resimulation", Direction.SOUTH, finalPlayer.getDirection());
        assertTrue("Player should have moved SOUTH", finalPlayer.getPosition().y > 3 * TILE_SIZE);
    }

    @Test
    public void testUpdateGameStateForGhostCollision() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        Player player = initialState.players().getFirst();
        player.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        player.setLives(3);

        Ghost ghost = initialState.ghosts().getFirst();
        ghost.setPosition(new Position(5 * TILE_SIZE, 3 * TILE_SIZE));
        ghost.setDirection(Direction.WEST);
        
        // Player moves EAST towards ghost
        player.setDirection(Direction.EAST);
        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));

        // Resimulate long enough for them to collide
        GameState finalState = controller.updateGameStateFor(initialState, 20);

        assertTrue("Player should have lost a life during resimulation", finalState.players().getFirst().getLives() < 3);
        assertFalse("Player should be dead or respawning", finalState.players().getFirst().isAlive());
    }

    @Test
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
        GameState finalState = controller.updateGameStateFor(initialState, 10);

        assertTrue("Ghosts should be frightened after eating energizer in resimulation", Ghost.getFrightenedTimerSec() > 0);
    }

    @Test
    public void testUpdateGameStateForImmutability() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        int initialClock = initialState.clock();
        Position initialPos = new Position(initialState.players().getFirst().getPosition().x, initialState.players().getFirst().getPosition().y);

        Constants.cleanActions.addAction(new Action(0, 1, 2, 0));
        
        GameState finalState = controller.updateGameStateFor(initialState, 10);

        assertNotEquals("Final state should be different from initial state", initialClock, finalState.clock());
        assertEquals("Initial state clock should NOT have changed", initialClock, initialState.clock());
        assertEquals("Initial state position should NOT have changed", initialPos.x, initialState.players().getFirst().getPosition().x, 0.001);
    }

    @Test
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
        
        assertEquals("States should have same clock", state1.clock(), state2.clock());
        assertEquals("Player X should be same", state1.players().getFirst().getPosition().x, state2.players().getFirst().getPosition().x, 0.001);
        assertEquals("Player Y should be same", state1.players().getFirst().getPosition().y, state2.players().getFirst().getPosition().y, 0.001);
    }

    @Test
    public void testUpdateGameStateForLargeClockJump() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        
        // Resimulate 100 ticks
        GameState finalState = controller.updateGameStateFor(initialState, 100);
        
        assertEquals(100, finalState.clock());
    }

    @Test
    public void testUpdateGameStateForClockRegression() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        GameState futureState = new GameState(10, initialState.players(), initialState.ghosts(), initialState.tiles(), null);
        
        // targetClock (5) < futureState.clock() (10)
        GameState resultState = controller.updateGameStateFor(futureState, 5);
        
        assertEquals("Clock should not regress and state should remain unchanged", 10, resultState.clock());
    }

    @Test
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
        assertEquals("Direction should still be EAST", Direction.EAST, finalState.players().getFirst().getDirection());
    }

    @Test
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
        
        assertTrue("P0 should have moved WEST", finalState.players().getFirst().getPosition().x < 3 * TILE_SIZE);
        assertTrue("P1 should have moved EAST", finalState.players().get(1).getPosition().x > 10 * TILE_SIZE);
    }

    @Test
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
        
        assertTrue("Missed action should be detected during resimulation", Constants.cleanActions.missedAction());
    }

    @Test
    public void testUpdateGameStateForEnergizerExpiration() {
        Constants.cleanActions = new ActionList();
        initialState = controller.initializeGameState(1);
        
        // Set frightened timer to very low value (e.g., 2 frames worth)
        Ghost.setFrightenedTimerSec(2.0 / Constants.TARGET_FPS);
        assertTrue(Ghost.getFrightenedTimerSec() > 0);
        
        // Resimulate 5 ticks
        controller.updateGameStateFor(initialState, 5);
        
        assertEquals("Frightened timer should have expired during resimulation", 0.0, Ghost.getFrightenedTimerSec(), 0.001);
    }
}
