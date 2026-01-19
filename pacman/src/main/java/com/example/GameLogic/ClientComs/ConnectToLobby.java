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

    private Reader reader;

    private Space space1;

    private boolean isLobbyOpen = true;

    public void createLobby(int nrOfPlayers) throws Exception {
        try {
            space1 = new RemoteSpace(URIUtil.getSpace1URI(Constants.REMOTE_PUBLIC_URI));
            lobbyID = (int) space1.get(new FormalField(Integer.class), new ActualField(0), new ActualField("FREE"))[0];

            space1.put(lobbyID, nrOfPlayers, "CREATE");
            space1.get(new ActualField(lobbyID), new ActualField(nrOfPlayers), new ActualField("OK"));

            sync = new RemoteSpace(URIUtil.getSyncURI(Constants.REMOTE_PUBLIC_URI, lobbyID));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class), new ActualField("PLAYERID"));

            playerID = (int) t[0];
            this.nrOfPlayers = (int) t[1];

            System.out.println("Created lobby with ID: "+lobbyID);

        } catch (UnknownHostException e) {
            throw new UnknownHostException("Failed to connect to server");
        } catch (NullPointerException e) {
            throw new NullPointerException("Failed to connect to server");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lobby", e);
        }
    }

    public void joinLobby(String lobbyID) throws Exception {
        try {
            space1 = new RemoteSpace(URIUtil.getSpace1URI(Constants.REMOTE_PUBLIC_URI));
            this.lobbyID = Integer.parseInt(lobbyID);

            sync = new RemoteSpace(URIUtil.getSyncURI(Constants.REMOTE_PUBLIC_URI, this.lobbyID));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class), new ActualField("PLAYERID"));

            playerID = (int) t[0];
            nrOfPlayers = (int) t[1];
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid lobby ID: " + lobbyID);
        } catch (UnknownHostException e) {
            throw new UnknownHostException("Failed to connect to server");
        } catch (NullPointerException e) {
            throw new NullPointerException("Failed to connect to server");
        } catch (IOException e) {
            throw new RuntimeException("Lobby " + lobbyID + " not found or connection failed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Join interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to join lobby: " + e.getMessage(), e);
        }
    }

    public boolean isLobbyOpen() {
        if(!isLobbyOpen)return false;
        try {
            Object[] t = space1.queryp(new ActualField(lobbyID), new ActualField(0), new ActualField("FREE"));
            if(t != null){
                isLobbyOpen = false;
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void joinLobby(String lobbyID, long timeoutMs) throws java.util.concurrent.TimeoutException {
        throw new UnsupportedOperationException("TDD: Implement join with timeout.");
    }

    public void leaveLobby() {
        throw new UnsupportedOperationException("TDD: Implement this method to handle player disconnection.");
    }

    public Object getGameState() {
        throw new UnsupportedOperationException("TDD: Implement this method to retrieve current game state for testing.");
    }

    public boolean isPlayerInGame(int playerID) {
        throw new UnsupportedOperationException("TDD: Implement this method to check if a player is currently in the active game.");
    }

    public void startGame() {
        try {

            reader = new Reader(lobbyID);
            Thread t = new Thread(reader);
            t.setDaemon(true);
            t.start();

            sync.put(playerID, nrOfPlayers, "OK");
            sync.query(new ActualField(0), new ActualField(nrOfPlayers), new ActualField("START"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    public void quit() {
        reader.stop();
        try {
            sync.put(playerID, nrOfPlayers, "QUIT");
        } catch (Exception e) {
            return;
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
