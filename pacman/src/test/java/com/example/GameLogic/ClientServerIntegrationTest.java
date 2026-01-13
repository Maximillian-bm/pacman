package com.example.GameLogic;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.model.Constants;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class ClientServerIntegrationTest {

    @Test
    public void testCreateLobbySuccess() {
        ConnectToLobby client = new ConnectToLobby();
        client.createLobby(2);
        assertTrue("Lobby ID should be positive", client.getLobbyID() > 0);
        assertTrue("Player ID should be set (usually 0 for creator)", client.getPlayerID() >= 0);
    }

    @Test
    public void testJoinLobbySuccess() {
        // First create a lobby
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(2);
        int lobbyId = creator.getLobbyID();
        assertTrue("Failed to create lobby for join test", lobbyId > 0);

        // Then join it
        ConnectToLobby joiner = new ConnectToLobby();
        joiner.joinLobby(String.valueOf(lobbyId));
        assertEquals("Joined lobby ID should match", lobbyId, joiner.getLobbyID());
        assertTrue("Player ID should be valid", joiner.getPlayerID() >= 0);
        assertNotEquals("Players should have different IDs", creator.getPlayerID(), joiner.getPlayerID());
    }

    @Test(expected = NumberFormatException.class)
    public void testJoinLobbyInvalidIdFormat() {
        ConnectToLobby client = new ConnectToLobby();
        // This should throw an exception, but existing code likely catches it and prints stack trace
        client.joinLobby("invalid-id");
    }

    @Test
    public void testJoinNonExistentLobby() {
        ConnectToLobby client = new ConnectToLobby();
        // Assuming 999999 is not a valid lobby ID
        client.joinLobby("999999");
        // If the code swallows the error, lobbyID might remain 0 or be invalid.
        // We expect it to fail or throw, but if it fails silently:
        // verify that we are NOT in a valid state
        assertNotEquals("Should not be able to join non-existent lobby", 999999, client.getLobbyID());
    }

    @Test(timeout = 5000)
    public void testStartGameWait() throws InterruptedException {
        // Test that startGame blocks or waits correctly.
        // This requires a full lobby usually.
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1); // 1 player lobby
        
        // This might block indefinitely if logic is wrong or server doesn't respond
        Thread gameThread = new Thread(creator::startGame);
        gameThread.start();
        gameThread.join(2000); // Wait 2 seconds
        
        // If startGame works for 1 player, the thread might still be running the Reader, 
        // but we just want to ensure it didn't crash immediately.
        assertTrue(gameThread.isAlive());
    }

    @Test
    public void testMultipleLobbyCreation() {
        ConnectToLobby client1 = new ConnectToLobby();
        client1.createLobby(2);
        
        ConnectToLobby client2 = new ConnectToLobby();
        client2.createLobby(2);
        
        assertNotEquals("Lobby IDs should be unique", client1.getLobbyID(), client2.getLobbyID());
    }
    
    @Test
    public void testLobbyCapacityLimit() {
        // Create a lobby for 1 player
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1);
        
        ConnectToLobby joiner = new ConnectToLobby();
        // Try to join the 1-player lobby (which is full because creator is in it?)
        // Actually creator is player 0. If size is 1, is it full? 
        // Logic depends on server. Assuming size 1 means 1 player total.
        joiner.joinLobby(String.valueOf(creator.getLobbyID()));
        
        // This should probably fail or wait indefinitely?
        // If the code doesn't handle full lobbies gracefully, this test will fail/timeout.
        // We assume it should NOT be able to join or get a valid ID.
        assertNotEquals("Should not be able to join full lobby", creator.getLobbyID(), joiner.getLobbyID());
    }
}
