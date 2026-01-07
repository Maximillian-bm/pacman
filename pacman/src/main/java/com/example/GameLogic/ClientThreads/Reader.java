package com.example.GameLogic.ClientThreads;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.GameLogic.ClientMain;
import com.example.GameLogic.Constands;

public class Reader implements Runnable {

    @Override
    public void run() {
        try {
            Space remoteActions = new RemoteSpace(Constands.REMOTE_URI);
            Object[] t = remoteActions.query(new ActualField(ClientMain.nrOfActions), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 
}