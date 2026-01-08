package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

public class Constants {
    public final static ArrayList<Action> cleanActions = new ArrayList<>();
    public final static String REMOTE_URI = "tcp://XXX.XXX.X.XXX/cleanAction?rep";
    public final static SpaceRepository rep = new SpaceRepository();

    // Game constants
    public final static int PLAYER_HEALTH = 1;
    public final static int PLAYER_LIVES = 3;

    public final static int INIT_SCREEN_WIDTH = 720;
    public final static int INIT_SCREEN_HEIGHT = 720;
}
