package com.example.GameLogic.ClientThreads;

import com.example.GameLogic.ClientMain;
import com.example.GameLogic.Constands;
import org.jspace.Space;

public class HandleActions implements Runnable {

    @Override
    public void run() {
        int nrOfCleanActions = 0;
        Space rawAction = Constands.rep.get("rawAction");
        while(true) {
            if(ClientMain.nrOfActions > nrOfCleanActions) {
                
            }
        }
    }
 
}