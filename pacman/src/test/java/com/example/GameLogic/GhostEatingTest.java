package com.example.GameLogic;

import static com.example.model.Constants.COLLISION_DISTANCE_PVG;
import static com.example.model.Constants.FRIGHTENED_DURATION_SEC;
import static com.example.model.Constants.GHOST_RESPAWN_DELAY_SEC;
import static com.example.model.Constants.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.common.BaseTest;
import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.GhostType;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Ghost Eating with Energizer Tests")
public class GhostEatingTest extends BaseTest {

    private ClientGameController controller;
    private GameState state;

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
        Constants.clock = 0;
        state = controller.initializeGameState(2);
        state.entityTracker().setGhostScatterMode(false);
    }

    // Test position coordinates (center of an empty area)
    private static final int TEST_GRID_X = 6;
    private static final int TEST_GRID_Y = 1;

    private void givePlayerEnergizer(Player player) {
        player.setPowerUpTimer(FRIGHTENED_DURATION_SEC);
        player.setInvulnerableTimer(0.0);
        state.entityTracker().assignPowerTo(player);
        state.entityTracker().setFrightenedTimerSec(FRIGHTENED_DURATION_SEC);
    }

    private void clearTileAt(int gridX, int gridY) {
        if (gridX >= 0 && gridY >= 0 && gridY < state.tiles().length && gridX < state.tiles()[0].length) {
            state.tiles()[gridY][gridX] = TileType.EMPTY;
        }
    }

    private void positionEntitiesForCollision(Player player, Ghost ghost) {
        // Clear the tile so no pac-dot pickup interferes with point tests
        clearTileAt(TEST_GRID_X, TEST_GRID_Y);

        Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
        player.setPosition(pos);
        player.setAlive(true);
        player.setRespawnTimer(0.0);
        player.setInvulnerableTimer(0.0);

        ghost.setPosition(new Position(pos.x, pos.y));
        ghost.setRespawnTimer(0.0);
    }

    @Nested
    @DisplayName("Basic Ghost Eating")
    class BasicGhostEating {

        @Test
        @DisplayName("Energized player should eat ghost on collision")
        public void testEnergizedPlayerEatsGhost() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertTrue(ghost.getRespawnTimer() > 0, "Ghost should be respawning after being eaten");
            assertEquals(-1000, ghost.getPosition().x, 0.1, "Ghost should be hidden off-map");
            assertTrue(player.getPoints() > initialPoints, "Player should gain points for eating ghost");
        }

        @Test
        @DisplayName("Ghost respawn timer should be set to GHOST_RESPAWN_DELAY_SEC when eaten")
        public void testGhostRespawnTimerSetCorrectly() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);

            state = controller.updateGameState(state, new ArrayList<>());
            ghost = state.ghosts().getFirst();

            assertEquals(GHOST_RESPAWN_DELAY_SEC, ghost.getRespawnTimer(), 0.1,
                "Ghost respawn timer should be set to GHOST_RESPAWN_DELAY_SEC");
        }

        @Test
        @DisplayName("Player ateGhost flag should be set when eating a ghost")
        public void testAteGhostFlagSet() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);
            assertFalse(player.isAteGhost(), "ateGhost flag should be false initially");

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertTrue(player.isAteGhost(), "ateGhost flag should be set after eating ghost");
        }
    }

    @Nested
    @DisplayName("Point Scoring Progression")
    class PointScoringProgression {

        @Test
        @DisplayName("First ghost eaten awards 200 points")
        public void testFirstGhostAwards200Points() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(initialPoints + 200, player.getPoints(),
                "First ghost should award 200 points");
        }

        @Test
        @DisplayName("Second ghost eaten awards 400 points (600 total)")
        public void testSecondGhostAwards400Points() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            state.ghosts().add(g1);
            state.ghosts().add(g2);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            Player player = state.players().getFirst();
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            player.setInvulnerableTimer(0.0);
            g1.setPosition(new Position(pos.x, pos.y));
            g1.setRespawnTimer(0.0);
            g2.setPosition(new Position(pos.x, pos.y));
            g2.setRespawnTimer(0.0);

            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(initialPoints + 600, player.getPoints(),
                "Two ghosts eaten should award 200 + 400 = 600 points");
        }

        @Test
        @DisplayName("Third ghost eaten awards 800 points (1400 total)")
        public void testThirdGhostAwards800Points() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            Ghost g3 = new Ghost(GhostType.CYAN);
            state.ghosts().add(g1);
            state.ghosts().add(g2);
            state.ghosts().add(g3);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            Player player = state.players().getFirst();
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            player.setInvulnerableTimer(0.0);
            g1.setPosition(new Position(pos.x, pos.y));
            g1.setRespawnTimer(0.0);
            g2.setPosition(new Position(pos.x, pos.y));
            g2.setRespawnTimer(0.0);
            g3.setPosition(new Position(pos.x, pos.y));
            g3.setRespawnTimer(0.0);

            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(initialPoints + 1400, player.getPoints(),
                "Three ghosts eaten should award 200 + 400 + 800 = 1400 points");
        }

        @Test
        @DisplayName("Fourth ghost eaten awards 1600 points (3000 total)")
        public void testFourthGhostAwards1600Points() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            Ghost g3 = new Ghost(GhostType.CYAN);
            Ghost g4 = new Ghost(GhostType.ORANGE);
            state.ghosts().add(g1);
            state.ghosts().add(g2);
            state.ghosts().add(g3);
            state.ghosts().add(g4);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            Player player = state.players().getFirst();
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            player.setInvulnerableTimer(0.0);
            for (Ghost g : state.ghosts()) {
                g.setPosition(new Position(pos.x, pos.y));
                g.setRespawnTimer(0.0);
            }

            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(initialPoints + 3000, player.getPoints(),
                "Four ghosts eaten should award 200 + 400 + 800 + 1600 = 3000 points");
        }

        @Test
        @DisplayName("Ghost counter resets when new energizer is acquired")
        public void testGhostCounterResetsOnNewEnergizer() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(initialPoints + 200, player.getPoints(), "First ghost should give 200 points");

            // Simulate getting a new energizer
            state.entityTracker().assignPowerTo(player);

            // Reset ghost position for another collision
            ghost = state.ghosts().getFirst();
            ghost.setRespawnTimer(0.0);
            positionEntitiesForCollision(player, ghost);

            int pointsAfterFirstGhost = player.getPoints();
            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(pointsAfterFirstGhost + 200, player.getPoints(),
                "After new energizer, first ghost should again give 200 points");
        }
    }

    @Nested
    @DisplayName("Power Owner Restrictions")
    class PowerOwnerRestrictions {

        @Test
        @DisplayName("Only power owner can eat ghosts during frightened mode")
        public void testOnlyPowerOwnerCanEatGhosts() {
            Player powerOwner = state.players().getFirst();
            Player otherPlayer = state.players().get(1);
            Ghost ghost = state.ghosts().getFirst();

            // Clear test positions
            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            clearTileAt(15, 15);

            // Give power to first player
            givePlayerEnergizer(powerOwner);

            // Position other player (without power) next to ghost
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            otherPlayer.setPosition(pos);
            otherPlayer.setAlive(true);
            otherPlayer.setRespawnTimer(0.0);
            otherPlayer.setInvulnerableTimer(0.0);
            ghost.setPosition(new Position(pos.x, pos.y));
            ghost.setRespawnTimer(0.0);

            // Move power owner away
            powerOwner.setPosition(new Position(15 * TILE_SIZE, 15 * TILE_SIZE));

            int otherPlayerInitialPoints = otherPlayer.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            ghost = state.ghosts().getFirst();
            otherPlayer = state.players().get(1);

            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should NOT be eaten by non-power owner");
            assertEquals(otherPlayerInitialPoints, otherPlayer.getPoints(),
                "Non-power owner should not gain points from ghosts");
        }

        @Test
        @DisplayName("Player without energizer gets killed by ghost")
        public void testPlayerWithoutEnergizerDies() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            // Move other player far away so ghost targets our test player
            Player otherPlayer = state.players().get(1);
            otherPlayer.setPosition(new Position(-1000, -1000));
            otherPlayer.setAlive(false);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);

            player.setLives(3);
            player.setAlive(true);
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            player.setPowerUpTimer(0.0);
            player.setIntendedDirection(null);
            // Player defaults to EAST direction, ghost will chase and they'll collide
            state.entityTracker().setFrightenedTimerSec(0.0);
            state.entityTracker().clearPowerOwner();

            // Position ghost directly on player - they'll still be within collision distance after movement
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));
            ghost.setRespawnTimer(0.0);
            ghost.setDirection(Direction.EAST); // Same direction as player to stay together

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertFalse(player.isAlive(), "Player without energizer should die when hitting ghost");
            assertEquals(2, player.getLives(), "Player should lose a life");
            assertTrue(player.getRespawnTimer() > 0, "Player should be respawning");
        }

        @Test
        @DisplayName("Player with expired power timer cannot eat ghosts")
        public void testExpiredPowerTimerCannotEatGhost() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            // Move other player far away
            Player otherPlayer = state.players().get(1);
            otherPlayer.setPosition(new Position(-1000, -1000));
            otherPlayer.setAlive(false);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);

            player.setLives(3);
            player.setAlive(true);
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            player.setIntendedDirection(null);

            // Set up with expired power
            player.setPowerUpTimer(0.0);
            state.entityTracker().setFrightenedTimerSec(0.0);
            state.entityTracker().clearPowerOwner();

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));
            ghost.setRespawnTimer(0.0);
            ghost.setDirection(Direction.EAST); // Same direction as player to stay together

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should not be eaten when player power has expired");
            assertFalse(player.isAlive(), "Player should die when power is expired");
        }
    }

    @Nested
    @DisplayName("Entity State Restrictions")
    class EntityStateRestrictions {

        @Test
        @DisplayName("Respawning ghost cannot be eaten")
        public void testRespawningGhostCannotBeEaten() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);

            // Set ghost as already respawning
            ghost.setRespawnTimer(5.0);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertEquals(initialPoints, player.getPoints(),
                "Player should not gain points for already respawning ghost");
            assertTrue(ghost.getRespawnTimer() > 0 && ghost.getRespawnTimer() < 5.0,
                "Ghost respawn timer should just decrement, not reset");
        }

        @Test
        @DisplayName("Respawning player cannot eat ghosts")
        public void testRespawningPlayerCannotEatGhost() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);

            // Set player as respawning
            player.setRespawnTimer(1.0);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertEquals(initialPoints, player.getPoints(),
                "Respawning player should not gain points");
            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should not be eaten by respawning player");
        }

        @Test
        @DisplayName("Dead player cannot eat ghosts")
        public void testDeadPlayerCannotEatGhost() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            positionEntitiesForCollision(player, ghost);
            givePlayerEnergizer(player);

            // Set player as dead
            player.setAlive(false);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertEquals(initialPoints, player.getPoints(),
                "Dead player should not gain points");
            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should not be eaten by dead player");
        }

        @Test
        @DisplayName("Invulnerable player does not interact with ghosts")
        public void testInvulnerablePlayerNoGhostInteraction() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            player.setLives(3);
            player.setAlive(true);
            player.setRespawnTimer(0.0);

            // Give spawn protection
            player.setInvulnerableTimer(2.0);

            // Player without energizer but invulnerable
            player.setPowerUpTimer(0.0);
            state.entityTracker().setFrightenedTimerSec(0.0);
            state.entityTracker().clearPowerOwner();

            positionEntitiesForCollision(player, ghost);

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertTrue(player.isAlive(), "Invulnerable player should not die");
            assertEquals(3, player.getLives(), "Invulnerable player should not lose life");
            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should not be affected by invulnerable player");
        }
    }

    @Nested
    @DisplayName("Collision Distance")
    class CollisionDistance {

        @Test
        @DisplayName("Ghost is eaten when exactly at collision distance")
        public void testGhostEatenAtExactCollisionDistance() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            Position playerPos = new Position(6 * TILE_SIZE, 1 * TILE_SIZE);
            player.setPosition(playerPos);
            // Position ghost exactly at collision distance
            ghost.setPosition(new Position(playerPos.x + COLLISION_DISTANCE_PVG - 0.1, playerPos.y));
            ghost.setRespawnTimer(0.0);

            givePlayerEnergizer(player);

            state = controller.updateGameState(state, new ArrayList<>());
            ghost = state.ghosts().getFirst();

            assertTrue(ghost.getRespawnTimer() > 0,
                "Ghost should be eaten when within collision distance");
        }

        @Test
        @DisplayName("Ghost is NOT eaten when just outside collision distance")
        public void testGhostNotEatenOutsideCollisionDistance() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            clearTileAt(TEST_GRID_X + 3, TEST_GRID_Y);

            Position playerPos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(playerPos);
            player.setInvulnerableTimer(0.0);
            // Position ghost far enough away that movement during update won't bring it into range
            // Ghost moves at 75% speed when frightened, which is ~6.5 pixels per frame
            // Place 3 tiles away to ensure no collision even after movement
            ghost.setPosition(new Position(playerPos.x + 3 * TILE_SIZE, playerPos.y));
            ghost.setRespawnTimer(0.0);

            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            ghost = state.ghosts().getFirst();

            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost should NOT be eaten when outside collision distance");
            assertEquals(initialPoints, player.getPoints(),
                "Player should not gain points when ghost is out of range");
        }
    }

    @Nested
    @DisplayName("Multiple Ghost Scenarios")
    class MultipleGhostScenarios {

        @Test
        @DisplayName("All four ghosts can be eaten in a single update")
        public void testAllFourGhostsEatenSimultaneously() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            Ghost g3 = new Ghost(GhostType.CYAN);
            Ghost g4 = new Ghost(GhostType.ORANGE);
            state.ghosts().add(g1);
            state.ghosts().add(g2);
            state.ghosts().add(g3);
            state.ghosts().add(g4);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            Player player = state.players().getFirst();
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            player.setInvulnerableTimer(0.0);

            for (Ghost g : state.ghosts()) {
                g.setPosition(new Position(pos.x, pos.y));
                g.setRespawnTimer(0.0);
            }

            givePlayerEnergizer(player);

            state = controller.updateGameState(state, new ArrayList<>());

            for (Ghost g : state.ghosts()) {
                assertTrue(g.getRespawnTimer() > 0,
                    "Ghost " + g.getType() + " should be eaten");
                assertEquals(-1000, g.getPosition().x, 0.1,
                    "Ghost " + g.getType() + " should be hidden off-map");
            }
        }

        @Test
        @DisplayName("Only ghosts within range are eaten")
        public void testOnlyGhostsInRangeAreEaten() {
            state.ghosts().clear();
            Ghost nearGhost = new Ghost(GhostType.RED);
            Ghost farGhost = new Ghost(GhostType.PINK);
            state.ghosts().add(nearGhost);
            state.ghosts().add(farGhost);

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            clearTileAt(TEST_GRID_X + 5, TEST_GRID_Y);
            Player player = state.players().getFirst();
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            player.setInvulnerableTimer(0.0);

            nearGhost.setPosition(new Position(pos.x, pos.y));
            nearGhost.setRespawnTimer(0.0);

            farGhost.setPosition(new Position(pos.x + 5 * TILE_SIZE, pos.y));
            farGhost.setRespawnTimer(0.0);

            givePlayerEnergizer(player);

            state = controller.updateGameState(state, new ArrayList<>());
            nearGhost = state.ghosts().getFirst();
            farGhost = state.ghosts().get(1);

            assertTrue(nearGhost.getRespawnTimer() > 0, "Near ghost should be eaten");
            assertEquals(0.0, farGhost.getRespawnTimer(), 0.001, "Far ghost should NOT be eaten");
        }

        @Test
        @DisplayName("Eating ghosts sequentially across multiple updates accumulates points correctly")
        public void testSequentialGhostEatingAcrossUpdates() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            state.ghosts().add(g1);
            state.ghosts().add(g2);

            // Clear both positions
            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            clearTileAt(TEST_GRID_X + 10, TEST_GRID_Y);

            Player player = state.players().getFirst();
            givePlayerEnergizer(player);
            int initialPoints = player.getPoints();

            // Eat first ghost
            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            g1.setPosition(new Position(pos.x, pos.y));
            g1.setRespawnTimer(0.0);
            g2.setPosition(new Position(pos.x + 10 * TILE_SIZE, pos.y)); // Far away
            g2.setRespawnTimer(0.0);

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            g1 = state.ghosts().getFirst();
            g2 = state.ghosts().get(1);

            assertEquals(initialPoints + 200, player.getPoints(), "First ghost: 200 points");
            assertTrue(g1.getRespawnTimer() > 0, "First ghost should be eaten");
            assertEquals(0.0, g2.getRespawnTimer(), 0.001, "Second ghost should not be eaten yet");

            // Eat second ghost - teleport directly to ghost position
            player.setPosition(new Position(g2.getPosition().x, g2.getPosition().y));
            int pointsBeforeSecondGhost = player.getPoints();

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            g2 = state.ghosts().get(1);

            assertEquals(pointsBeforeSecondGhost + 400, player.getPoints(),
                "After second ghost: should add 400 points");
            assertTrue(g2.getRespawnTimer() > 0, "Second ghost should be eaten");
        }
    }

    @Nested
    @DisplayName("Movement-Based Collision Scenarios")
    class MovementBasedCollisions {

        // Horizontal corridor at row 5 (all PAC_DOTs)
        private static final int HORIZONTAL_CORRIDOR_Y = 5;
        // Vertical corridor at column 1 (rows 1-5)
        private static final int VERTICAL_CORRIDOR_X = 1;

        private void setupMovementTest(Player player, Ghost ghost) {
            // Disable other player to avoid interference
            Player otherPlayer = state.players().get(1);
            otherPlayer.setPosition(new Position(-1000, -1000));
            otherPlayer.setAlive(false);

            // Setup player state
            player.setAlive(true);
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            player.setIntendedDirection(null);

            // Setup ghost state
            ghost.setRespawnTimer(0.0);

            // Give player energizer
            givePlayerEnergizer(player);
        }

        private void clearHorizontalCorridor() {
            for (int x = 1; x < 27; x++) {
                clearTileAt(x, HORIZONTAL_CORRIDOR_Y);
            }
        }

        private void clearVerticalCorridor() {
            for (int y = 1; y < 6; y++) {
                clearTileAt(VERTICAL_CORRIDOR_X, y);
            }
        }

        // ==================== SAME DIRECTION TESTS (Player catches up) ====================

        @Test
        @DisplayName("Player moving EAST catches and eats ghost moving EAST")
        public void testPlayerCatchesGhostBothMovingEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            setupMovementTest(player, ghost);

            // Player is behind ghost, both moving east
            // Player at x=5, ghost at x=6 (about 1 tile ahead)
            // Player is faster, will catch up
            player.setPosition(new Position(5 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            ghost.setPosition(new Position(6 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            // Run multiple updates until collision occurs
            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving EAST should catch and eat ghost also moving EAST");
        }

        @Test
        @DisplayName("Player moving WEST catches and eats ghost moving WEST")
        public void testPlayerCatchesGhostBothMovingWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            setupMovementTest(player, ghost);

            // Player is behind ghost (to the east), both moving west
            player.setPosition(new Position(10 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            ghost.setPosition(new Position(9 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving WEST should catch and eat ghost also moving WEST");
        }

        @Test
        @DisplayName("Player moving SOUTH catches and eats ghost moving SOUTH")
        public void testPlayerCatchesGhostBothMovingSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            setupMovementTest(player, ghost);

            // Player is behind ghost (to the north), both moving south
            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 1 * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 2 * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving SOUTH should catch and eat ghost also moving SOUTH");
        }

        @Test
        @DisplayName("Player moving NORTH catches and eats ghost moving NORTH")
        public void testPlayerCatchesGhostBothMovingNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            setupMovementTest(player, ghost);

            // Player is behind ghost (to the south), both moving north
            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 5 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 4 * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving NORTH should catch and eat ghost also moving NORTH");
        }

        // ==================== OPPOSITE DIRECTION TESTS (Head-on collision) ====================

        @Test
        @DisplayName("Player moving EAST collides with ghost moving WEST (head-on)")
        public void testHeadOnCollisionEastVsWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            setupMovementTest(player, ghost);

            // Player moving east, ghost moving west - they approach each other
            player.setPosition(new Position(5 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            ghost.setPosition(new Position(7 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving EAST should collide with and eat ghost moving WEST");
        }

        @Test
        @DisplayName("Player moving WEST collides with ghost moving EAST (head-on)")
        public void testHeadOnCollisionWestVsEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            setupMovementTest(player, ghost);

            // Player moving west, ghost moving east - they approach each other
            player.setPosition(new Position(10 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            ghost.setPosition(new Position(8 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving WEST should collide with and eat ghost moving EAST");
        }

        @Test
        @DisplayName("Player moving SOUTH collides with ghost moving NORTH (head-on)")
        public void testHeadOnCollisionSouthVsNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            setupMovementTest(player, ghost);

            // Player moving south, ghost moving north - they approach each other
            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 1 * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 3 * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving SOUTH should collide with and eat ghost moving NORTH");
        }

        @Test
        @DisplayName("Player moving NORTH collides with ghost moving SOUTH (head-on)")
        public void testHeadOnCollisionNorthVsSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            setupMovementTest(player, ghost);

            // Player moving north, ghost moving south - they approach each other
            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 5 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 3 * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 20 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving NORTH should collide with and eat ghost moving SOUTH");
        }

        // ==================== PERPENDICULAR DIRECTION TESTS (Player intercepts sideways ghost) ====================

        // Intersection point where horizontal (row 5) and vertical (column 6) corridors meet
        private static final int INTERSECTION_X = 6;
        private static final int INTERSECTION_Y = 5;

        private void clearIntersectionArea() {
            // Clear horizontal corridor at row 5
            for (int x = 1; x < 27; x++) {
                clearTileAt(x, INTERSECTION_Y);
            }
            // Clear vertical corridor at column 6 (rows 5-8)
            for (int y = 5; y <= 8; y++) {
                clearTileAt(INTERSECTION_X, y);
            }
        }

        @Test
        @DisplayName("Player moving EAST intercepts ghost moving NORTH (perpendicular)")
        public void testPerpendicularEastInterceptsNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from west, ghost is at intersection moving north
            // Position player close enough to catch ghost before it escapes
            player.setPosition(new Position((INTERSECTION_X - 1) * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            // Ghost at intersection, will try to flee but player is close enough to catch it
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving EAST should intercept and eat ghost moving NORTH");
        }

        @Test
        @DisplayName("Player moving EAST intercepts ghost moving SOUTH (perpendicular)")
        public void testPerpendicularEastInterceptsSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from west, ghost approaches from north moving south
            player.setPosition(new Position(4 * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            // Ghost starts at intersection and moves south, player will catch it
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving EAST should intercept and eat ghost moving SOUTH");
        }

        @Test
        @DisplayName("Player moving WEST intercepts ghost moving NORTH (perpendicular)")
        public void testPerpendicularWestInterceptsNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from east, ghost at intersection moving north
            // Position player close enough to catch ghost
            player.setPosition(new Position((INTERSECTION_X + 1) * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            // Ghost at intersection, player is close enough to catch it
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving WEST should intercept and eat ghost moving NORTH");
        }

        @Test
        @DisplayName("Player moving WEST intercepts ghost moving SOUTH (perpendicular)")
        public void testPerpendicularWestInterceptsSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from east, ghost at intersection moves south
            player.setPosition(new Position(8 * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving WEST should intercept and eat ghost moving SOUTH");
        }

        @Test
        @DisplayName("Player moving NORTH intercepts ghost moving EAST (perpendicular)")
        public void testPerpendicularNorthInterceptsEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from south, ghost at intersection moves east
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, 8 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving NORTH should intercept and eat ghost moving EAST");
        }

        @Test
        @DisplayName("Player moving NORTH intercepts ghost moving WEST (perpendicular)")
        public void testPerpendicularNorthInterceptsWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches intersection from south, ghost at intersection moves west
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, 8 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving NORTH should intercept and eat ghost moving WEST");
        }

        @Test
        @DisplayName("Player moving SOUTH intercepts ghost moving EAST (perpendicular)")
        public void testPerpendicularSouthInterceptsEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches from north, ghost at intersection moving east
            // Position player one tile above intersection
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, (INTERSECTION_Y - 1) * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            // Ghost at intersection, player will catch it
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving SOUTH should intercept and eat ghost moving EAST");
        }

        @Test
        @DisplayName("Player moving SOUTH intercepts ghost moving WEST (perpendicular)")
        public void testPerpendicularSouthInterceptsWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            setupMovementTest(player, ghost);

            // Player approaches from north, ghost at intersection moving west
            // Position player one tile above intersection
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, (INTERSECTION_Y - 1) * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            // Ghost at intersection, player will catch it
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean ghostEaten = false;
            for (int i = 0; i < 30 && !ghostEaten; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                ghost = state.ghosts().getFirst();
                ghostEaten = ghost.getRespawnTimer() > 0;
            }

            assertTrue(ghostEaten, "Player moving SOUTH should intercept and eat ghost moving WEST");
        }
    }
}
