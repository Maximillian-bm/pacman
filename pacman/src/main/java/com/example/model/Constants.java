package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

import static com.example.model.Maps.map1;

public class Constants {
    public final static int NR_OF_LOBBYS_CAP = 4;
    public final static ArrayList<Action> cleanActions = new ArrayList<>();
    public final static String REMOTE_PUBLIC_URI = "tcp://127.0.0.1:50000/space1?keep";
    public final static String GATE_URI = "tcp://127.0.0.1:50000/?keep";

    public final static int TILE_SIZE = 48;
    public final static int PLAYER_LIVES = 3;
    public final static double PLAYER_SPEED = 250;

    public final static int TILES_WIDE = map1[0].length;
    public final static int TILES_TALL = map1.length;
    public final static int INIT_SCREEN_WIDTH = TILES_WIDE * TILE_SIZE;
    public final static int INIT_SCREEN_HEIGHT = TILES_TALL * TILE_SIZE;
    public final static int TARGET_FPS = 30;
}
