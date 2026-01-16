package com.example.ServerLogic;

import java.util.ArrayList;
import java.util.List;

import org.jspace.ActualField;
import org.jspace.Space;

import com.example.model.Constants;

public class LobbyCleaner implements Runnable{

    private final List<Lobby> lobbys;
    private final Space space1;
    private boolean running = true;
    private final List<Lobby> toBeAdded = new ArrayList<>();
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
                try {
                    Space sync = lobby.getRep().get(lobby.getLobbyID()+"sync");
                    Object[] t = sync.getp(new ActualField("QUIT"));
                    if(t != null){
                        int lobbyID = lobby.getLobbyID();
                        lobby.stop();
                        toBeRemoved.add(lobby);
                        System.out.println("Closing lobby "+lobby.getLobbyID()+" due to player quiting");
                        try {
                            space1.put(lobbyID);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("tried to check lobby "+lobby.getLobbyID()+" but it was already deleted");
                    //e.printStackTrace();
                }
                if(System.currentTimeMillis() - lobby.getTimeOfCreation() > Constants.LOBBY_TTL){
                    int lobbyID = lobby.getLobbyID();
                    lobby.stop();
                    toBeRemoved.add(lobby);
                    System.out.println("Closing lobby "+lobby.getLobbyID()+" due to exeeded TTL");
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
                System.out.println("All lobbys have been displayed");
                showActiveLobbys = false;
            }
            if(closeAllLobbys){
                for(Lobby lobby : lobbys) {
                System.out.println("Closing lobby "+lobby.getLobbyID());
                lobby.stop();
                toBeRemoved.add(lobby);
                try {
                    space1.put(lobby.getLobbyID());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }
                System.out.println("All lobbys have been closed");
                closeAllLobbys = false;
            }
            lobbys.removeAll(toBeRemoved);
            lobbys.addAll(toBeAdded);
            toBeAdded.clear();
        }
    }

    public void closeAllLobbys(){
        System.out.println("Closing all lobbys...");
        closeAllLobbys = true;
    }

    public void showActiveLobbys(){
        System.out.println("Loading active lobbys...");
        showActiveLobbys = true;
    }

    public void stop(){
        running = false;
    }

    public void addLobby(Lobby lobby){
        toBeAdded.add(lobby);
    }
}
