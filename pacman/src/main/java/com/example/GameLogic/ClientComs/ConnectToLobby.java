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

    private String gameURI;

    private int playerID;

    private int lobbyID;

    private Space sync;

    public void createLobby(int nrOfPlayers) {
        try {
            Space space1 = new RemoteSpace(Constants.REMOTE_PUBLIC_URI);
            gameURI = (String) space1.get(new FormalField(String.class))[0];
            space1.put(gameURI, nrOfPlayers);
            sync = new RemoteSpace(URIUtil.getSyncURI(gameURI));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class));
            playerID = (int) t[0];
            this.nrOfPlayers = (int) t[1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void joinLobby(String lobbyID) {
        try {
            this.lobbyID = Integer.parseInt(lobbyID);
            gameURI = "tcp://127.0.0.1:50000/"+lobbyID+"/?keep";
            sync = new RemoteSpace(URIUtil.getSyncURI(gameURI));
            Object[] t = sync.get(new FormalField(Integer.class), new FormalField(Integer.class));
            playerID = (int) t[0];
            nrOfPlayers = (int) t[1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        try {

            Reader reader = new Reader();
            Thread t = new Thread(reader);
            t.setDaemon(true);
            t.start();
            
            sync.put(playerID, "OK");
            sync.query(new ActualField("START"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    public int getLobbyID(){
        return lobbyID;
    }
}
