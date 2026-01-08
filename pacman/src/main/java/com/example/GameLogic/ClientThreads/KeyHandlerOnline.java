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

public class KeyHandlerOnline extends KeyHandler{

    private Space rawActions;

    public KeyHandlerOnline() {
        try {
            this.rawActions = new RemoteSpace(Constants.REMOTE_URI_RAW);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void move(KeyCode key) {
        if(key == KeyCode.W) moveUp();
        if(key == KeyCode.S) moveDown();
        if(key == KeyCode.A) moveLeft();
        if(key == KeyCode.D) moveRight();
    }

    @Override
    public void moveUp() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock + 2, 3), rawActions);
    }

    @Override
    public void moveDown() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock + 2, 4), rawActions);
    }

    @Override
    public void moveLeft() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock + 2, 1), rawActions);
    }

    @Override
    public void moveRight() {
        ActionUtil.registerRawAction(new Action(0, ClientMain.clock + 2, 2), rawActions);
    }
}
