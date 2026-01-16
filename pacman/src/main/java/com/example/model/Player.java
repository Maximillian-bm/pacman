package com.example.model;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

public class Player extends Entity {

    @Getter
    private final int id;

    @Setter
    private int ghostsEatenThisEnergizer;

    @Getter @Setter
    private int points = 0;

    @Getter @Setter
    private int lives;

    @Getter @Setter
    private Direction intendedDirection;

    @Getter
    @Setter
    private boolean alive;

    @Getter @Setter
    private double powerUpTimer = 0.0;

    @Getter @Setter
    private double invulnerableTimer = 0.0;
    @Getter @Setter
    private boolean lostHeart = false;
    @Getter @Setter
    private boolean ateFruit = false;
    @Getter @Setter
    private boolean ateGhost = false;
    @Getter @Setter
    private boolean atePowerUp = false;

    public Player(int id) {
        super(Maps.getPlayerSpawnTile(id));
        this.id = id;
        this.alive = true;
        this.lives = Constants.PLAYER_LIVES;
        this.direction = Direction.EAST;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void resetGhostsEatenThisEnergizer() {
        this.ghostsEatenThisEnergizer = 0;
    }

    public void eatGhost() {
        ateGhost = true;
        points += switch (ghostsEatenThisEnergizer) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            case 3 -> 1600;
            default -> 3200;
        };
        ghostsEatenThisEnergizer++;
    }

    public int loseLife() {
        if (!isDead()){
            lives--;
            lostHeart = true;
        }

        return lives;
    }

    public boolean isDead() {
        return lives <= 0;
    }

    public Color getColor() {
        return switch (id) {
            case 1 -> Color.rgb(255, 0, 0);
            case 2 -> Color.rgb(0, 255, 0);
            case 3 -> Color.rgb(0, 0, 255);
            default -> Color.rgb(255, 241, 0);
        };
    }

    public Player copy() {
        Player copy = new Player(this.id);
        copy.position = this.position != null ? new Position(this.position.x, this.position.y) : null;
        copy.direction = this.direction;
        copy.points = this.points;
        copy.lives = this.lives;
        copy.intendedDirection = this.intendedDirection;
        copy.respawnTimer = this.respawnTimer;
        copy.alive = this.alive;
        copy.spawnPosition = this.spawnPosition != null ? new Position(this.spawnPosition.x, this.spawnPosition.y) : null;
        copy.powerUpTimer = this.powerUpTimer;
        copy.invulnerableTimer = this.invulnerableTimer;
        copy.ghostsEatenThisEnergizer = this.ghostsEatenThisEnergizer;
        return copy;
    }
}
