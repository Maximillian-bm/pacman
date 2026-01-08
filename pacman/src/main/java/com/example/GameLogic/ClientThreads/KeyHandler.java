package com.example.GameLogic.ClientThreads;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.GameLogic.ActionUtil;
import com.example.GameLogic.ClientMain;
import com.example.model.Action;
import com.example.model.Constants;

import javafx.scene.input.KeyCode;

public class KeyHandler implements Runnable {

    private Space rawActions;
    private final Set<KeyCode> down;

    public KeyHandler(Set<KeyCode> down) {
        this.down = down;
        try {
            this.rawActions = new RemoteSpace(Constants.REMOTE_URI_RAW);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock, 3), rawActions);
    }

    private void moveDown() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock, 4), rawActions);
    }

    private void moveLeft() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock, 1), rawActions);
    }

    private void moveRight() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock, 2), rawActions);
    }
 
}