package com.example.ServerLogic;

import com.example.model.*;
import com.example.GameLogic.*;

public class Lobby {
 
    private GameState saveState;
    private ServerGameController serverGameController;
    private int id;

    public static void main(String[] arg) {
        LobbyActionHandler lobbyActionHandler = new LobbyActionHandler();
        lobbyActionHandler.launch();
    }
}