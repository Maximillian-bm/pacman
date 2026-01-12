package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionList {
    private final Map<Integer, List<Action>> actions = new HashMap<>();

    public void addAction(Action action) {
        actions
            .computeIfAbsent(action.getClock(), k -> new ArrayList<>())
            .add(action);
    }

    public List<Action> getActions(int clock) {
        return actions.containsKey(clock)
            ? List.copyOf(actions.get(clock))
            : List.of();
    }
}
