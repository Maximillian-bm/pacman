package com.example.GameLogic;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.Reader;
import org.junit.Test;
import java.util.concurrent.TimeoutException;

public class NetworkFailureTest {

    @Test(expected = TimeoutException.class, timeout = 2000)
    public void testJoinLobbyTimeout() throws Throwable {
        // This test expects the client to throw a formatted TimeoutException 
        // when it cannot connect within a reasonable time, rather than crashing 
        // or throwing a raw ConnectException (which is wrapped in IOException and caught).
        
        // Since existing code catches IOException and prints it, this test will likely 
        // finish without exception (but with invalid state), failing the 'expected' condition.
        // OR it will throw nothing and fail.
        
        ConnectToLobby client = new ConnectToLobby();
        client.joinLobby("12345"); // Non-existent lobby on non-existent server (effectively)
        
        // If the method returns silently (as it currently does on error), 
        // we throw a dummy exception to say "Hey, you didn't throw TimeoutException!"
        // But JUnit handles "expected" by failing if NO exception is thrown.
    }

    @Test(timeout = 1000)
    public void testReaderHandleDisconnect() throws InterruptedException {
        // Test that Reader detects a disconnect and stops, rather than looping forever.
        // Since Reader.run() is an infinite loop that catches exceptions, this thread will never finish.
        // The timeout=1000 will kill it and mark the test as FAILED (Timeout).
        
        Reader reader = new Reader(999);
        Thread t = new Thread(reader);
        t.start();
        t.join(); // Wait for it to finish (it won't)
    }
}
