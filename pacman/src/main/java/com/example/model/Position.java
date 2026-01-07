package com.example.model;

import javafx.util.Pair;

public class Position {
    public double x;
    public double y;

    public Pair<Integer, Integer> ToGridPosition() {
        return new Pair<>((int)(x / 32), (int)(y / 32));
    }

    public Pair<Integer, Integer> ToScreenPosition() {
        return new Pair<>((int) (x), (int) (y));
    }
}
