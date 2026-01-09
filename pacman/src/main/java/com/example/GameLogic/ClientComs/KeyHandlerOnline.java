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

public class KeyHandlerOnline extends KeyHandler{

    private Space rawActions;
    private int playerID;

    public KeyHandlerOnline(int lobbyID, int playerID) {
        this.playerID = playerID;
        try {
            this.rawActions = new RemoteSpace(URIUtil.getRawActionURI(Constants.REMOTE_PUBLIC_URI, lobbyID));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void move(KeyCode key) {
        if(key == KeyCode.W || key == KeyCode.UP) moveUp();
        if(key == KeyCode.S || key == KeyCode.DOWN) moveDown();
        if(key == KeyCode.A || key == KeyCode.LEFT) moveLeft();
        if(key == KeyCode.D || key == KeyCode.RIGHT) moveRight();
    }

    @Override
    public void moveUp() {
        ActionUtil.registerRawAction(new Action(playerID, ClientMain.clock + 2, 3), rawActions);
    }

    @Override
    public void moveDown() {
        ActionUtil.registerRawAction(new Action(playerID, ClientMain.clock + 2, 4), rawActions);
    }

    @Override
    public void moveLeft() {
        ActionUtil.registerRawAction(new Action(playerID, ClientMain.clock + 2, 1), rawActions);
    }

    @Override
    public void moveRight() {
        ActionUtil.registerRawAction(new Action(playerID, ClientMain.clock + 2, 2), rawActions);
    }
}
