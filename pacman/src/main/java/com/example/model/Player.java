package com.example.model;

import java.util.List;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

public class Player extends Entity {

    @Getter
    private final int id;

    private int ghostsEatenThisEnergizer;

    @Getter @Setter
    private int points = 0;

    @Getter @Setter
    private int lives = Constants.PLAYER_LIVES;

    @Getter @Setter
    private Direction intendedDirection;

    @Getter @Setter
    private double respawnTimer = 0.0;

    private boolean alive;

    @Getter @Setter
    private Position spawnPosition;

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



    private static int powerOwnerId = -1;

    public static int getPowerOwnerId() {
        return powerOwnerId;
    }

    public static void setPowerOwnerId(int playerId) {
        powerOwnerId = playerId;
    }

    public static void clearPowerOwner() {
        powerOwnerId = -1;
    }

    public static boolean isAnyPowerActive() {
        return powerOwnerId != -1;
    }

    public static boolean isPowerOwner(Player p) {
        return p != null
            && p.getId() == powerOwnerId
            && p.getPowerUpTimer() > 0.0
            && p.isAlive()
            && p.getRespawnTimer() <= 0.0;
    }

    public static void assignPowerTo(Player owner) {
        if (owner == null) return;
        powerOwnerId = owner.getId();
        owner.ghostsEatenThisEnergizer = 0;
    }
    public static boolean clearPowerIfOwnerInvalid(List<Player> players) {
    if (powerOwnerId == -1) return false;

    Player owner = null;
    if (players != null) {
        for (Player p : players) {
            if (p != null && p.getId() == powerOwnerId) {
                owner = p;
                break;
            }
        }
    }
    if (owner == null
        || owner.getPowerUpTimer() <= 0.0
        || !owner.isAlive()
        || owner.getRespawnTimer() > 0.0) {

        powerOwnerId = -1;
        return true;
    }

    return false;
}

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

    public void addPoints(int points) {
        this.points += points;
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
        switch (id) {
            case 1: return Color.rgb(255, 0, 0);
            case 2: return Color.rgb(0, 255, 0);
            case 3: return Color.rgb(0, 0, 255);
            default: return Color.rgb(255, 241, 0);
        }
    }
}
