package com.example.GameLogic;

import com.example.common.BaseTest;
import com.example.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.*;

public class GhostTargetingTest extends BaseTest {
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

    @Before
    public void setUp() {
        controller = new ClientGameController();
        state = controller.initializeGameState(2); // 2 players
        Ghost.setFrightenedTimerSec(0.0);
        Ghost.setGhostScatterMode(false); // Chase mode
        ClientMain.clock = 0;
    }

    @Test
    public void testGhostDoesNotTargetRespawningPlayer() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst().get();

        // Place blinky at an intersection: (3, 1)
        blinky.setPosition(new Position(3 * TILE_SIZE, 1 * TILE_SIZE));
        blinky.setDirection(Direction.SOUTH); 

        // P1 is alive and to the east: (15, 1)
        p1.setAlive(true);
        p1.setRespawnTimer(0.0);
        p1.setPosition(new Position(15 * TILE_SIZE, 1 * TILE_SIZE));

        // P2 is respawning and to the west (but hidden at -1000, -1000)
        p2.setAlive(false);
        p2.setRespawnTimer(1.0);
        p2.setPosition(new Position(-1000, -1000));

        // Update game state
        controller.updateGameState(state, new ArrayList<>());

        // Blinky should choose EAST to go towards P1, rather than WEST towards the respawning P2 at (-1000, -1000)
        assertEquals("Ghost should target the only alive player (East)", Direction.EAST, blinky.getDirection());
    }

    @Test
    public void testGhostDoesNotTargetDeadPlayer() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst().get();

        // Place blinky at an intersection: (3, 1)
        blinky.setPosition(new Position(3 * TILE_SIZE, 1 * TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        // P1 is alive and to the East: (10, 1)
        // Distance to (3,1) is (10-3)^2 = 49
        p1.setAlive(true);
        p1.setRespawnTimer(0.0);
        p1.setPosition(new Position(10 * TILE_SIZE, 1 * TILE_SIZE));

        // P2 is dead for good (lives = 0)
        // Position -1000, -1000 is clamped to (0,0) in grid coordinates.
        // Distance to (3,1) is (0-3)^2 + (0-1)^2 = 10
        p2.setAlive(false);
        p2.setLives(0);
        p2.setRespawnTimer(0.0);
        p2.setPosition(new Position(-1000, -1000));

        // Update
        controller.updateGameState(state, new ArrayList<>());

        // Blinky should move towards P1 (East)
        assertEquals("Ghost should target the only alive player (East)", Direction.EAST, blinky.getDirection());
    }

    @Test
    public void testGhostGoesToCornerWhenNoAlivePlayers() {
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst().get();
        
        // Kill all players
        for (Player p : state.players()) {
            p.setAlive(false);
            p.setLives(0);
            p.setPosition(new Position(-1000, -1000));
        }

        // Place blinky at intersection: (3, 1)
        // Corner for RED is (MaxX, 0) - Top Right. MaxX is 16.
        blinky.setPosition(new Position(3 * TILE_SIZE, 1 * TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        // Update
        controller.updateGameState(state, new ArrayList<>());

        // Blinky should move towards its corner (Top-Right: which means EAST from (3,1))
        assertEquals("Ghost should move towards its corner if no players are alive", Direction.EAST, blinky.getDirection());
    }

    @Test
    public void testFrightenedGhostFleesFromAlivePlayerIgnoringDeadOnes() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst().get();

        Ghost.setFrightenedTimerSec(10.0);
        
        // Ghost at (3, 1)
        blinky.setPosition(new Position(3 * TILE_SIZE, 1 * TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        // P1 (alive) at (10, 1) -> East. Dist (10-3)^2 = 49.
        p1.setAlive(true);
        p1.setPosition(new Position(10 * TILE_SIZE, 1 * TILE_SIZE));

        // P2 (dead) at (0, 0) -> West. Dist (0-3)^2 + (0-1)^2 = 10.
        p2.setAlive(false);
        p2.setLives(0);
        p2.setPosition(new Position(-1000, -1000)); // Effectively (0,0)

        // Update
        controller.updateGameState(state, new ArrayList<>());

        // Ghost should flee from P1 (move West), but will likely flee from P2 (move East)
        assertEquals("Frightened ghost should flee from the only alive player (West)", Direction.WEST, blinky.getDirection());
    }

    @Test
    public void testGhostChangesTargetImmediatelyWhenPlayerDies() {
        Player p1 = state.players().get(0);
        Player p2 = state.players().get(1);
        Ghost blinky = state.ghosts().stream().filter(g -> g.getType() == GhostType.RED).findFirst().get();

        // Ghost at (3, 1)
        blinky.setPosition(new Position(3 * TILE_SIZE, 1 * TILE_SIZE));
        blinky.setDirection(Direction.SOUTH);

        // P1 (alive) at (2, 1) -> West (Nearer)
        p1.setAlive(true);
        p1.setPosition(new Position(2 * TILE_SIZE, 1 * TILE_SIZE));

        // P2 (alive) at (10, 1) -> East (Further)
        p2.setAlive(true);
        p2.setPosition(new Position(10 * TILE_SIZE, 1 * TILE_SIZE));

        // First update: should target P1 (West)
        // Wait, I need to make sure it targets P1. 
        // findNearestPlayer uses distance. (2-3)^2 = 1. (10-3)^2 = 49.
        
        // Now kill P1 in the same tick or just before update
        p1.setAlive(false);
        p1.setLives(0);
        p1.setPosition(new Position(-1000, -1000));

        // Update
        controller.updateGameState(state, new ArrayList<>());

        // Should now target P2 (East)
        assertEquals("Ghost should immediately retarget to P2 after P1 dies", Direction.EAST, blinky.getDirection());
    }
}
