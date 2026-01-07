package com.example.GameLogic;

import java.util.ArrayList;

import org.jspace.SpaceRepository;

import com.example.model.Action;

public class Constands {
    public final static ArrayList<Action> cleanActions = new ArrayList<>();
    public final static String REMOTE_URI = "tcp://XXX.XXX.X.XXX/cleanAction?rep";
    public final static SpaceRepository rep = new SpaceRepository();
}
