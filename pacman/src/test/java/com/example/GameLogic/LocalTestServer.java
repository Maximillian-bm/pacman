package com.example.GameLogic;

import com.example.ServerLogic.ServerController;

public class LocalTestServer {
    private static boolean started = false;

    public static synchronized void startServer() {
        if (started) return;
        
        if (System.getProperty("remote") == null) {
            System.out.println("DEFAULT LOCAL MODE: Starting local server...");
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
