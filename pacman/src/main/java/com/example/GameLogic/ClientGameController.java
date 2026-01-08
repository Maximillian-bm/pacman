package com.example.GameLogic;

import java.util.ArrayList;
import java.util.List;

import com.example.model.*;

import static com.example.model.Constants.TILE_SIZE;

import com.example.model.Maps;

public class ClientGameController extends GameController {

    public ClientGameController(){}

    public GameState updateGameState(GameState gameState, List<Action> actions) {
        // Initialize game and return early
        if (gameState == null) return initializeGameState();

        // Actual update loop
        GameState newGameState = new GameState(
            ClientMain.clock,
            gameState.players(),
            gameState.ghosts(),
            gameState.tiles(),
            gameState.winner()
        );

        return newGameState;
    }

    private GameState initializeGameState() {
        List<Player> players = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        TileType[][] tiles = Maps.getMap1();

        // Create test player
        Player testPlayer = new Player(1);
        testPlayer.setPosition(new Position(
            5 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        ));
        players.add(testPlayer);

        // Create test ghosts
        Ghost ghost1 = new Ghost();
        ghost1.type = GhostType.RED;
        ghost1.position = new Position(
            3 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        );
        ghosts.add(ghost1);

        Ghost ghost2 = new Ghost();
        ghost2.type = GhostType.PINK;
        ghost1.position = new Position(
            2 * TILE_SIZE + TILE_SIZE / 2.0,
            TILE_SIZE + TILE_SIZE / 2.0
        );
        ghosts.add(ghost2);

        // // Initialize tiles with a simple test level
        // for (int row = 0; row < tiles.length; row++) {
        //     for (int col = 0; col < tiles[row].length; col++) {
        //         // Border walls
        //         if (row == 0 || row == tiles.length - 1 || col == 0 || col == tiles[row].length - 1) {
        //             tiles[row][col] = TileType.WALL;
        //         }
        //         // Some interior walls
        //         else if ((row == 3 || row == 7 || row == 11 || row == 15) && col % 3 == 0) {
        //             tiles[row][col] = TileType.WALL;
        //         }
        //         // Energizers in corners
        //         else if ((row == 2 && col == 2) || (row == 2 && col == 17) ||
        //             (row == 17 && col == 2) || (row == 17 && col == 17)) {
        //             tiles[row][col] = TileType.ENERGIZER;
        //         }
        //         // Cherry in center
        //         else if (row == 10 && col == 10) {
        //             tiles[row][col] = TileType.CHERRY;
        //         }
        //         // Pac-dots everywhere else
        //         else {
        //             tiles[row][col] = TileType.PAC_DOT;
        //         }
        //     }
        // }

        return new GameState(
            ClientMain.clock,
            players,
            ghosts,
            tiles,
            null
        );
    }
}
