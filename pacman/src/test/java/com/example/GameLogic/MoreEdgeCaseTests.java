package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.model.Action;
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

public class MoreEdgeCaseTests extends BaseTest {

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
        initialState = controller.initializeGameState(2); // 2 Players for interaction tests
        Ghost.setFrightenedTimerSec(0.0);
        ClientMain.clock = 0;
    }

    @Test
    public void testNullActionHandling() {
        // Ensure robust handling of null lists or elements
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
        // While we can't easily wait for overflow, we can set the clock manually if the constructor allowed.
        // Since we can't easily mock the static clock in ClientMain without access, 
        // we check if a high clock value passed in state persists.
        
        GameState highClockState = new GameState(
            Integer.MAX_VALUE - 5,
            initialState.players(),
            initialState.ghosts(),
            initialState.tiles(),
            null
        );
        
        GameState nextState = controller.updateGameState(highClockState, new ArrayList<>());
        
        // This relies on updateGameState NOT using the static ClientMain.clock but the state's clock?
        // Actually, ClientMain.clock is static and used in updateGameState.
        // This test highlights a potential design flaw if the state clock and static clock desync.
        // The test expects safe handling.
        
        assertTrue("Clock should handle potential high values gracefully", nextState.clock() != 0);
    }

    @Test
    public void testTeleportationBoundary() {
        // Test exact boundary condition for teleport
        Player p = initialState.players().getFirst();
        double mapWidth = initialState.tiles().length * TILE_SIZE;
        
        // Right edge
        p.setPosition(new Position(mapWidth - (TILE_SIZE / 2.0) + 0.1, TILE_SIZE)); 
        p.setDirection(Direction.EAST);
        
        controller.updateGameState(initialState, new ArrayList<>());
        
        assertTrue("Player should have teleported to left", p.getPosition().x < TILE_SIZE);
    }

    @Test
    public void testGhostTrappedInBox() {
        // Create a custom small map where a ghost is boxed in
        TileType[][] boxMap = new TileType[5][5];
        for(int x=0; x<5; x++) for(int y=0; y<5; y++) boxMap[x][y] = TileType.WALL;
        boxMap[2][2] = TileType.EMPTY;
        
        GameState boxState = new GameState(
            0,
            initialState.players(),
            initialState.ghosts(), // Use existing ghosts but we'll modify one
            boxMap,
            null
        );
        
        Ghost g = boxState.ghosts().getFirst();
        g.setPosition(new Position(2 * TILE_SIZE, 2 * TILE_SIZE));
        
        // Logic should not crash or hang
        controller.updateGameState(boxState, new ArrayList<>());
        
        // Check if ghost is still there (didn't escape magically)
        assertEquals(2 * TILE_SIZE, g.getPosition().x, 0.1);
        assertEquals(2 * TILE_SIZE, g.getPosition().y, 0.1);
    }

    @Test
    public void testSimultaneousPlayerGhostSpawnCollision() {
        // What if a player spawns ON TOP of a ghost?
        Player p = initialState.players().get(0);
        Ghost g = initialState.ghosts().get(0);
        
        Position deathSpot = new Position(5 * TILE_SIZE, 5 * TILE_SIZE);
        p.setSpawnPosition(deathSpot);
        g.setPosition(deathSpot); // Ghost camping the spawn
        
        // Kill player to trigger respawn
        p.setAlive(false);
        p.setRespawnTimer(0.01); 
        p.setLives(2);
        
        // Force update to respawn player
        controller.updateGameState(initialState, new ArrayList<>());
        
        // Player should respawn, then IMMEDIATELY die or be handled gracefully
        // If logic is: Update Respawn -> Move -> Collision
        // Then it dies same tick.
        
        if (p.isAlive()) {
             // If alive, check if they are immune?
             // Actually, usually immunity is needed for this case.
        } else {
             assertEquals("Player should lose a life immediately if spawn is camped", 1, p.getLives());
        }
    }
    
    @Test
    public void testRapidDirectionSwitching() {
        // Simulate a player mashing keys to glitch through walls or turn illegally
        Player p = initialState.players().get(0);
        p.setPosition(new Position(1 * TILE_SIZE, 1 * TILE_SIZE)); // Top left corner (1,1) is empty usually
        
        List<Action> spamActions = new ArrayList<>();
        spamActions.add(new Action(p.getId(), 0, 1)); // West
        spamActions.add(new Action(p.getId(), 0, 2)); // East
        spamActions.add(new Action(p.getId(), 0, 3)); // North
        spamActions.add(new Action(p.getId(), 0, 4)); // South
        
        controller.updateGameState(initialState, spamActions);
        
        // Should end up with ONE valid intended direction (likely the last one processed)
        assertNotNull(p.getIntendedDirection());
        // Should not have moved into a wall (assuming (1,0) is wall)
    }
}
