package com.example.GameLogic;

import static org.junit.Assert.assertTrue;

import com.example.common.BaseTest;
import com.example.model.Action;
import com.example.model.GameState;
import com.example.model.Ghost;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class LogicPerformanceTest extends BaseTest {

    private ClientGameController controller;
    private GameState gameState;

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
        gameState = controller.initializeGameState(1);
    }

    @Test
    public void testGameLoopPerformance() {
        // Warmup
        for (int i = 0; i < 100; i++) {
            controller.updateGameState(gameState, new ArrayList<>());
        }

        long startTime = System.nanoTime();
        int iterations = 5000;
        
        List<Action> emptyActions = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            gameState = controller.updateGameState(gameState, emptyActions);
        }
        
        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        double averageTimeMs = (durationNs / (double) iterations) / 1_000_000.0;
        
        System.out.println("Average Logic Update Time: " + averageTimeMs + " ms");
        
        // Logic should be very fast (< 0.5ms) to leave room for rendering (16ms total budget)
        assertTrue("Game logic is too slow! Avg: " + averageTimeMs + "ms", averageTimeMs < 0.5);
    }

    @Test
    public void testInputStormPerformance() {
        // Simulate receiving many inputs in a single frame (e.g. network lag catchup)
        int inputCount = 100;
        List<Action> stormActions = new ArrayList<>();
        Random rng = new Random();
        
        for (int i = 0; i < inputCount; i++) {
            stormActions.add(new Action(0, 0, rng.nextInt(4) + 1));
        }

        long startTime = System.nanoTime();
        
        gameState = controller.updateGameState(gameState, stormActions);
        
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("Input Storm Processing Time: " + durationMs + " ms");
        
        assertTrue("Input storm processing took too long: " + durationMs + "ms", durationMs < 5.0);
    }

    @Test
    public void testGhostAIStress() {
        // Add 100 ghosts to see if AI logic scales
        for (int i = 0; i < 100; i++) {
            gameState.ghosts().add(new Ghost(com.example.model.GhostType.RED));
        }

        long startTime = System.nanoTime();
        int iterations = 1000;
        
        for (int i = 0; i < iterations; i++) {
            controller.updateGameState(gameState, new ArrayList<>());
        }
        
        long endTime = System.nanoTime();
        double averageTimeMs = ((endTime - startTime) / (double) iterations) / 1_000_000.0;
        
        System.out.println("Stress Test (100 Ghosts) Avg Time: " + averageTimeMs + " ms");
        
        assertTrue("Ghost AI stress test failed! Avg: " + averageTimeMs + "ms", averageTimeMs < 2.0);
    }
}
