package com.example.GameLogic;

import org.jspace.SpaceRepository;

public class Constands {
    public final static String SPACE_URI = "tcp://127.0.0.1:9001/?rep";
    public final static String REMOTE_URI = "tcp://XXX.XXX.X.XXX/?rep";
    public final static SpaceRepository rep = new SpaceRepository();
}
