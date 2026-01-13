package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionList {
    private final Map<Integer, List<Action>> actionsMap = new HashMap<>();
    private final List<Action> actionsList = new ArrayList<>();
    private int nrOfActionsCalled = 0;
    private boolean missedAction = false;
    private int playerID = 0;

    public void addAction(Action action) {
        actionsList.add(action);
        actionsMap
            .computeIfAbsent(action.getClock(), k -> new ArrayList<>())
            .add(action);
    }

    public List<Action> getActions(int clock) {
        List<Action> actionsOfClock = actionsMap.containsKey(clock)
            ? List.copyOf(actionsMap.get(clock))
            : List.of();
        if(!actionsOfClock.isEmpty() && actionsOfClock.get(0).getIndex() > nrOfActionsCalled){
            missedAction = true;
            if(actionsList.get(nrOfActionsCalled).getPlayerId() == playerID){
                Constants.actionOffset++;
                System.out.println("you missed your own action, action offset is now set to "+Constants.actionOffset+"game ticks");
            }else{
                Constants.timeOffset = Constants.timeOffset + (500000000/Constants.TARGET_FPS);
                double temp = Constants.timeOffset/(1000000000/Constants.TARGET_FPS);
                System.out.println(Constants.timeOffset);
                System.out.println("you missed another players action, your clock offset is now set to "+temp+" game ticks");
            }
        }else if(!actionsOfClock.isEmpty()){
            nrOfActionsCalled = actionsOfClock.getLast().getIndex() + 1;
        }
        return actionsOfClock;
    }

    public boolean missedAction(){
        return missedAction;
    }

    public void fixedMissedAction(){
        missedAction = false;
    }

    public void setPlayerID(int playerID){
        this.playerID = playerID;
    }
}
