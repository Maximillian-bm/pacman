package com.example.GameLogic.ClientThreads;

import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;

import javafx.scene.input.KeyCode;

public class KeyHandler {

    public void move(KeyCode key) {
        if(key == KeyCode.W || key == KeyCode.UP) moveUp();
        if(key == KeyCode.S || key == KeyCode.DOWN) moveDown();
        if(key == KeyCode.A || key == KeyCode.LEFT) moveLeft();
        if(key == KeyCode.D || key == KeyCode.RIGHT) moveRight();
    }

    public void moveUp() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 3));
    }

    public void moveDown() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 4));
    }

    public void moveLeft() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 1));
    }

    public void moveRight() {
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 2, 2));
    }
}
