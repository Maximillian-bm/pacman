package com.example.GameLogic;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.Reader;
import com.example.common.BaseTest;
import org.junit.Test;
import java.util.concurrent.TimeoutException;

public class NetworkFailureTest extends BaseTest {

    @Override
    protected long getTimeoutSeconds() {
        return 2;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 1000;
    }

    @Test(expected = TimeoutException.class)
    public void testJoinLobbyTimeout() throws Throwable {
        // This test expects the client to throw a formatted TimeoutException 
        ConnectToLobby client = new ConnectToLobby();
        // Use the new method that supports timeout
        client.joinLobby("12345", 500); 
    }

    @Test
    public void testReaderHandleDisconnect() throws InterruptedException {
        // Test that Reader detects a disconnect and updates its state
        Reader reader = new Reader(999);
        Thread t = new Thread(reader);
        t.start();
        
        // Wait briefly for it to "start" (it will fail immediately in reality)
        t.join(100);
        
        // Assert that the reader reports disconnected
        // Note: currently isConnected throws UnsupportedOperationException, so this will fail as expected for TDD
        if (reader.isConnected()) {
            throw new RuntimeException("Reader should report disconnected");
        }
    }
}
