package com.example.GameLogic.ClientComs;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import com.example.GameLogic.URIUtil;
import com.example.model.Constants;

public class ConnectToLobby {

    private int nrOfPlayers;

    private int playerID;

    private int lobbyID;

    private Space sync;

    public void createLobby(int nrOfPlayers) {
        try {
            Space space1 = new RemoteSpace(URIUtil.getSpace1URI(Constants.REMOTE_PUBLIC_URI));
            lobbyID = (int) space1.get(new FormalField(Integer.class))[0];
            space1.put(lobbyID, nrOfPlayers);
            space1.get(new ActualField(lobbyID), new ActualField(nrOfPlayers), new ActualField("OK"));
            sync = new RemoteSpace(URIUtil.getSyncURI(Constants.REMOTE_PUBLIC_URI, lobbyID));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class));
            playerID = (int) t[0];
            this.nrOfPlayers = (int) t[1];
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lobby", e);
        }
        System.out.println("Created lobby with ID: "+lobbyID);
    }

    public void joinLobby(String lobbyID) {
        try {
            this.lobbyID = Integer.parseInt(lobbyID);
            sync = new RemoteSpace(URIUtil.getSyncURI(Constants.REMOTE_PUBLIC_URI, this.lobbyID));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class));
            playerID = (int) t[0];
            nrOfPlayers = (int) t[1];
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid lobby ID: " + lobbyID);
        } catch (IOException e) {
            throw new RuntimeException("Lobby " + lobbyID + " not found or connection failed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Join interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to join lobby: " + e.getMessage(), e);
        }
    }

    public void quit() {
        try {
            sync.put("QUIT");
        } catch (Exception e) {
            return;
        }
    }

    public void replay() {
        try {
            sync.getp(new ActualField("START"));
            sync.put(playerID, "REPLAY");
        } catch (Exception e) {
            return;
        }
    }

    public void startGame() {
        try {

            Reader reader = new Reader(lobbyID);
            Thread t = new Thread(reader);
            t.setDaemon(true);
            t.start();

            sync.put(playerID, "OK");
            sync.query(new ActualField("START"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public int getNrOfPlayers() {
        return nrOfPlayers;
    }

    public int getPlayerID() {
        return playerID;
    }
}
