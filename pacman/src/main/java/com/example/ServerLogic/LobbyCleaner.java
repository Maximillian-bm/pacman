package com.example.ServerLogic;

import java.util.ArrayList;
import java.util.List;

import org.jspace.Space;

import com.example.model.Constants;

public class LobbyCleaner implements Runnable{

    private List<Lobby> lobbys;
    private Space space1;
    private boolean running = true;
    private List<Lobby> toBeAdded = new ArrayList<>();
    private boolean showActiveLobbys = false;
    private boolean closeAllLobbys = false;

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
                if(System.currentTimeMillis() - lobby.getTimeOfCreation() > Constants.LOBBY_TTL){
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
            if(showActiveLobbys){
                if(lobbys.isEmpty()){
                    System.out.println("No active lobbys");
                }else{
                    for(Lobby lobby : lobbys) {
                    System.out.println("Lobby "+lobby.getLobbyID()+" is active");
                    }
                }
                showActiveLobbys = false;
            }
            if(closeAllLobbys){
                for(Lobby lobby : lobbys) {
                System.out.println("Closing lobby "+lobby.getLobbyID());
                lobby.stop();
                toBeRemoved.add(lobby);
                }
                closeAllLobbys = false;
            }
            lobbys.removeAll(toBeRemoved);
            lobbys.addAll(toBeAdded);
            toBeAdded.removeAll(toBeAdded);
        }
    }

    public void closeAllLobbys(){
        System.out.println("Closing all lobbys lobbys");
        closeAllLobbys = true;
    }

    public void showActiveLobbys(){
        System.out.println("Loading active lobbys");
        showActiveLobbys = true;
    }

    public void stop(){
        running = false;
    }

    public void addLobby(Lobby lobby){
        toBeAdded.add(lobby);
    }
}
