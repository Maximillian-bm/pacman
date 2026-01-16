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
        if(key == KeyCode.W || key == KeyCode.UP){
            new Thread(new Runnable() {
                public void run() {
                    moveUp();
                }
            }).start();
        };
        if(key == KeyCode.S || key == KeyCode.DOWN){
            new Thread(new Runnable() {
                public void run() {
                    moveDown();
                }
            }).start();
        };
        if(key == KeyCode.A || key == KeyCode.LEFT){
            new Thread(new Runnable() {
                public void run() {
                    moveLeft();
                }
            }).start();
        };
        if(key == KeyCode.D || key == KeyCode.RIGHT){
            new Thread(new Runnable() {
                public void run() {
                    moveRight();
                }
            }).start();
        };
    }

    public void moveUp() {
        int c = Constants.clock + Constants.actionOffset;
        System.out.println("Sending action with clock "+c);
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 3), rawActions);
    }

    public void moveDown() {
        int c = Constants.clock + Constants.actionOffset;
        System.out.println("Sending action with clock "+c);
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 4), rawActions);
    }

    public void moveLeft() {
        int c = Constants.clock + Constants.actionOffset;
        System.out.println("Sending action with clock "+c);
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 1), rawActions);
    }

    public void moveRight() {
        int c = Constants.clock + Constants.actionOffset;
        System.out.println("Sending action with clock "+c);
        ActionUtil.registerRawAction(new Action(playerID, Constants.clock + Constants.actionOffset, 2), rawActions);
    }
}
