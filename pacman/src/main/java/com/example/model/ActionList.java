package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionList {
    private final Map<Integer, List<Action>> actions = new HashMap<>();
    private int nrOfActionsCalled = 0;
    private boolean missedAction = false;

    public void addAction(Action action) {
        actions
            .computeIfAbsent(action.getClock(), k -> new ArrayList<>())
            .add(action);
    }

    public List<Action> getActions(int clock) {
        List<Action> actionsOfClock = actions.containsKey(clock)
            ? List.copyOf(actions.get(clock))
            : List.of();
        if(!actionsOfClock.isEmpty() && actionsOfClock.get(0).getIndex() > nrOfActionsCalled){
            missedAction = true;
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
}
