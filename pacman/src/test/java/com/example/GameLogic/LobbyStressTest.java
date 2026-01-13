package com.example.GameLogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.common.BaseTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class LobbyStressTest extends BaseTest {

    @Override
    protected long getOptimalTimeoutMillis() {
        return 5000;
    }

    @Test
    public void testConcurrentLobbyLifecycles() throws InterruptedException {
        // Create two lobbies simultaneously
        int playersPerLobby = 3;
        ConnectToLobby hostA = new ConnectToLobby();
        ConnectToLobby hostB = new ConnectToLobby();

        hostA.createLobby(playersPerLobby);
        hostB.createLobby(playersPerLobby);

        int lobbyIdA = hostA.getLobbyID();
        int lobbyIdB = hostB.getLobbyID();

        assertNotEquals("Lobbies should have different IDs", lobbyIdA, lobbyIdB);

        // Threads to join Lobby A
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

        // Add players to both lobbies concurrently
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
            fail("Exceptions during concurrent joining: " + exceptions.get(0).getMessage());
        }

        // Start games in both lobbies
        Thread startA = new Thread(hostA::startGame);
        Thread startB = new Thread(hostB::startGame);
        
        startA.start();
        startB.start();

        startA.join(2000);
        startB.join(2000);

        // Simple assertions to ensure we got this far without crashing
        assertTrue(startA.isAlive() || !exceptions.isEmpty()); // If it finished, great, if it's running (waiting for reader), also fine.
    }

    @Test
    public void testJoinLeaveLoopInLobby() {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(5);
        String lobbyId = String.valueOf(host.getLobbyID());

        for (int i = 0; i < 10; i++) {
            ConnectToLobby p = new ConnectToLobby();
            p.joinLobby(lobbyId);
            assertTrue("Player should connect", p.getPlayerID() > 0);
            p.leaveLobby(); // This will fail as per current implementation, which is desired
        }
    }

    @Test
    public void testLeaveWhileStarting() throws InterruptedException {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);

        // Start game in a separate thread so we can leave "during" start
        Thread starter = new Thread(host::startGame);
        starter.start();

        // Give it a tiny moment to initiate
        Thread.sleep(100);

        // Player 2 disconnects abruptly
        try {
            p2.leaveLobby();
        } catch (UnsupportedOperationException e) {
            // Expected for now, but in real logic this should trigger server cleanup
            throw e; 
        }

        assertFalse("Player 2 should be removed from game state", host.isPlayerInGame(p2.getPlayerID()));
    }

    @Test
    public void testMixedGameStatesInMultipleLobbies() throws InterruptedException {
        // Lobby 1: Just created
        ConnectToLobby host1 = new ConnectToLobby();
        host1.createLobby(4);

        // Lobby 2: Starting
        ConnectToLobby host2 = new ConnectToLobby();
        host2.createLobby(2);
        ConnectToLobby p2_2 = new ConnectToLobby();
        p2_2.joinLobby(String.valueOf(host2.getLobbyID()));
        Thread t2 = new Thread(host2::startGame);
        t2.start();

        // Lobby 3: Filling up
        ConnectToLobby host3 = new ConnectToLobby();
        host3.createLobby(10);
        
        // Player joins Lobby 3, then leaves, then joins Lobby 1
        ConnectToLobby traveler = new ConnectToLobby();
        traveler.joinLobby(String.valueOf(host3.getLobbyID()));
        traveler.leaveLobby();
        
        traveler.joinLobby(String.valueOf(host1.getLobbyID()));
        assertEquals("Traveler should now be in Lobby 1", host1.getLobbyID(), traveler.getLobbyID());
    }

    @Test
    public void testRapidRejoiningSameLobby() {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        
        // Join/Leave 5 times
        for(int i=0; i<5; i++) {
            p2.joinLobby(lobbyId);
            int pid = p2.getPlayerID();
            assertTrue(pid > 0);
            p2.leaveLobby();
            // Optional: check if we can join immediately again
        }
    }

    @Test
    public void testLateJoinerToStartedGame() throws InterruptedException {
        ConnectToLobby host = new ConnectToLobby();
        host.createLobby(1); 
        // 1 player lobby starts immediately usually
        Thread t = new Thread(host::startGame);
        t.start();
        t.join(1000);

        ConnectToLobby intruder = new ConnectToLobby();
        // Try to join started lobby
        try {
            intruder.joinLobby(String.valueOf(host.getLobbyID()));
            // Should fail or put in spectator
            fail("Should not be able to join a started game as regular player");
        } catch (Exception e) {
            // Expected exception
        }
    }
}
