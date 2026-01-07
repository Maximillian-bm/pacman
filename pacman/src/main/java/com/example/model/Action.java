package com.example.model;

public class Action {
    
    private int index;
    private int playerId;
    private int clock;
    private int move;

    public Action(int index, int playerId, int clock, int move){
        this.index = index;
        this.playerId = playerId;
        this.clock = clock;
        this.move = move;
    }
}