package com.example.ServerLogic;

import com.example.model.*;

import org.jspace.ActualField;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.*;

public class Lobby implements Runnable{

    private SpaceRepository rep;
    private int nrOfPlayers;
    private LobbyActionHandler actionHandler;
    private int lobbyID;
    private long timeOfCreation;

    public Lobby(SpaceRepository rep, int nrOfPlayers, int lobbyID, long timeOfCreation){
        this.lobbyID = lobbyID;
        this.rep = rep;
        this.nrOfPlayers = nrOfPlayers;
        this.actionHandler = new LobbyActionHandler(rep, lobbyID);
        this.timeOfCreation = timeOfCreation;
    }

    @Override
    public void run(){
        Thread actionThread = new Thread(actionHandler);
        actionThread.setDaemon(true);
        actionThread.start();

        Space sync = rep.get(lobbyID+"sync");

        for(int i = 0; i < nrOfPlayers; i++){
            try {
                sync.put(i, nrOfPlayers);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < nrOfPlayers; i++){
            try {
                sync.get(new ActualField(i), new ActualField("OK"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            sync.put("START");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getTimeOfCreation(){
        return timeOfCreation;
    }

    public int getLobbyID(){
        return lobbyID;
    }

    public void stop(){
        actionHandler.stop();
    }

}