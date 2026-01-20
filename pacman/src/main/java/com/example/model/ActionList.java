package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;

public class ActionList {
    private final Map<Integer, List<Action>> actionsMap = new HashMap<>();
    private final List<Action> actionsList = new ArrayList<>();
    private int nrOfActionsCalled = 0;
    @Setter
    private int playerID = 0;

    public void addAction(Action action) {
        actionsList.add(action);
        actionsMap
            .computeIfAbsent(action.clock(), _ -> new ArrayList<>())
            .add(action);
    }

    public List<Action> getActions(int clock) {
        List<Action> actionsOfClock = actionsMap.containsKey(clock)
            ? List.copyOf(actionsMap.get(clock))
            : List.of();
        if(!actionsOfClock.isEmpty()){
            nrOfActionsCalled = actionsOfClock.getLast().getIndex() + 1;
        }
        return actionsOfClock;
    }

    public boolean missedAction(int clock){
        if(actionsList.size() > nrOfActionsCalled && actionsList.get(nrOfActionsCalled).clock() < clock){
            if(actionsList.get(nrOfActionsCalled).getPlayerId() != playerID){
                Constants.timeOffset = Constants.timeOffset + (500000000/Constants.TARGET_FPS);
                double temp = (double) Constants.timeOffset /((double) 1000000000 /Constants.TARGET_FPS);
                System.out.println("you missed another players action, your clock offset is now set to "+temp+" game ticks");
            }
            return true;
        }
        return false;
    }
}
