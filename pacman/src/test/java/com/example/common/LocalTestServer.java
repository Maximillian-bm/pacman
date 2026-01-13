package com.example.common;

import com.example.ServerLogic.ServerController;
import java.net.Socket;

public class LocalTestServer {
    private static boolean started = false;

    public static synchronized void startServer() {
        if (started) return;
        
        if (System.getProperty("offline") != null) {

            try (Socket ignored = new Socket("127.0.0.1", 50000)) {
                System.out.println("OFFLINE MODE: Local server detected on port 50000. Skipping auto-start.");
                started = true;
                return;
            } catch (Exception _) { }

            System.out.println("OFFLINE MODE: Starting local server...");
            Thread serverThread = new Thread(() -> ServerController.main(new String[]{}));
            serverThread.setDaemon(true);
            serverThread.start();
            started = true;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
