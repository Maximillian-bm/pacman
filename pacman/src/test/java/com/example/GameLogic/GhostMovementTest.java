package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;

import com.example.common.BaseTest;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.GhostType;
import com.example.model.Player;
import com.example.model.Position;
import com.example.model.TileType;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GhostMovementTest extends BaseTest {

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
        initialState = controller.initializeGameState(1);
        Ghost.setFrightenedTimerSec(0.0);
        ClientMain.clock = 0;
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
    public void testDoubleGhostEat() {
        // Scenario: Two ghosts on the same tile, frightened. Player eats BOTH in one tick.
        initialState.ghosts().clear();
        
        Ghost g1 = new Ghost(GhostType.RED);
        g1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        g1.setRespawnTimer(0.0);
        
        Ghost g2 = new Ghost(GhostType.PINK);
        g2.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        g2.setRespawnTimer(0.0);
        
        initialState.ghosts().add(g1);
        initialState.ghosts().add(g2);
        
        Player p = initialState.players().getFirst();
        p.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        
        // Frightened mode ON
        Ghost.setFrightenedTimerSec(10.0);
        
        controller.updateGameState(initialState, new ArrayList<>());
        
        assertTrue("Ghost 1 should be eaten (respawning)", g1.getRespawnTimer() > 0);
        assertTrue("Ghost 2 should be eaten (respawning)", g2.getRespawnTimer() > 0);
    }

    @Test
    public void testGhostRespawnTimerReset() {
        // Ensure ghost respawn timer doesn't go below 0
        Ghost g = initialState.ghosts().getFirst();
        g.setRespawnTimer(0.0000001); // Almost done
        
        // Run enough updates to pass 0
        controller.updateGameState(initialState, new ArrayList<>());
        
        assertEquals("Respawn timer should cap at 0.0", 0.0, g.getRespawnTimer(), 0.00001);
    }

    @Test
    public void testSimultaneousGhostRespawn() {
        // Multiple ghosts finishing respawn timer at the exact same tick.
        for (Ghost g : initialState.ghosts()) {
            g.setRespawnTimer(0.001);
        }
        
        controller.updateGameState(initialState, new ArrayList<>());
        
        for (Ghost g : initialState.ghosts()) {
            assertEquals("All ghosts should have respawned at their spawn points", 0.0, g.getRespawnTimer(), 0.001);
            assertNotEquals("Ghost should not be at 'hidden' position", -1000, g.getPosition().x, 0.1);
        }
    }
}
