package com.example.GameLogic.ClientComs;

import static com.example.model.Constants.REMOTE_PUBLIC_URI;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.model.Action;
import com.example.GameLogic.ActionUtil;
import com.example.GameLogic.URIUtil;
import com.example.model.Constants;

public class Reader implements Runnable {

    private int lobbyID;
    private boolean running = true;

    public Reader(int lobbyID){
        this.lobbyID = lobbyID;
    }

    public boolean isConnected() {
        throw new UnsupportedOperationException("TDD: Implement check for active connection.");
    }

    //Continuesly reads from the remote clean actions space and updates the static list of clean actions
    @Override
    public void run() {
        int nrOfActions = 0;
        try {
            Space remoteActions = new RemoteSpace(URIUtil.getCleanActionURI(Constants.REMOTE_PUBLIC_URI, lobbyID));
            while(running) {
                Action action = ActionUtil.convertObjToAction(remoteActions.query(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new ActualField(nrOfActions)));
                Constants.cleanActions.addAction(action);
                nrOfActions++;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            Constants.lobbyClosed = true;
        }
    }

    public void stop(){
        running = false;
    }
 
}