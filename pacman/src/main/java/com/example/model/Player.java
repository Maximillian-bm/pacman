package com.example.model;

import javafx.scene.paint.Color;

import lombok.Getter;
import lombok.Setter;

public class Player extends Entity {
    @Getter
    private final int id;

    @Getter @Setter
    private int
        points = 0,
        lives = Constants.PLAYER_LIVES;

    @Getter
    @Setter
    private Position position = new Position();
    @Getter
    @Setter
    private Direction direction = Direction.EAST;

    @Getter @Setter
    private Direction intendedDirection;
    @Getter @Setter
    private double respawnTimer = 0.0;
    private boolean alive;

    @Getter @Setter
    private Position spawnPosition;
    @Getter @Setter
    private boolean isEnergized;

    public Player(int id) {
        this.id = id;
        this.alive = true;
        this.lives = Constants.PLAYER_LIVES;
        this.direction = Direction.EAST;
        this.respawnTimer = 0.0;
    }
    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }   
    /*
     * From: "https://pacman.fandom.com/wiki/Power_Pellet"
     * If consecutive ghosts are eaten during the same Energizer effect, they will give out 400, 800 and 1,600 point bonuses for each of the consecutive ghosts in order.
     */
    private int ghostsEatenThisEnergizer;

    public void addPoints(int points) {
        this.points += points;
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

    public void loseLife() {
        if (!isDead()) lives--;
    }

    // Instead of calling a function/event like 'die()' in loseHealth, we can just check if a player is dead like this:
    public boolean isDead() {
        return lives <= 0;
    }

    public Color getColor() {
        switch (id) {
            case 1: return Color.rgb(255, 0, 0);
            case 2: return Color.rgb(0, 255, 0);
            case 3: return Color.rgb(0, 0, 255);
            default: return Color.rgb(255, 241, 0);
        }
    }
}
