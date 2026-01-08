package com.example.GameLogic.ClientThreads;

import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;

import javafx.scene.input.KeyCode;

public class KeyHandler {

    public void move(KeyCode key) {
        if(key == KeyCode.W) moveUp();
        if(key == KeyCode.S) moveDown();
        if(key == KeyCode.A) moveLeft();
        if(key == KeyCode.D) moveRight();
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
