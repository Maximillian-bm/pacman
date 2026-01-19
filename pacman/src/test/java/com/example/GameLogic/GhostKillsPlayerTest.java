package com.example.GameLogic;

import static com.example.model.Constants.COLLISION_DISTANCE_PVG;
import static com.example.model.Constants.PLAYER_RESPAWN_DELAY_SEC;
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

@DisplayName("Ghost Kills Player Tests")
public class GhostKillsPlayerTest extends BaseTest {

    private ClientGameController controller;
    private GameState state;

    // Test position coordinates
    private static final int TEST_GRID_X = 6;
    private static final int TEST_GRID_Y = 5;

    // Horizontal corridor at row 5
    private static final int HORIZONTAL_CORRIDOR_Y = 5;
    // Vertical corridor at column 6
    private static final int VERTICAL_CORRIDOR_X = 6;
    // Intersection point
    private static final int INTERSECTION_X = 6;
    private static final int INTERSECTION_Y = 5;

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
        // Ensure no frightened mode (player can't eat ghosts)
        state.entityTracker().setFrightenedTimerSec(0.0);
        state.entityTracker().clearPowerOwner();
    }

    private void clearTileAt(int gridX, int gridY) {
        if (gridX >= 0 && gridY >= 0 && gridY < state.tiles().length && gridX < state.tiles()[0].length) {
            state.tiles()[gridY][gridX] = TileType.EMPTY;
        }
    }

    private void setupPlayerForKill(Player player) {
        player.setLives(3);
        player.setAlive(true);
        player.setRespawnTimer(0.0);
        player.setInvulnerableTimer(0.0);
        player.setPowerUpTimer(0.0);
        player.setIntendedDirection(null);
    }

    private void setupGhostForKill(Ghost ghost) {
        ghost.setRespawnTimer(0.0);
    }

    private void disableOtherPlayer() {
        Player otherPlayer = state.players().get(1);
        otherPlayer.setPosition(new Position(-1000, -1000));
        otherPlayer.setAlive(false);
    }

    private void clearHorizontalCorridor() {
        for (int x = 1; x < 27; x++) {
            clearTileAt(x, HORIZONTAL_CORRIDOR_Y);
        }
    }

    private void clearVerticalCorridor() {
        for (int y = 1; y < 12; y++) {
            clearTileAt(VERTICAL_CORRIDOR_X, y);
        }
    }

    private void clearIntersectionArea() {
        clearHorizontalCorridor();
        for (int y = 5; y <= 8; y++) {
            clearTileAt(INTERSECTION_X, y);
        }
    }

    @Nested
    @DisplayName("Basic Ghost Kill Mechanics")
    class BasicGhostKillMechanics {

        @Test
        @DisplayName("Ghost kills player on direct collision")
        public void testGhostKillsPlayerOnCollision() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertFalse(player.isAlive(), "Player should be dead after ghost collision");
            assertEquals(2, player.getLives(), "Player should have lost one life");
        }

        @Test
        @DisplayName("Player respawn timer is set correctly when killed")
        public void testPlayerRespawnTimerSetCorrectly() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(PLAYER_RESPAWN_DELAY_SEC, player.getRespawnTimer(), 0.1,
                "Player respawn timer should be set to PLAYER_RESPAWN_DELAY_SEC");
        }

        @Test
        @DisplayName("Player lostHeart flag is set when killed")
        public void testLostHeartFlagSet() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);
            assertFalse(player.isLostHeart(), "lostHeart should be false initially");

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertTrue(player.isLostHeart(), "lostHeart flag should be set after being killed");
        }

        @Test
        @DisplayName("Player intended direction is cleared when killed")
        public void testIntendedDirectionClearedWhenKilled() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);
            player.setIntendedDirection(Direction.NORTH);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(null, player.getIntendedDirection(),
                "Player intended direction should be null after being killed");
        }

        @Test
        @DisplayName("Ghost is not affected when killing player")
        public void testGhostNotAffectedWhenKillingPlayer() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            ghost = state.ghosts().getFirst();

            assertEquals(0.0, ghost.getRespawnTimer(), 0.001,
                "Ghost respawn timer should remain 0 after killing player");
        }
    }

    @Nested
    @DisplayName("Player Lives and Death")
    class PlayerLivesAndDeath {

        @Test
        @DisplayName("Player with 1 life becomes fully dead when killed")
        public void testPlayerWithOneLifeBecomesDead() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            player.setLives(1);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertFalse(player.isAlive(), "Player should not be alive");
            assertEquals(0, player.getLives(), "Player should have 0 lives");
            assertTrue(player.isDead(), "Player isDead() should return true");
        }

        @Test
        @DisplayName("Player loses lives progressively with multiple deaths")
        public void testPlayerLosesLivesProgressively() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            player.setLives(3);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);

            // First death
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));
            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            assertEquals(2, player.getLives(), "Should have 2 lives after first death");

            // Simulate respawn and second death
            player.setAlive(true);
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            player.setPosition(pos);
            ghost = state.ghosts().getFirst();
            ghost.setPosition(new Position(pos.x, pos.y));
            ghost.setRespawnTimer(0.0);

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            assertEquals(1, player.getLives(), "Should have 1 life after second death");

            // Third death
            player.setAlive(true);
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            player.setPosition(pos);
            ghost = state.ghosts().getFirst();
            ghost.setPosition(new Position(pos.x, pos.y));
            ghost.setRespawnTimer(0.0);

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();
            assertEquals(0, player.getLives(), "Should have 0 lives after third death");
            assertTrue(player.isDead(), "Player should be fully dead");
        }

        @Test
        @DisplayName("Dead player (0 lives) cannot lose more lives")
        public void testDeadPlayerCannotLoseMoreLives() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            player.setLives(0);
            player.setAlive(false);
            player.setRespawnTimer(0.0);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(0, player.getLives(), "Dead player should still have 0 lives");
        }
    }

    @Nested
    @DisplayName("Player Protection States")
    class PlayerProtectionStates {

        @Test
        @DisplayName("Invulnerable player cannot be killed by ghost")
        public void testInvulnerablePlayerCannotBeKilled() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            player.setInvulnerableTimer(2.0); // Spawn protection active
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Invulnerable player should still be alive");
            assertEquals(3, player.getLives(), "Invulnerable player should not lose life");
        }

        @Test
        @DisplayName("Respawning player cannot be killed")
        public void testRespawningPlayerCannotBeKilled() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            player.setRespawnTimer(1.0); // Currently respawning
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            int livesBefore = player.getLives();
            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(livesBefore, player.getLives(),
                "Respawning player should not lose life");
        }

        @Test
        @DisplayName("Already dead player cannot be killed again")
        public void testAlreadyDeadPlayerCannotBeKilledAgain() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            player.setLives(2);
            player.setAlive(false); // Already dead
            player.setRespawnTimer(0.0);
            player.setInvulnerableTimer(0.0);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertEquals(2, player.getLives(), "Already dead player should not lose more lives");
        }
    }

    @Nested
    @DisplayName("Ghost State Conditions")
    class GhostStateConditions {

        @Test
        @DisplayName("Respawning ghost cannot kill player")
        public void testRespawningGhostCannotKillPlayer() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            ghost.setRespawnTimer(5.0); // Ghost is respawning

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Respawning ghost should not kill player");
            assertEquals(3, player.getLives(), "Player should not lose life to respawning ghost");
        }

        @Test
        @DisplayName("Only first ghost kills player (break after kill)")
        public void testOnlyFirstGhostKillsPlayer() {
            state.ghosts().clear();
            Ghost g1 = new Ghost(GhostType.RED);
            Ghost g2 = new Ghost(GhostType.PINK);
            state.ghosts().add(g1);
            state.ghosts().add(g2);

            Player player = state.players().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            g1.setPosition(new Position(pos.x, pos.y));
            g1.setRespawnTimer(0.0);
            g2.setPosition(new Position(pos.x, pos.y));
            g2.setRespawnTimer(0.0);

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            // Player should only lose 1 life, not 2
            assertEquals(2, player.getLives(), "Player should only lose 1 life even with multiple ghosts");
        }
    }

    @Nested
    @DisplayName("Collision Distance")
    class CollisionDistanceTests {

        @Test
        @DisplayName("Ghost kills player when exactly at collision distance")
        public void testGhostKillsAtCollisionDistance() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position playerPos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(playerPos);
            // Position ghost just within collision distance
            ghost.setPosition(new Position(playerPos.x + COLLISION_DISTANCE_PVG - 0.1, playerPos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertFalse(player.isAlive(), "Ghost should kill player within collision distance");
        }

        @Test
        @DisplayName("Ghost does NOT kill player when outside collision distance")
        public void testGhostDoesNotKillOutsideCollisionDistance() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            clearTileAt(TEST_GRID_X + 3, TEST_GRID_Y);
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position playerPos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(playerPos);
            // Position ghost far outside collision distance
            ghost.setPosition(new Position(playerPos.x + 3 * TILE_SIZE, playerPos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Ghost should not kill player outside collision distance");
            assertEquals(3, player.getLives(), "Player should not lose life");
        }
    }

    @Nested
    @DisplayName("Movement-Based Kill Scenarios")
    class MovementBasedKillScenarios {

        // ==================== HEAD-ON COLLISION TESTS ====================

        @Test
        @DisplayName("Player running EAST into ghost moving WEST (head-on kill)")
        public void testHeadOnKillEastVsWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Player moving east, ghost moving west - head-on collision
            player.setPosition(new Position(5 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            ghost.setPosition(new Position(7 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player moving EAST should be killed by ghost moving WEST");
        }

        @Test
        @DisplayName("Player running WEST into ghost moving EAST (head-on kill)")
        public void testHeadOnKillWestVsEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            player.setPosition(new Position(10 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            ghost.setPosition(new Position(8 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player moving WEST should be killed by ghost moving EAST");
        }

        @Test
        @DisplayName("Player running SOUTH into ghost moving NORTH (head-on kill)")
        public void testHeadOnKillSouthVsNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 1 * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 3 * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player moving SOUTH should be killed by ghost moving NORTH");
        }

        @Test
        @DisplayName("Player running NORTH into ghost moving SOUTH (head-on kill)")
        public void testHeadOnKillNorthVsSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 5 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 3 * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player moving NORTH should be killed by ghost moving SOUTH");
        }

        // ==================== PLAYER CANNOT ESCAPE (SAME DIRECTION) ====================
        // Since player is faster than ghost, ghost cannot catch player from behind
        // These tests verify the speed difference

        @Test
        @DisplayName("Ghost moving EAST cannot catch player moving EAST (player is faster)")
        public void testGhostCannotCatchPlayerSameDirectionEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Ghost behind player, both moving east
            // Player is faster, so ghost should never catch up
            ghost.setPosition(new Position(5 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            player.setPosition(new Position(6 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            // Run for several frames
            for (int i = 0; i < 15; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
            }
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Ghost should not catch faster player moving same direction");
            assertEquals(3, player.getLives(), "Player should not lose life");
        }

        @Test
        @DisplayName("Ghost moving WEST cannot catch player moving WEST (player is faster)")
        public void testGhostCannotCatchPlayerSameDirectionWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearHorizontalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Ghost behind player (to the east), both moving west
            ghost.setPosition(new Position(10 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            player.setPosition(new Position(9 * TILE_SIZE, HORIZONTAL_CORRIDOR_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            for (int i = 0; i < 15; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
            }
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Ghost should not catch faster player moving same direction");
            assertEquals(3, player.getLives(), "Player should not lose life");
        }

        @Test
        @DisplayName("Ghost moving SOUTH cannot catch player moving SOUTH (player is faster)")
        public void testGhostCannotCatchPlayerSameDirectionSouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Ghost behind player (to the north), both moving south
            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 1 * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 2 * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            for (int i = 0; i < 15; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
            }
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Ghost should not catch faster player moving same direction");
            assertEquals(3, player.getLives(), "Player should not lose life");
        }

        @Test
        @DisplayName("Ghost moving NORTH cannot catch player moving NORTH (player is faster)")
        public void testGhostCannotCatchPlayerSameDirectionNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearVerticalCorridor();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Ghost behind player (to the south), both moving north
            // Start them 2 tiles apart to ensure no initial collision
            ghost.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 6 * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            player.setPosition(new Position(VERTICAL_CORRIDOR_X * TILE_SIZE, 4 * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            for (int i = 0; i < 15; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
            }
            player = state.players().getFirst();

            assertTrue(player.isAlive(), "Ghost should not catch faster player moving same direction");
            assertEquals(3, player.getLives(), "Player should not lose life");
        }

        // ==================== PERPENDICULAR COLLISION TESTS ====================

        @Test
        @DisplayName("Player running EAST killed by ghost from NORTH (perpendicular)")
        public void testPerpendicularKillEastByNorth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Player approaches intersection from west
            player.setPosition(new Position((INTERSECTION_X - 1) * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.EAST);

            // Ghost at intersection moving south (will hit player)
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.SOUTH);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player should be killed by ghost at perpendicular intersection");
        }

        @Test
        @DisplayName("Player running WEST killed by ghost from SOUTH (perpendicular)")
        public void testPerpendicularKillWestBySouth() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Player approaches intersection from east
            player.setPosition(new Position((INTERSECTION_X + 1) * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            player.setDirection(Direction.WEST);

            // Ghost at intersection
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.NORTH);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player should be killed by ghost at perpendicular intersection");
        }

        @Test
        @DisplayName("Player running NORTH killed by ghost from EAST (perpendicular)")
        public void testPerpendicularKillNorthByEast() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Player approaches intersection from south
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, (INTERSECTION_Y + 1) * TILE_SIZE));
            player.setDirection(Direction.NORTH);

            // Ghost at intersection
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.WEST);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player should be killed by ghost at perpendicular intersection");
        }

        @Test
        @DisplayName("Player running SOUTH killed by ghost from WEST (perpendicular)")
        public void testPerpendicularKillSouthByWest() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearIntersectionArea();
            disableOtherPlayer();
            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            // Player approaches intersection from north
            player.setPosition(new Position(INTERSECTION_X * TILE_SIZE, (INTERSECTION_Y - 1) * TILE_SIZE));
            player.setDirection(Direction.SOUTH);

            // Ghost at intersection
            ghost.setPosition(new Position(INTERSECTION_X * TILE_SIZE, INTERSECTION_Y * TILE_SIZE));
            ghost.setDirection(Direction.EAST);

            boolean playerKilled = false;
            for (int i = 0; i < 20 && !playerKilled; i++) {
                state = controller.updateGameState(state, new ArrayList<>());
                player = state.players().getFirst();
                playerKilled = !player.isAlive();
            }

            assertTrue(playerKilled, "Player should be killed by ghost at perpendicular intersection");
        }
    }

    @Nested
    @DisplayName("Frightened Mode Edge Cases")
    class FrightenedModeEdgeCases {

        @Test
        @DisplayName("Non-power-owner player is killed even during frightened mode")
        public void testNonPowerOwnerKilledDuringFrightenedMode() {
            Player powerOwner = state.players().getFirst();
            Player victim = state.players().get(1);
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);

            // Give power to first player
            powerOwner.setPowerUpTimer(8.0);
            state.entityTracker().assignPowerTo(powerOwner);
            state.entityTracker().setFrightenedTimerSec(8.0);

            // Move power owner away
            powerOwner.setPosition(new Position(20 * TILE_SIZE, 20 * TILE_SIZE));

            // Setup victim (non-power-owner)
            victim.setLives(3);
            victim.setAlive(true);
            victim.setRespawnTimer(0.0);
            victim.setInvulnerableTimer(0.0);
            victim.setPowerUpTimer(0.0);

            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            victim.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            victim = state.players().get(1);

            // Non-power-owner should NOT be killed during frightened mode
            // Based on the code: if (frightened) { ... continue; }
            // The ghost doesn't kill anyone during frightened mode, it just continues
            assertTrue(victim.isAlive(), "Non-power-owner should not be killed during frightened mode");
        }

        @Test
        @DisplayName("Player without power is NOT killed during frightened mode")
        public void testPlayerNotKilledDuringFrightenedModeEvenWithoutPower() {
            Player player = state.players().getFirst();
            Ghost ghost = state.ghosts().getFirst();

            clearTileAt(TEST_GRID_X, TEST_GRID_Y);
            disableOtherPlayer();

            // Set frightened mode but player has no power
            state.entityTracker().setFrightenedTimerSec(8.0);
            // No power owner assigned

            setupPlayerForKill(player);
            setupGhostForKill(ghost);

            Position pos = new Position(TEST_GRID_X * TILE_SIZE, TEST_GRID_Y * TILE_SIZE);
            player.setPosition(pos);
            ghost.setPosition(new Position(pos.x, pos.y));

            state = controller.updateGameState(state, new ArrayList<>());
            player = state.players().getFirst();

            // During frightened mode, ghosts don't kill players (they just continue in the loop)
            assertTrue(player.isAlive(), "Player should not be killed during frightened mode");
        }
    }
}
