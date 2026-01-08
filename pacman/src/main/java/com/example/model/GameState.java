package com.example.model;

import java.util.*;

public class GameState {
    private int clock;

    private List<Player> players;
    private List<Ghost> ghosts;
    private TileType[][] tiles;

    private Player winner;

    public GameState(){}
}
