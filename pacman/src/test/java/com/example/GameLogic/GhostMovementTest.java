package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;

import com.example.common.BaseTest;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.Position;
import com.example.model.TileType;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

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
}
