package com.example.GameLogic;

import org.jspace.Space;

import com.example.model.Action;

public class ActionUtil {

    //Converts an object array (retrived from a space of actions) into a an action object, works for both raw and clean actions
    public static Action convertObjToAction(Object[] t) {
        assert t.length >= 3 : "Can not convert to Action";
        int playerId = (int) t[0];
        int clock = (int) t[1];
        int move = (int) t[2];
        int index = ((t.length != 4) ? -1 : (int) t[3]);
        return new Action(playerId, clock, move, index);
    }

    //Given a raw action object, registeres that in the given space
    //Meant to be used by the key handler
    public static void registerRawAction(Action action, Space remoteRawActions) {
        try {
            remoteRawActions.put(action.getPlayerId(), action.clock(), action.getMove());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Given the clock and index of the last clean action, registeres a raw action to the given clean action space
    //Meant to be used exclusivly by the server
    public static void handleRawAction(int lastActionsClock, int index, Action action, Space cleanActions) {
        action.setClock((Math.max(action.clock(), lastActionsClock)));
        try {
            cleanActions.put(action.getPlayerId(), action.clock(), action.getMove(), index);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
