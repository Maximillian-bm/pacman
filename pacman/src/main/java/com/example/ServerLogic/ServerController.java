package com.example.ServerLogic;

import java.util.*;

import org.jspace.FormalField;
import org.jspace.PileSpace;
import org.jspace.QueueSpace;
import org.jspace.RandomSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.model.Constants;

public class ServerController {
 
    private static List<Lobby> lobbys = new ArrayList<>();

    private static SpaceRepository rep = new SpaceRepository();

    private static Space space1 = new RandomSpace();

    public static void main(String[] arg){
        rep.addGate(Constants.REMOTE_PUBLIC_URI);
        rep.add("space1", space1);
        for(int i = 0; i < Constants.NR_OF_LOBBYS_CAP; i++){
            try {
                space1.put(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(true){
            try {
                Object[] lobbyInstruction = space1.get(new FormalField(Integer.class), new FormalField(Integer.class));
                int lobbyID = (int) lobbyInstruction[0];
                System.out.println("creating lobby with id: "+lobbyID);
                int nrOfPlayers = (int) lobbyInstruction[1];
                rep.add(lobbyID+"sync", new RandomSpace());
                rep.add(lobbyID+"rawAction", new QueueSpace());
                rep.add(lobbyID+"cleanAction", new PileSpace());
                Lobby lobby = new Lobby(rep, nrOfPlayers, lobbyID);
                space1.put(lobbyID, nrOfPlayers, "OK");
                lobby.start();
                lobbys.add(lobby);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}