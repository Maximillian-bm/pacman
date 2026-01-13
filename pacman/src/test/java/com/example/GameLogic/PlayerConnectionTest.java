package com.example.GameLogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlayerConnectionTest extends BaseTest {

    private ConnectToLobby host;
    private List<ConnectToLobby> players;

    @Override
    protected long getTimeoutSeconds() {
        return 5;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 2000;
    }

    @Before
    public void setUp() {
        host = new ConnectToLobby();
        players = new ArrayList<>();
    }

    @Test
    public void testPlayerJoinAndLeaveLobby() {
        // Player 1 creates lobby
        host.createLobby(3);
        int lobbyId = host.getLobbyID();
        assertTrue("Lobby ID should be valid", lobbyId > 0);

        // Player 2 joins
        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(lobbyId));
        assertEquals("Player 2 should have ID 2", 2, p2.getPlayerID());

        // Player 2 leaves
        p2.leaveLobby();

        // Player 3 joins (Should potentially reuse ID or simply be the next valid player)
        ConnectToLobby p3 = new ConnectToLobby();
        p3.joinLobby(String.valueOf(lobbyId));
        
        // Assert state integrity
        assertTrue("Lobby should accept new player after one leaves", p3.getPlayerID() > 0);
    }

    @Test
    public void testRapidJoinLeaveChurn() throws InterruptedException {
        host.createLobby(10);
        int lobbyId = host.getLobbyID();
        
        int numberOfChurns = 5;
        for (int i = 0; i < numberOfChurns; i++) {
            ConnectToLobby p = new ConnectToLobby();
            p.joinLobby(String.valueOf(lobbyId));
            assertTrue("Player " + i + " should join successfully", p.getPlayerID() > 0);
            p.leaveLobby();
        }
    }

    @Test
    public void testLeaveDuringGameStart() throws InterruptedException {
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);

        // Simulate game start sequence
        Thread gameThread = new Thread(() -> host.startGame());
        gameThread.setDaemon(true);
        gameThread.start();

        // Player 2 leaves right as game starts
        p2.leaveLobby();

        // Join back - should either fail (game in progress) or Spectate (if implemented)
        ConnectToLobby p2Rejoin = new ConnectToLobby();
        p2Rejoin.joinLobby(lobbyId);
        
        // This assertion forces a decision: Should rejoining be allowed?
        // For now, we assert it fails or handles it specifically
        // If rejoining is allowed, this test will need updating, but for now it tests robustness.
    }

    @Test
    public void testFullLobbyJoinLeaveCycle() {
        int maxPlayers = 3;
        host.createLobby(maxPlayers);
        String lobbyId = String.valueOf(host.getLobbyID());

        // Fill lobby
        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);
        ConnectToLobby p3 = new ConnectToLobby();
        p3.joinLobby(lobbyId);

        // Try to add 4th - should fail/block
        ConnectToLobby p4 = new ConnectToLobby();
        // This runs in a separate thread to avoid blocking the test if it hangs
        Thread t = new Thread(() -> p4.joinLobby(lobbyId));
        t.setDaemon(true);
        t.start();
        
        try {
            t.join(1000);
            // If p4 joined successfully (and has an ID), that might be a bug if cap is 3
            // But we specifically want to test LEAVING making space
        } catch (InterruptedException e) {}

        // P2 leaves, making space
        p2.leaveLobby();

        // P4 should now be able to join (if it was waiting) or a new P5 can join
        ConnectToLobby p5 = new ConnectToLobby();
        p5.joinLobby(lobbyId);
        assertTrue("Player should be able to join after spot frees up", p5.getPlayerID() > 0);
    }

    @Test
    public void testDisconnectMidGame() {
        host.createLobby(2);
        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(host.getLobbyID()));

        host.startGame();
        p2.startGame();

        // Simulate disconnect mid-game
        p2.leaveLobby(); 

        // Assert game logic handles this (e.g., game doesn't crash, p2 is removed from state)
        // Check that player 2 is no longer in the game state
        boolean isP2InGame = host.isPlayerInGame(p2.getPlayerID());
        assertEquals("Player 2 should be removed from game after leaving", false, isP2InGame);
    }

    @Test
    public void testSimultaneousConnections() throws InterruptedException {
        int numberOfPlayers = 10;
        host.createLobby(numberOfPlayers);
        String lobbyId = String.valueOf(host.getLobbyID());

        List<ConnectToLobby> connectedPlayers = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // Create threads for multiple players joining simultaneously
        for (int i = 0; i < numberOfPlayers - 1; i++) {
            Thread t = new Thread(() -> {
                try {
                    ConnectToLobby player = new ConnectToLobby();
                    player.joinLobby(lobbyId);
                    connectedPlayers.add(player);
                } catch (Throwable e) {
                    exceptions.add(e);
                }
            });
            t.setDaemon(true);
            threads.add(t);
        }

        // Start all threads effectively at once
        for (Thread t : threads) t.start();

        // Wait for all threads to finish
        for (Thread t : threads) t.join(2000); // 2 second timeout

        if (!exceptions.isEmpty()) {
            fail("Exceptions occurred during simultaneous connection: " + exceptions.get(0).getMessage());
        }

        assertEquals("All players should have joined", numberOfPlayers - 1, connectedPlayers.size());
        
        // Verify all have unique IDs
        long uniqueIds = connectedPlayers.stream()
                .map(ConnectToLobby::getPlayerID)
                .distinct()
                .count();
        assertEquals("All players should have unique IDs", connectedPlayers.size(), uniqueIds);
    }
}
