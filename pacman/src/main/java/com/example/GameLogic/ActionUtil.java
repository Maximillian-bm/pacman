package com.example.GameLogic;

import com.example.model.Action;

public class ActionUtil {

    public static Action convertObjToAction(Object[] t) {
        assert t.length >= 3 : "Can not convert to Action";
        int index = (int) t[0];
        int playerId = (int) t[1];
        int clock = (int) t[2];
        int move = ((t.length == 4) ? null : (int) t[3]);
        return new Action(index, playerId, clock, move);
    }

}
