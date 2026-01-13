package com.example.ServerLogic;

import org.jspace.FormalField;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.ActionUtil;
import com.example.model.Action;

public class LobbyActionHandler implements Runnable{

    SpaceRepository repository;
    int lobbyID;
    boolean running = true;

    public LobbyActionHandler(SpaceRepository repository, int lobbyID){
        this.repository = repository;
        this.lobbyID = lobbyID;
    }

    @Override
    public void run() {
        Space rawActions = repository.get(lobbyID+"rawAction");
        Space cleanActions = repository.get(lobbyID+"cleanAction");
        int actionCount = 0;
        int clock = 0;
        while(running) {
            try {
                Action rawAction = ActionUtil.convertObjToAction(rawActions.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class)));
                int tempClock = rawAction.getClock();
                ActionUtil.handleRawAction(clock, actionCount, rawAction, cleanActions);
                actionCount++;
                clock = ((tempClock < clock) ? clock : tempClock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
    
}
