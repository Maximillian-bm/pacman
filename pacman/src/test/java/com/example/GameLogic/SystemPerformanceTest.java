package com.example.GameLogic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.common.BaseTest;
import com.example.model.Action;
import com.example.model.GameState;
import com.example.model.Ghost;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Game Performance, Scalability and Concurrency Tests")
public class SystemPerformanceTest extends BaseTest {

    private ClientGameController controller;
    private GameState initialState;

    @Override
    protected long getOptimalTimeoutMillis() {
        return 1000;
    }

    @BeforeEach
    public void setUp() {
        controller = new ClientGameController();
        initialState = controller.initializeGameState(4);
    }

    @Test
    @DisplayName("Game update loop should be fast enough for high FPS")
    public void testUpdateLoopSmoothness() {
        int iterations = 5000;
        long startTime = System.nanoTime();

        GameState currentState = initialState;
        List<Action> actions = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            currentState = controller.updateGameState(currentState, actions);
        }

        long endTime = System.nanoTime();
        long totalTimeNs = endTime - startTime;
        double averageTimeMs = (totalTimeNs / (double) iterations) / 1_000_000.0;

        assertTrue(averageTimeMs < 1.0, "Game logic update took too long: " + averageTimeMs + "ms");
        assertTrue(currentState.getClock() > initialState.getClock(), "Clock should have advanced");
        assertNotEquals(initialState.getPlayers().getFirst().getPosition().x, 
            currentState.getPlayers().getFirst().getPosition().x, 0.001, "Player position should have changed");
    }

    @Test
    @DisplayName("Catch-up mechanism should resimulate lag frames within latency limits")
    public void testCatchUpLag() {
        int lagFrames = 60;
        int targetClock = initialState.getClock() + lagFrames;

        long startTime = System.nanoTime();
        GameState nextState = controller.updateGameStateFor(initialState, targetClock);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        assertTrue(durationMs < 20, "Catch-up mechanism is too slow, UI will freeze. Took: " + durationMs + "ms");
        assertEquals(targetClock, nextState.getClock(), "State clock should match target after catch-up");
    }

    @Test
    @DisplayName("Collision detection should scale efficiently with many entities")
    public void testStressTestEntityCollision() {
        GameState stressState = controller.initializeGameState(1);
        try {
            for (int i = 0; i < 100; i++) {
                Ghost g = new Ghost(com.example.model.GhostType.RED);
                g.setPosition(new com.example.model.Position(100, 100));
                stressState.getGhosts().add(g);
            }
        } catch (UnsupportedOperationException e) {
            return;
        }

        long startTime = System.nanoTime();
        stressState = controller.updateGameState(stressState, new ArrayList<>());
        long endTime = System.nanoTime();

        double durationMs = (endTime - startTime) / 1_000_000.0;
        assertTrue(durationMs < 5.0, "Collision detection scaling is poor: " + durationMs + "ms");
    }

    @Test
    @DisplayName("System should handle multiple concurrent lobbies starting and joining")
    public void testConcurrentLobbyLifecycles() throws InterruptedException {
        int playersPerLobby = 3;
        ConnectToLobby hostA = new ConnectToLobby();
        ConnectToLobby hostB = new ConnectToLobby();

        hostA.createLobby(playersPerLobby);
        hostB.createLobby(playersPerLobby);

        int lobbyIdA = hostA.getLobbyID();
        int lobbyIdB = hostB.getLobbyID();

        assertNotEquals(lobbyIdA, lobbyIdB, "Lobbies should have different IDs");

        List<Thread> threads = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();

        Runnable joinA = () -> {
            try {
                ConnectToLobby p = new ConnectToLobby();
                p.joinLobby(String.valueOf(lobbyIdA));
                assertTrue(p.getPlayerID() > 0);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        };

        Runnable joinB = () -> {
            try {
                ConnectToLobby p = new ConnectToLobby();
                p.joinLobby(String.valueOf(lobbyIdB));
                assertTrue(p.getPlayerID() > 0);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        };

        for (int i = 0; i < playersPerLobby - 1; i++) {
            Thread tA = new Thread(joinA);
            Thread tB = new Thread(joinB);
            threads.add(tA);
            threads.add(tB);
            tA.start();
            tB.start();
        }

        for (Thread t : threads) {
            t.join(5000);
        }

        if (!exceptions.isEmpty()) {
            fail("Exceptions during concurrent joining: " + exceptions.getFirst().getMessage());
        }

        Thread startA = new Thread(hostA::startGame);
        Thread startB = new Thread(hostB::startGame);

        startA.start();
        startB.start();

        startA.join(2000);
        startB.join(2000);

        assertTrue(startA.isAlive() || !exceptions.isEmpty());
    }

    @Test
    @DisplayName("Lobby should remain stable under rapid join/leave churn")
    public void testJoinLeaveLoopInLobby() {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(5);
        String lobbyId = String.valueOf(host.getLobbyID());

        for (int i = 0; i < 10; i++) {
            ConnectToLobby p = new ConnectToLobby();
            p.joinLobby(lobbyId);
            assertTrue(p.getPlayerID() > 0, "Player should connect");
            p.leaveLobby();
        }
    }

    @Test
    @DisplayName("Different lobbies should maintain isolated game states")
    public void testMixedGameStatesInMultipleLobbies() {
        ConnectToLobby host1 = new ConnectToLobby();
        host1.createLobby(4);

        ConnectToLobby host2 = new ConnectToLobby();
        host2.createLobby(2);
        ConnectToLobby p2_2 = new ConnectToLobby();
        p2_2.joinLobby(String.valueOf(host2.getLobbyID()));
        Thread t2 = new Thread(host2::startGame);
        t2.start();

        ConnectToLobby host3 = new ConnectToLobby();
        host3.createLobby(10);

        ConnectToLobby traveler = new ConnectToLobby();
        traveler.joinLobby(String.valueOf(host3.getLobbyID()));
        traveler.leaveLobby();

        traveler.joinLobby(String.valueOf(host1.getLobbyID()));
        assertEquals(host1.getLobbyID(), traveler.getLobbyID(), "Traveler should now be in Lobby 1");
    }

    @Test
    @DisplayName("Player should be able to rejoin the same lobby rapidly")
    public void testRapidRejoiningSameLobby() {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();

        for (int i = 0; i < 5; i++) {
            p2.joinLobby(lobbyId);
            int pid = p2.getPlayerID();
            assertTrue(pid > 0);
            p2.leaveLobby();
        }
    }

    @Test
    @DisplayName("Leaving while a game is starting should be handled safely")
    public void testLeaveWhileStarting() throws InterruptedException {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);

        Thread starter = new Thread(host::startGame);
        starter.start();

        Thread.sleep(100);

        p2.leaveLobby();

        assertFalse(host.isPlayerInGame(p2.getPlayerID()), "Player 2 should be removed from game state");
    }

    @Test
    @DisplayName("Late joiners should be blocked from entering already started games")
    public void testLateJoinerToStartedGame() throws InterruptedException {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(1);
        Thread t = new Thread(host::startGame);
        t.start();
        t.join(1000);

        ConnectToLobby intruder = new ConnectToLobby();
        try {
            intruder.joinLobby(String.valueOf(host.getLobbyID()));
            fail("Should not be able to join a started game as regular player");
        } catch (Exception _) { }
    }
}
