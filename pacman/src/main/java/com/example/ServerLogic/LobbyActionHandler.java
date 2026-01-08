package com.example.ServerLogic;

import org.jspace.PileSpace;
import org.jspace.QueueSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.ActionUtil;
import com.example.model.Action;
import com.example.model.Constants;

public class LobbyActionHandler implements Runnable{

    @Override
    public void run() {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(Constants.GATE_URI);
        Space cleanActions = new PileSpace();
        Space rawActions = new QueueSpace();
        repository.add("cleanAction", cleanActions);
        repository.add("rawAction", rawActions);
        int actionCount = 0;
        int clock = 0;
        while(true) {
            try {
                Action rawAction = ActionUtil.convertObjToAction(rawActions.get());
                int tempClock = rawAction.getClock();
                actionCount = ActionUtil.handleRawAction(clock, actionCount, rawAction, cleanActions);
                clock = ((tempClock < clock) ? clock : tempClock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}
