package com.example.ServerLogic;

import com.example.model.*;

import lombok.Getter;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.*;

public class Lobby implements Runnable{

    private final SpaceRepository rep;
    private final int nrOfPlayers;
    private LobbyActionHandler actionHandler;
    @Getter
    private final int lobbyID;
    @Getter
    private final long timeOfCreation;
    private final boolean[] readyForReplay;
    private Space sync;

    public Lobby(SpaceRepository rep, int nrOfPlayers, int lobbyID, long timeOfCreation){
        this.lobbyID = lobbyID;
        this.rep = rep;
        this.nrOfPlayers = nrOfPlayers;
        this.actionHandler = new LobbyActionHandler(rep, lobbyID);
        this.timeOfCreation = timeOfCreation;
        this.readyForReplay = new boolean[nrOfPlayers];
        this.sync = rep.get(lobbyID+"sync");
    }

    @Override
    public void run(){
        Thread actionThread = new Thread(actionHandler);
        actionThread.setDaemon(true);
        actionThread.start();

        for(int i = 0; i < nrOfPlayers; i++){
            try {
                sync.put(i, nrOfPlayers);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        startGame();
    }

    public void startGame(){

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

    public void replay(){
        actionHandler.stop();
        try {
			rep.get(lobbyID+"rawAction").getAll(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
            rep.get(lobbyID+"cleanAction").getAll(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        actionHandler = new LobbyActionHandler(rep, lobbyID);
        Thread actionThread = new Thread(actionHandler);
        actionThread.setDaemon(true);
        actionThread.start();
        for(int i = 0; i < nrOfPlayers; i++){
            readyForReplay[i] = false;
        }
        startGame();
    }

    public void checkForReplay() {
        try {
			Object[] t = sync.getp(new ActualField("START"));
            if(t == null){
                for(int i = 0; i < nrOfPlayers; i++){
                    Object[] t2 = sync.getp(new ActualField(i), new ActualField("REPLAY"));
                    if(t2 != null){
                        readyForReplay[i] = true;
                    }
                }
            }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
    }

    public void stop(){
        actionHandler.stopAndDeleteSpaces();
    }

    public SpaceRepository getRep(){
        return rep;
    }

    public boolean allReadyForReplay(){
        boolean r = true;
        for (boolean b : readyForReplay) {
            if(!b) r = false;
        }
        return r;
    }

    public void setToReady(int playerID){
        readyForReplay[playerID] = true;
    }
}