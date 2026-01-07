package com.example.model;

public enum TileType {
    EMPTY,
    WALL,
    POWERUP,
    PELLET, // Gives points when eating

    // FRUIT - Gives a lot of points when eating
    CHERRY,     // 100
    STRAWBERRY, // 300
    ORANGE,     // 500
    APPLE,      // 700
    MELON,      // 1000
    GALAXIAN,   // 2000
    BELL,       // 3000
    KEY         // 5000
}
