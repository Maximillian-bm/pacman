package com.example.GameLogic.ClientComs;
import static com.example.model.Constants.REMOTE_PUBLIC_URI;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.GameLogic.ActionUtil;
import com.example.GameLogic.ClientMain;
import com.example.GameLogic.URIUtil;
import com.example.model.Action;
import com.example.model.Constants;

import javafx.scene.input.KeyCode;

public class KeyHandler{

    private Space rawActions;
    private int playerID;

    public KeyHandler(int lobbyID, int playerID) {
        this.playerID = playerID;
        try {
            this.rawActions = new RemoteSpace(URIUtil.getRawActionURI(Constants.REMOTE_PUBLIC_URI, lobbyID));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(KeyCode key) {
        if(key == KeyCode.W || key == KeyCode.UP) moveUp();
        if(key == KeyCode.S || key == KeyCode.DOWN) moveDown();
        if(key == KeyCode.A || key == KeyCode.LEFT) moveLeft();
        if(key == KeyCode.D || key == KeyCode.RIGHT) moveRight();
    }

    public void moveUp() {
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 3), rawActions);
    }

    public void moveDown() {
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 4), rawActions);
    }

    public void moveLeft() {
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 1), rawActions);
    }

    public void moveRight() {
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 2), rawActions);
    }
}
