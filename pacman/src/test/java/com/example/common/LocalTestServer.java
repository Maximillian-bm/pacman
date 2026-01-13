package com.example.common;

import com.example.ServerLogic.ServerController;
import java.net.Socket;

public class LocalTestServer {
    private static boolean started = false;

    public static synchronized void startServer() {
        if (started) return;
        
        if (System.getProperty("offline") != null) {
            
            // Check if server is already running on port 50000
            try (Socket ignored = new Socket("127.0.0.1", 50000)) {
                System.out.println("OFFLINE MODE: Local server detected on port 50000. Skipping auto-start.");
                started = true; 
                return;
            } catch (Exception e) {
                // Connection failed, so server is not running. Proceed to start it.
            }

            System.out.println("OFFLINE MODE: Starting local server...");
            Thread serverThread = new Thread(() -> {
                 ServerController.main(new String[]{});
            });
            serverThread.setDaemon(true);
            serverThread.start();
            started = true;
            
            // Give it a moment to bind the port and initialize spaces
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
