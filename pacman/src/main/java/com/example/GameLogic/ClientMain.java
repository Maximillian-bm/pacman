package com.example.GameLogic;

import com.example.UI.UI;
import com.example.model.*;
import javafx.application.Application;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.PileSpace;

public class ClientMain {
 
    //private jSpaceStack rawAction;
    public static int nrOfActions;
    public static int clock;
    private UI UI;
    private GameState gameState;
    private GameState saveState;
    private ClientGameController gameController;
    
    public static void main(String[] args) {
        Constands.rep.addGate(Constands.SPACE_URI);
        Constands.rep.add("cleanActions", new PileSpace());
        Application.launch(UI.class, args);
    }
}
