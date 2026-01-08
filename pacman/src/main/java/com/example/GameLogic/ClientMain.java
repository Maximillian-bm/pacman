package com.example.GameLogic;

import com.example.GameLogic.ClientThreads.KeyHandler;
import com.example.GameLogic.ClientThreads.Reader;
import com.example.UI.UI;
import com.example.model.*;
import javafx.application.Application;
import com.example.GameLogic.ClientThreads.KeyHandler;
import java.security.Key;

import org.jspace.PileSpace;

public class ClientMain {
    public static int nrOfActions = 0;
    public static int clock = 0;
    
    public static void main(String[] args) {
        Reader reader = new Reader();
        KeyHandler keyHandler = new KeyHandler();
        reader.run();
        keyHandler.run();
        Application.launch(UI.class, args);
    }
}
