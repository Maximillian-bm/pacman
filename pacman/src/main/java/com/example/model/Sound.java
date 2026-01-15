package com.example.model;

public enum Sound {
    CREDIT("sounds/01_credit_sound.mp3"),
    START_MUSIC("sounds/02_start_music.mp3"),
    EAT_DOT("sounds/03_pacman_eating_The_pacdots.mp3"),
    TURNING_CORNER("sounds/04_pacman_turning_the_corner_while_eating_the_pacdots.mp3"),
    EXTEND("sounds/05_extend_sound.mp3"),
    GHOST_NORMAL("sounds/06_ghost_normal_move.mp3"),
    GHOST_SPURT_1("sounds/07_ghost_spurt_move1.mp3"),
    GHOST_SPURT_2("sounds/08_ghost_spurt_move2.mp3"),
    GHOST_SPURT_3("sounds/09_ghost_spurt_move3.mp3"),
    GHOST_SPURT_4("sounds/10_ghost_spurt_move4.mp3"),
    EAT_FRUIT("sounds/11_pacman_eating_the_fruit.mp3"),
    GHOST_BLUE("sounds/12_ghost_turn_to_blue.mp3"),
    EAT_GHOST("sounds/13_pacman_eating_the_ghost.mp3"),
    GHOST_HOME("sounds/14_ghost_return_to_home.mp3"),
    FAIL("sounds/15_fail.mp3"),
    COFFEE_BREAK("sounds/16_coffee_break_music.mp3");

    private final String path;

    Sound(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
