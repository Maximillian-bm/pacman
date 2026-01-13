package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlayerInteractionTest extends BaseTest {

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
        // Reset static state
        Ghost.setFrightenedTimerSec(0.0);
        
        // Initialize for 2 players
        initialState = controller.initializeGameState(2);
        initialState.ghosts().clear(); // Remove ghosts to isolate player interactions
    }

    @Test
    public void testPlayerEatsPlayerWithEnergizer() {
        // Setup: Player 1 has Energizer, Player 2 does not.
        Player predator = initialState.players().get(0);
        Player prey = initialState.players().get(1);

        predator.setEnergized(true);
        prey.setEnergized(false);
        
        // Position them to collide
        predator.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        prey.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        
        // Ensure they are alive and have lives
        prey.setLives(3);
        prey.setAlive(true);
        int initialPoints = predator.getPoints();

        controller.updateGameState(initialState, new ArrayList<>());

        // Expectation: Prey loses a life and dies (respawns)
        assertEquals("Prey should lose a life when eaten by energized player", 2, prey.getLives());
        assertFalse("Prey should be dead/respawning", prey.isAlive());
        
        // Expectation: Predator gains points
        assertTrue("Predator should gain points for eating player", predator.getPoints() > initialPoints);
    }

    @Test
    public void testPlayerCollisionNoEnergizer() {
        // Setup: Two players moving towards each other, no energizers.
        Player p1 = initialState.players().get(0);
        Player p2 = initialState.players().get(1);

        p1.setEnergized(false);
        p2.setEnergized(false);

        // Place P1 at (3,3) facing East
        p1.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        p1.setDirection(Direction.EAST);

        // Place P2 at (4,3) facing West (adjacent tile)
        p2.setPosition(new Position(4 * TILE_SIZE, 3 * TILE_SIZE));
        p2.setDirection(Direction.WEST);

        // Update game state
        controller.updateGameState(initialState, new ArrayList<>());

        // Expectation: Players should NOT move into each other (overlap).
        // Since they are 1 tile apart and moving towards each other, if they move, they would swap or overlap.
        // We want to enforce physical collision blocking.
        // P1 should still be at or near 3*TILE_SIZE (blocked)
        // P2 should still be at or near 4*TILE_SIZE (blocked)
        
        // Note: Exact coordinate depends on speed, but they shouldn't pass or be on same spot.
        // If they overlap, distance < TILE_SIZE.
        double distance = Math.abs(p1.getPosition().x - p2.getPosition().x);
        
        assertTrue("Players should be at least one tile apart (collision blocked). Distance: " + distance, 
                   distance >= TILE_SIZE - 1.0); // -1.0 tolerance
                   
        // Also check neither died
        assertTrue(p1.isAlive());
        assertTrue(p2.isAlive());
    }
    
    @Test
    public void testPlayerCollisionSameSpot() {
         // Setup: Force positions to overlap and see if the engine separates them or logic handles it.
         // This tests the "players aren't overlapping" requirement more statically.
        Player p1 = initialState.players().get(0);
        Player p2 = initialState.players().get(1);
        
        p1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        p2.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        
        // Try to move them out? Or just assert that this state is handled/prevented if possible.
        // If the test is about PREVENTING overlap, we should start them separate and move them.
        // But if they ARE overlapped, maybe they should be pushed apart?
        // For now, let's stick to the movement blocking test above as the primary collision test.
        // Let's add a test where one is stationary and the other runs into them.
        
        p1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE)); // Stationary
        p2.setPosition(new Position(4 * TILE_SIZE, 5 * TILE_SIZE)); // Left of P1
        p2.setDirection(Direction.EAST); // Run into P1
        
        controller.updateGameState(initialState, new ArrayList<>());
        
        double p2X = p2.getPosition().x;
        assertTrue("Moving player should be blocked by stationary player. Pos: " + p2X, 
                   p2X <= 4 * TILE_SIZE + 2.0); // Allow small movement before hit, but not full step
    }
}
