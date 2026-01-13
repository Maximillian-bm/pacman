package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

import static com.example.model.Maps.map1;

public class Constants {
    //Varibles
    public static int clock = 0;
    public static int actionOffset = 1;
    public static long timeOffset = 0;

    //Constants
    public final static long LOBBY_TTL = 300000;
    public final static int NR_OF_LOBBYS_CAP = 100;
    public final static ActionList cleanActions = new ActionList();
    public final static String REMOTE_PUBLIC_URI = "tcp://pacman.maximillian.info:50000/?keep";
    public final static String LOCAL_GATE = "tcp://192.168.1.112:50000/?keep";

    public final static int TILE_SIZE = 48;

    public final static int TILES_WIDE = map1[0].length;
    public final static int TILES_TALL = map1.length;
    public final static int INIT_SCREEN_WIDTH = TILES_WIDE * TILE_SIZE;
    public final static int INIT_SCREEN_HEIGHT = TILES_TALL * TILE_SIZE;
    
    public static final double PLAYER_RESPAWN_DELAY_SEC = 2.0;

    public final static long TARGET_FPS = 15;
    public static final double CENTER_EPS_PX = 1.5;

    // Player
    public final static int PLAYER_LIVES = 3;
    public final static double PLAYER_SPEED = 250;

    // Ghost
    public static final double GHOST_RESPAWN_DELAY_SEC  = 3.0;
    public final static double FRIGHTENED_DURATION_SEC = 8.0;
}
