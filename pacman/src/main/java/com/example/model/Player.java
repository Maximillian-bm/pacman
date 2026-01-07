package com.example.model;

public class Player extends Entity {
    private int id;
    public Position position;
    public Direction direction;

    private int points;
    private int hearts;
    private int ghostsEaten;

    public void updateDir(Direction direction) {
        this.direction = direction;
    }

    public void eatGhost() {
        points += switch (ghostsEaten) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            case 3 -> 1600;
            default -> 3200;
        };

        ghostsEaten++;
    }
}
