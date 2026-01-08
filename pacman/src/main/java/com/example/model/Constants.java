package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

import static com.example.model.Maps.map1;

public class Constants {
    public final static boolean online = false;
    public final static ArrayList<Action> cleanActions = new ArrayList<>();
    public final static String REMOTE_URI_CLEAN = "tcp://XXX.XXX.X.XXX/cleanAction?rep";
    public final static String REMOTE_URI_RAW = "tcp://XXX.XXX.X.XXX/rawAction?rep";
    public final static String GATE_URI = "tcp://XXX.XXX.X.XXX/?rep";

    public final static int TILE_SIZE = 48;
    public final static int PLAYER_LIVES = 3;
    public final static double PLAYER_SPEED = 250;

    public final static int INIT_SCREEN_WIDTH = map1[0].length * TILE_SIZE;
    public final static int INIT_SCREEN_HEIGHT = map1.length * TILE_SIZE;
    public final static int TARGET_FPS = 30;
}
