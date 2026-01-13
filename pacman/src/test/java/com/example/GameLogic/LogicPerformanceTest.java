package com.example.GameLogic;

import com.example.common.BaseTest;
import com.example.model.Action;
import com.example.model.GameState;
import com.example.model.Ghost;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class LogicPerformanceTest extends BaseTest {

    private ClientGameController controller;
    private GameState initialState;

    @Override
    protected long getTimeoutSeconds() {
        return 5;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 500;
    }

    @Before
    public void setUp() {
        controller = new ClientGameController();
        // Setup a "heavy" state if possible, or just standard
        initialState = controller.initializeGameState(4); // Max players
    }

    @Test
    public void testUpdateLoopSmoothness() {
        // Measure average update time
        int iterations = 5000;
        long startTime = System.nanoTime();

        GameState currentState = initialState;
        List<Action> actions = new ArrayList<>(); // Empty actions

        for (int i = 0; i < iterations; i++) {
            currentState = controller.updateGameState(currentState, actions);
        }

        long endTime = System.nanoTime();
        long totalTimeNs = endTime - startTime;
        double averageTimeMs = (totalTimeNs / (double) iterations) / 1_000_000.0;

        System.out.println("Average Update Time: " + averageTimeMs + " ms");

        // The UI needs to run at ~60 FPS (16ms frame time). 
        // Logic should take a fraction of that (e.g., < 2ms) to leave room for rendering.
        assertTrue("Game logic update took too long: " + averageTimeMs + "ms", averageTimeMs < 1.0);
    }

    @Test
    public void testCatchUpLag() {
        // Simulate a lag spike where the client falls behind by 60 frames (1 second)
        int lagFrames = 60;
        int startClock = initialState.clock();
        int targetClock = startClock + lagFrames;

        long startTime = System.nanoTime();
        
        // This runs the catch-up loop
        controller.updateGameStateFor(initialState, targetClock);

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("Catch-up for " + lagFrames + " frames took: " + durationMs + " ms");

        // If catching up 1 second of gameplay takes more than ~16ms, 
        // we can't catch up within a single frame, leading to a freeze or "spiral of death".
        assertTrue("Catch-up mechanism is too slow, UI will freeze. Took: " + durationMs + "ms", durationMs < 20);
    }
    
    @Test
    public void testStressTestEntityCollision() {
        // Create a state with MANY ghosts/entities if possible to test O(N^2) collisions
        // Current limit is 5 ghosts, but let's try to add more manually if the list is mutable
        
        GameState stressState = controller.initializeGameState(1);
        try {
            // Add 100 ghosts at the same position
            for (int i = 0; i < 100; i++) {
                Ghost g = new Ghost(com.example.model.GhostType.RED);
                g.setPosition(new com.example.model.Position(100, 100));
                stressState.ghosts().add(g);
            }
        } catch (UnsupportedOperationException e) {
            // List might be immutable, skip if so
            return;
        }

        long startTime = System.nanoTime();
        controller.updateGameState(stressState, new ArrayList<>());
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        assertTrue("Collision detection scaling is poor: " + durationMs + "ms", durationMs < 5.0);
    }
}