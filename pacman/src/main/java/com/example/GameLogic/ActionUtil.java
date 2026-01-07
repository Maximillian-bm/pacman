package com.example.GameLogic;

import org.jspace.Space;

import com.example.model.Action;

public class ActionUtil {

    public static Action convertObjToAction(Object[] t) {
        assert t.length >= 3 : "Can not convert to Action";
        int playerId = (int) t[0];
        int clock = (int) t[1];
        int move = (int) t[2];
        int index = ((t.length != 4) ? -1 : (int) t[3]);
        return new Action(index, playerId, clock, move);
    }

    public static void registerRawAction(Action action, Space remoteRawActions){
        try {
            remoteRawActions.put(action.getPlayerId(), action.getClock(), action.getMove());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
