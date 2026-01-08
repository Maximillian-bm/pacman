package com.example.GameLogic.ClientThreads;

import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;
import javafx.scene.input.KeyCode;

public class KeyHandler implements Runnable {

    private final int playerId;

    public KeyHandler(int playerId) {
        this.playerId = playerId;
    }
    
    public void onKeyPressed(KeyCode code) {
        int move = switch (code) {
            case A, LEFT  -> 1; // WEST
            case D, RIGHT -> 2; // EAST
            case W, UP    -> 3; // NORTH
            case S, DOWN  -> 4; // SOUTH
            default -> -1;
        };

        if (move != -1) {
            Action a = new Action(playerId, ClientMain.clock, move);
            Constants.cleanActions.add(a);
            ClientMain.nrOfActions++;
        }
    }

    @Override
    public void run() {
      
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
