package com.example.model;

import static com.example.model.Maps.map1;

public class Constants {
    //Constants
    public final static long LOBBY_TTL = 300000;
    public final static int NR_OF_LOBBYS_CAP = 100;
    public static ActionList cleanActions = new ActionList();

    public static String REMOTE_PUBLIC_URI;
    public static String LOCAL_GATE;
    static {
        if (System.getProperty("offline") != null) {
            REMOTE_PUBLIC_URI = "tcp://127.0.0.1:50000/?keep";
            LOCAL_GATE = "tcp://127.0.0.1:50000/?keep";
        } else {
            REMOTE_PUBLIC_URI = "tcp://pacman.maximillian.info:50000/?keep";
            LOCAL_GATE = "tcp://192.168.1.112:50000/?keep";
        }
    }

    public final static int TILE_SIZE = 48;

    public final static int TILES_WIDE = map1[0].length;
    public final static int TILES_TALL = map1.length;
    public final static int INIT_SCREEN_WIDTH = TILES_WIDE * TILE_SIZE;
    public final static int INIT_SCREEN_HEIGHT = TILES_TALL * TILE_SIZE;
    
    public static final double PLAYER_RESPAWN_DELAY_SEC = 2.0;

    public final static long TARGET_FPS = 20;
    public static final double CENTER_EPS_PX = 1.5;

    public final static int COUNTDOWN_DURATION_TICKS = 60;
    public static final double COLLISION_DISTANCE_PVG = TILE_SIZE/2;
    public static final double COLLISION_DISTANCE_PVP = TILE_SIZE;

    // Player
    public final static int PLAYER_LIVES = 1;
    public final static double PLAYER_SPEED = 175;
    public final static double PLAYER_SPAWN_PROTECT_SEC = 2.0;


    // Ghost
    public static final double GHOST_RESPAWN_DELAY_SEC  = 3.0;
    public final static double FRIGHTENED_DURATION_SEC = 8.0;

    //Varibles
    public static int clock = -COUNTDOWN_DURATION_TICKS;
    public static int actionOffset = 6;
    public static long timeOffset = 0;
}
