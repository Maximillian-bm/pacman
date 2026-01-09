package com.example.ServerLogic;

import com.example.model.*;

import org.jspace.ActualField;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.*;

public class Lobby {

    private SpaceRepository lobbyRep;
    private int nrOfPlayers;
    private LobbyActionHandler actionHandler;

    public Lobby(SpaceRepository lobbyRep, int nrOfPlayers){
        this.lobbyRep = lobbyRep;
        this.nrOfPlayers = nrOfPlayers;
        this.actionHandler = new LobbyActionHandler(lobbyRep);
    }

    public void start(){
        Thread actionThread = new Thread(actionHandler);
        actionThread.setDaemon(true);
        actionThread.start();

        Space sync = lobbyRep.get("sync");

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

}