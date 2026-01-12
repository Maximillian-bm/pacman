package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

import static com.example.model.Maps.map1;

public class Constants {
    public final static int NR_OF_LOBBYS_CAP = 100;
    public final static ActionList cleanActions = new ActionList();
    public final static String REMOTE_PUBLIC_URI = "tcp://pacman.maximillian.info:50000/?keep";
    public final static String LOCAL_GATE = "tcp://192.168.1.112:50000/?keep";

    public final static int TILE_SIZE = 48;
    public final static int PLAYER_LIVES = 3;
    public final static double PLAYER_SPEED = 250;

    public final static int INIT_SCREEN_WIDTH = map1[0].length * TILE_SIZE;
    public final static int INIT_SCREEN_HEIGHT = map1.length * TILE_SIZE;
    public final static long TARGET_FPS = 30;
    public static final double CENTER_EPS_PX = 1.5;

    //Ghost constants
    public final static double FRIGHTENED_DURATION_SEC = 8.0;
    


}
