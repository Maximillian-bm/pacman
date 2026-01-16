package com.example.model;

public class Action {
    
    private final int playerId;
    private int clock;
    private final int move;
    private final int index;

    //Construct clean action with index
    //Meant for server
    public Action(int playerId, int clock, int move, int index){
        this.index = index;
        this.playerId = playerId;
        this.clock = clock;
        this.move = move;
    }

    //Construct raw action
    //Meant for key handler
    public Action(int playerId, int clock, int move){
        this.index = -1;
        this.playerId = playerId;
        this.clock = clock;
        this.move = move;
    }

    public int getPlayerId(){
        return playerId;
    }

    public int clock(){
        return clock;
    }

    public int getMove(){
        return move;
    }

    public int getIndex(){
        return index;
    }

    public void setClock(int clock){
        this.clock = clock;
    }
}