package com.example.model;

public enum TileType {
    EMPTY(0),
    WALL(0),
    POWERUP(50),
    PELLET(10),

    // Fruits
    CHERRY(100),
    STRAWBERRY(300),
    ORANGE(500),
    APPLE(700),
    MELON(1000),
    GALAXIAN(2000),
    BELL(3000),
    KEY(5000);

    public final int points;

    TileType(int points) {
        this.points = points;
    }
}
