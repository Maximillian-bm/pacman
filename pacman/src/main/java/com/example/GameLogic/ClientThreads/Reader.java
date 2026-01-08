package com.example.GameLogic.ClientThreads;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.model.Action;
import com.example.GameLogic.ActionUtil;
import com.example.GameLogic.ClientMain;
import com.example.model.Constants;

public class Reader implements Runnable {

    //Continuesly reads from the remote clean actions space and updates the static list of clean actions
    @Override
    public void run() {
        try {
            Space remoteActions = new RemoteSpace(Constants.REMOTE_URI);
            while(true) {
                Action action = ActionUtil.convertObjToAction(remoteActions.query(new ActualField(ClientMain.nrOfActions), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class)));
                Constants.cleanActions.add(action);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 
}