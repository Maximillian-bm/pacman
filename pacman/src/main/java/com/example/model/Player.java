package com.example.model;

public class Player extends Entity {
    private int id;
    private Position position;
    private Direction direction;

    private int points;
    private int hearts;

    /*
     * From: "https://pacman.fandom.com/wiki/Power_Pellet"
     * If consecutive ghosts are eaten during the same Energizer effect, they will give out 400, 800 and 1,600 point bonuses for each of the consecutive ghosts in order.
     */
    private int ghostsEatenThisEnergizer;

    public void updateDir(Direction direction) {
        this.direction = direction;
    }

    public void eatPellet() {
        points += Constants.POINTS_PER_PELLET;
    }

    public void eatGhost() {
        points += switch (ghostsEatenThisEnergizer) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            case 3 -> 1600;
            default -> 3200;
        };

        ghostsEatenThisEnergizer++;
    }
}
