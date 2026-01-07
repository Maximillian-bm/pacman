package com.example.GameLogic;

import com.example.model.*;
import javafx.application.Application;
import com.example.UI.UI;
public class ClientMain {
 
    //private jSpaceStack rawAction;
    //private jSpaceStack cleanAction;
    public int nrOfActions;
    public int clock;
    private GameState gameState;
    private GameState saveState;
    private ClientGameController gameController;
    
    public static void main(String[] args) {
        Application.launch(UI.class, args);
    }
}
