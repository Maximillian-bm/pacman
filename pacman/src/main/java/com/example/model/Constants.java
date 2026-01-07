package com.example.model;

import org.jspace.SpaceRepository;

public class Constants {
    public final static String SPACE_URI = "tcp://127.0.0.1:9001/?rep";
    public final static SpaceRepository rep = new SpaceRepository();

    // Game constants
    public final static int POINTS_PER_PELLET = 10;
}
