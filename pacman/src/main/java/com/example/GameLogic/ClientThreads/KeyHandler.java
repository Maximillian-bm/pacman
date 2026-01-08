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
        System.out.println("move up");
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 60, 3));
    }

    public void moveDown() {
        System.out.println("move down");
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 60, 4));
    }

    public void moveLeft() {
        System.out.println("move left");
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 60, 1));
    }

    public void moveRight() {
        System.out.println("move right");
        Constants.cleanActions.add(new Action(0, ClientMain.clock + 60, 2));
    }
}
