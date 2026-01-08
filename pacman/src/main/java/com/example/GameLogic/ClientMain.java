package com.example.GameLogic;

import com.example.GameLogic.ClientThreads.KeyHandler;
import com.example.GameLogic.ClientThreads.Reader;
import com.example.UI.UI;
import javafx.application.Application;

public class ClientMain {
    public static volatile int nrOfActions = 0;
    public static volatile int clock = 0;

    public static KeyHandler keyHandler;

    public static void main(String[] args) {
        Reader reader = new Reader();

        keyHandler = new KeyHandler(0); // playerId = 0

        Thread readerThread = new Thread(reader, "Reader");
        readerThread.setDaemon(true);
        readerThread.start();

        Thread keyThread = new Thread(keyHandler, "KeyHandler");
        keyThread.setDaemon(true);
        keyThread.start();

        Application.launch(UI.class, args);
    }
}
