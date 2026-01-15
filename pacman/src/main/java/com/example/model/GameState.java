package com.example.model;

import static com.example.model.Constants.PLAYER_SPEED;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

public class GameState {
    @Getter
    private final int clock;
    @Getter
    private final List<Player> players;
    @Getter
    private final List<Ghost> ghosts;
    @Getter
    private final TileType[][] tiles;
    @Getter
    private final Player winner;

    // Previously static fields from Ghost
    @Getter @Setter
    private double ghostSpeed = PLAYER_SPEED * 0.8;
    @Getter @Setter
    private boolean ghostScatterMode = true;
    @Getter @Setter
    private double ghostChaseTimer = 0.0;
    @Getter @Setter
    private double frightenedTimerSec = 0.0;

    // Previously static field from Player
    @Getter @Setter
    private int powerOwnerId = -1;

    public GameState(int clock, List<Player> players, List<Ghost> ghosts, TileType[][] tiles, Player winner) {
        this.clock = clock;
        this.players = players;
        this.ghosts = ghosts;
        this.tiles = tiles;
        this.winner = winner;
    }

    // Factory method to create new GameState with different clock/tiles/winner
    public GameState withClock(int newClock) {
        GameState newState = new GameState(newClock, this.players, this.ghosts, this.tiles, this.winner);
        copyMutableState(newState);
        return newState;
    }

    public GameState withTiles(TileType[][] newTiles) {
        GameState newState = new GameState(this.clock, this.players, this.ghosts, newTiles, this.winner);
        copyMutableState(newState);
        return newState;
    }

    public GameState withWinner(Player newWinner) {
        GameState newState = new GameState(this.clock, this.players, this.ghosts, this.tiles, newWinner);
        copyMutableState(newState);
        return newState;
    }

    public GameState withClockTilesWinner(int newClock, TileType[][] newTiles, Player newWinner) {
        GameState newState = new GameState(newClock, this.players, this.ghosts, newTiles, newWinner);
        copyMutableState(newState);
        return newState;
    }

    private void copyMutableState(GameState target) {
        target.ghostSpeed = this.ghostSpeed;
        target.ghostScatterMode = this.ghostScatterMode;
        target.ghostChaseTimer = this.ghostChaseTimer;
        target.frightenedTimerSec = this.frightenedTimerSec;
        target.powerOwnerId = this.powerOwnerId;
    }

    // Power owner utility methods (previously in Player class)
    public void clearPowerOwner() {
        this.powerOwnerId = -1;
    }

    public boolean isAnyPowerActive() {
        return this.powerOwnerId != -1;
    }

    public boolean isPowerOwner(Player p) {
        return p != null
            && p.getId() == this.powerOwnerId
            && p.getPowerUpTimer() > 0.0
            && p.isAlive()
            && p.getRespawnTimer() <= 0.0;
    }

    public void assignPowerTo(Player owner) {
        if (owner == null) return;
        this.powerOwnerId = owner.getId();
        owner.resetGhostsEatenThisEnergizer();
    }

    public boolean clearPowerIfOwnerInvalid() {
        if (this.powerOwnerId == -1) return false;

        Player owner = null;
        if (this.players != null) {
            for (Player p : this.players) {
                if (p != null && p.getId() == this.powerOwnerId) {
                    owner = p;
                    break;
                }
            }
        }
        if (owner == null
            || owner.getPowerUpTimer() <= 0.0
            || !owner.isAlive()
            || owner.getRespawnTimer() > 0.0) {

            this.powerOwnerId = -1;
            return true;
        }

        return false;
    }
}
