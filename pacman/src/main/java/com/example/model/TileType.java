package com.example.model;

public enum TileType {
    EMPTY(0),
    WALL(0),

    // https://pacman.fandom.com/wiki/Pac-Dot
    PAC_DOT(10),
    /* From 'https://pacman.fandom.com/wiki/Power_Pellet':
     * "Making their debut in Pac-Man as Energizers, ..."
     */
    ENERGIZER(50),

    // Fruits
    CHERRY(100),
    STRAWBERRY(300),
    ORANGE(500),
    APPLE(700),
    MELON(1000),
    GALAXIAN(2000),
    BELL(3000),
    KEY(5000),

    WALL10(0),
    WALL11(0),
    WALL12(0);

    TileType(int points) {
        this.points = points;
    }
}
