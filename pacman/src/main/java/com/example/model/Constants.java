package com.example.model;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

public class Constants {
    public final static ArrayList<Action> cleanActions = new ArrayList<>();
    public final static String REMOTE_URI_CLEAN = "tcp://XXX.XXX.X.XXX/cleanAction?rep";
    public final static String REMOTE_URI_RAW = "tcp://XXX.XXX.X.XXX/rawAction?rep";
    public final static String GATE_URI = "tcp://XXX.XXX.X.XXX/?rep";

    // Game constants
    public final static int PLAYER_HEALTH = 1;
    public final static int PLAYER_LIVES = 3;
}
