package com.example.model;

public enum GhostType {
    RED("Blinky"),
    PINK("Pinky"),
    CYAN("Inky"),
    ORANGE("Clyde"),
    PURPLE("Sue");

    public final String name;

    GhostType(String name) {
        this.name = name;
    }
}
