package com.example.GameLogic;

import java.util.List;

import com.example.model.Action;
import com.example.model.GameState;

public class ClientGameController extends GameController {

    public ClientGameController(){}

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        GameState newGameState = new GameState(
            ClientMain.clock,
            gameState.players(),
            gameState.ghosts(),
            gameState.tiles(),
            gameState.winner()
        );

        return newGameState;
    }
}
