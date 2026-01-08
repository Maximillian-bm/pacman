package com.example.model;

import org.jspace.SpaceRepository;

public class Constants {
    public final static String SPACE_URI = "tcp://127.0.0.1:9001/?rep";
    public final static SpaceRepository rep = new SpaceRepository();

    // Game constants
    public final static int TILE_SIZE = 64;
    public final static int TILE_COUNT = 14;
    public final static int PLAYER_HEALTH = 1;
    public final static int PLAYER_LIVES = 3;
}
