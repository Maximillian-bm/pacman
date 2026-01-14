package com.example.model;

public enum Sound {
    CREDIT("sounds/01. Credit Sound.mp3"),
    START_MUSIC("sounds/02. Start Music.mp3"),
    EAT_DOT("sounds/03. PAC-MAN - Eating The Pac-dots.mp3"),
    TURNING_CORNER("sounds/04. PAC-MAN - Turning The Corner While Eating The Pac-dots.mp3"),
    EXTEND("sounds/05. Extend Sound.mp3"),
    GHOST_NORMAL("sounds/06. Ghost - Normal Move.mp3"),
    GHOST_SPURT_1("sounds/07. Ghost - Spurt Move #1.mp3"),
    GHOST_SPURT_2("sounds/08. Ghost - Spurt Move #2.mp3"),
    GHOST_SPURT_3("sounds/09. Ghost - Spurt Move #3.mp3"),
    GHOST_SPURT_4("sounds/10. Ghost - Spurt Move #4.mp3"),
    EAT_FRUIT("sounds/11. PAC-MAN - Eating The Fruit.mp3"),
    GHOST_BLUE("sounds/12. Ghost - Turn to Blue.mp3"),
    EAT_GHOST("sounds/13. PAC-MAN - Eating The Ghost.mp3"),
    GHOST_HOME("sounds/14. Ghost - Return to Home.mp3"),
    FAIL("sounds/15. Fail.mp3"),
    COFFEE_BREAK("sounds/16. Coffee Break Music.mp3");

    private final String path;

    Sound(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
