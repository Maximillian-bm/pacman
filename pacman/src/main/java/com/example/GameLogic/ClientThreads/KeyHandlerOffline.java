package com.example.GameLogic.ClientThreads;

import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;
import javafx.scene.input.KeyCode;

import java.util.Set;

public class KeyHandlerOffline implements Runnable{

    private final Set<KeyCode> down;

    public KeyHandlerOffline(Set<KeyCode> down) {
        this.down = down;
    }

    @Override
    public void run() {
        while(true){
            if (down.contains(KeyCode.W)) moveUp();
            if (down.contains(KeyCode.S)) moveDown();
            if (down.contains(KeyCode.A)) moveLeft();
            if (down.contains(KeyCode.D)) moveRight();
        }
    }

    private void moveUp() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 3));
    }

    private void moveDown() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 4));
    }

    private void moveLeft() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 1));
    }

    private void moveRight() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 2));
    }

    
}
