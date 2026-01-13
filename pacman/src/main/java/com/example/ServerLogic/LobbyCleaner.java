package com.example.ServerLogic;

import java.util.ArrayList;
import java.util.List;

import org.jspace.Space;

public class LobbyCleaner implements Runnable{

    private List<Lobby> lobbys;
    private Space space1;
    private boolean running = true;
    private List<Lobby> toBeAdded = new ArrayList<>();

    public LobbyCleaner(List<Lobby> lobbys, Space space1){
        this.lobbys = lobbys;
        this.space1 = space1;
    }

    @Override
    public void run() {
        while(running){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Lobby> toBeRemoved = new ArrayList<>();
            for (Lobby lobby : lobbys) {
                System.out.println("Lobby "+lobby.getLobbyID()+" is active");
                if(System.currentTimeMillis() - lobby.getTimeOfCreation() > 300000){
                    int lobbyID = lobby.getLobbyID();
                    lobby.stop();
                    toBeRemoved.add(lobby);
                    try {
                        space1.put(lobbyID);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }              
            }
            lobbys.removeAll(toBeRemoved);
            lobbys.addAll(toBeAdded);
        }
    }

    public void stop(){
        running = false;
    }

    public void addLobby(Lobby lobby){
        toBeAdded.add(lobby);
    }
}
