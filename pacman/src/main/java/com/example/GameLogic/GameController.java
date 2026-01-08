package com.example.GameLogic;

import java.util.List;
import com.example.model.Action;
import com.example.model.GameState;


public abstract class GameController {
 
    public abstract GameState updateGameState(
        GameState gameState,
        List<Action> actions
    );

    private void update() {
        //TODO
    }
}