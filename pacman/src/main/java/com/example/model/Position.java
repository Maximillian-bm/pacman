package com.example.model;

import javafx.util.Pair;

import static com.example.model.Constants.TILE_SIZE;
import static com.example.model.Maps.getMap1;

public class Position {
    public double x;
    public double y;

    public Position() { }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Pair<Integer, Integer> ToGridPosition() {
        return new Pair<>(Math.max(0, (int) ((x + TILE_SIZE / 2.0) / TILE_SIZE)), Math.max(0, (int) ((y + TILE_SIZE / 2.0) / TILE_SIZE)));
    }

    public Pair<Integer, Integer> ToScreenPosition() {
        return new Pair<>((int) (x), (int) (y));
    }
}
