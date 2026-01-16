package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Ghost AI and Movement Logic Tests")
public class GhostLogicTest extends BaseTest {

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
        state = controller.initializeGameState(2);
        Ghost.setFrightenedTimerSec(0.0);
        Ghost.setGhostScatterMode(false);
        Constants.clock = 0;
    }

    @Test
    @DisplayName("Ghost should remain stationary when trapped in a 1x1 empty space surrounded by walls")
    public void testGhostTrappedInBox() {

        TileType[][] boxMap = new TileType[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                boxMap[y][x] = TileType.WALL;
            }
        }
        boxMap[2][2] = TileType.EMPTY;

        GameState boxState = new GameState(
            0,
            state.players(),
            state.ghosts(),
            boxMap,
            null
        );

        Ghost g = boxState.ghosts().getFirst();
        g.setPosition(new Position(2 * TILE_SIZE, 2 * TILE_SIZE));

        controller.updateGameState(boxState, new ArrayList<>());

        assertEquals(2 * TILE_SIZE, g.getPosition().x, 0.1);
        assertEquals(2 * TILE_SIZE, g.getPosition().y, 0.1);
    }

    @Test
    @DisplayName("Player should be able to eat multiple ghosts in one state update")
    public void testDoubleGhostEat() {
        state.ghosts().clear();

        Ghost g1 = new Ghost(GhostType.RED);
        g1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        g1.setRespawnTimer(0.0);

        Ghost g2 = new Ghost(GhostType.PINK);
        g2.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        g2.setRespawnTimer(0.0);

        state.ghosts().add(g1);
        state.ghosts().add(g2);

        Player p = state.players().getFirst();
        p.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));

        Ghost.setFrightenedTimerSec(10.0);

        controller.updateGameState(state, new ArrayList<>());

        assertTrue(g1.getRespawnTimer() > 0, "Ghost 1 should be eaten (respawning)");
        assertTrue(g2.getRespawnTimer() > 0, "Ghost 2 should be eaten (respawning)");
    }

    @Test
    @DisplayName("Ghost respawn timer should decrement and cap at zero")
    public void testGhostRespawnTimerReset() {
        Ghost g = state.ghosts().getFirst();
        g.setRespawnTimer(0.0000001);

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(0.0, g.getRespawnTimer(), 0.00001, "Respawn timer should cap at 0.0");
    }

    @Test
    @DisplayName("Multiple ghosts should be able to respawn simultaneously")
    public void testSimultaneousGhostRespawn() {
        for (Ghost g : state.ghosts()) {
            g.setRespawnTimer(0.001);
        }

        controller.updateGameState(state, new ArrayList<>());

        for (Ghost g : state.ghosts()) {
            assertEquals(0.0, g.getRespawnTimer(), 0.001, "All ghosts should have respawned at their base points");
            assertNotEquals(-1000, g.getPosition().x, 0.1, "Ghost should not be at 'hidden' position");
        }
    }

    @Test
    @DisplayName("Ghost should ignore respawning players when selecting a target")
    public void testGhostDoesNotTargetRespawningPlayer() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst()
            .orElseThrow(() -> new AssertionError("Blinky not found"));

        blinky.setPosition(new Position(3 * TILE_SIZE, TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        p1.setAlive(true);
        p1.setRespawnTimer(0.0);
        p1.setPosition(new Position(15 * TILE_SIZE, TILE_SIZE));

        p2.setAlive(false);
        p2.setRespawnTimer(1.0);
        p2.setPosition(new Position(-1000, -1000));

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(Direction.EAST, blinky.getDirection(), "Ghost should target the only alive player (East)");
    }

    @Test
    @DisplayName("Ghost should ignore dead players when selecting a target")
    public void testGhostDoesNotTargetDeadPlayer() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst()
            .orElseThrow(() -> new AssertionError("Blinky not found"));

        blinky.setPosition(new Position(3 * TILE_SIZE, TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        p1.setAlive(true);
        p1.setRespawnTimer(0.0);
        p1.setPosition(new Position(10 * TILE_SIZE, TILE_SIZE));

        p2.setAlive(false);
        p2.setLives(0);
        p2.setPosition(new Position(-1000, -1000));

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(Direction.EAST, blinky.getDirection(), "Ghost should target the only alive player (East)");
    }

    @Test
    @DisplayName("Ghost should retreat to its home corner if no players are alive")
    public void testGhostGoesToCornerWhenNoAlivePlayers() {
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst()
            .orElseThrow(() -> new AssertionError("Blinky not found"));

        for (Player p : state.players()) {
            p.setAlive(false);
            p.setLives(0);
            p.setPosition(new Position(-1000, -1000));
        }

        double startX = 3 * TILE_SIZE;
        double startY = TILE_SIZE;
        blinky.setPosition(new Position(startX, startY));
        blinky.setDirection(Direction.SOUTH);

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(Direction.EAST, blinky.getDirection(), "Ghost should move towards its corner if no players are alive");
        assertTrue(blinky.getPosition().x > startX, "Ghost should have moved East");
        assertEquals(startY, blinky.getPosition().y, 0.01, "Ghost Y should be unchanged if moving purely East");
    }

    @Test
    @DisplayName("Frightened ghost should flee from alive players and ignore dead ones")
    public void testFrightenedGhostFleesFromAlivePlayerIgnoringDeadOnes() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst()
            .orElseThrow(() -> new AssertionError("Blinky not found"));

        Ghost.setFrightenedTimerSec(10.0);

        double startX = 3 * TILE_SIZE;
        blinky.setPosition(new Position(startX, TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        p1.setAlive(true);
        p1.setPosition(new Position(10 * TILE_SIZE, TILE_SIZE));

        p2.setAlive(false);
        p2.setLives(0);
        p2.setPosition(new Position(-1000, -1000));

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(Direction.WEST, blinky.getDirection(), "Frightened ghost should flee from the only alive player (West)");
        assertTrue(blinky.getPosition().x < startX, "Ghost should have moved West away from P1");
    }

    @Test
    @DisplayName("Ghost should immediately update its target when the primary player dies")
    public void testGhostChangesTargetImmediatelyWhenPlayerDies() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst()
            .orElseThrow(() -> new AssertionError("Blinky not found"));

        blinky.setPosition(new Position(3 * TILE_SIZE, TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        p1.setAlive(true);
        p1.setPosition(new Position(2 * TILE_SIZE, TILE_SIZE));

        p2.setAlive(true);
        p2.setPosition(new Position(10 * TILE_SIZE, TILE_SIZE));

        p1.setAlive(false);
        p1.setLives(0);
        p1.setPosition(new Position(-1000, -1000));

        controller.updateGameState(state, new ArrayList<>());

        assertEquals(Direction.EAST, blinky.getDirection(), "Ghost should immediately retarget to P2 after P1 dies");
    }
}
