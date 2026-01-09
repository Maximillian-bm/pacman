package com.example.ServerLogic;

import java.util.*;

import org.jspace.FormalField;
import org.jspace.RandomSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.model.Constants;

public class ServerController {
 
    private static List<Lobby> lobbys = new ArrayList<>();

    private static SpaceRepository rep = new SpaceRepository();

    private static Space space1 = new RandomSpace();

    public static void main(String[] arg){
        rep.addGate(Constants.GATE_URI);
        rep.add("space1", space1);
        for(int i = 0; i < Constants.NR_OF_LOBBYS_CAP; i++){
            try {
                space1.put("tcp://127.0.0.1:50000/"+i+"/?keep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(true){
            try {
                Object[] lobbyInstruction = space1.get(new FormalField(String.class), new FormalField(Integer.class));
                String gameURI = (String) lobbyInstruction[0];
                int nrOfPlayers = (int) lobbyInstruction[1];
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}