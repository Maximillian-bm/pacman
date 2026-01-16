package com.example.model;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

public abstract class Entity {
    @Getter @Setter
    protected Position position;
    @Getter @Setter
    protected Direction direction;
    @Getter @Setter
    protected Position spawnPosition;
    @Getter @Setter
    protected double respawnTimer = 0.0;

    protected void initializeSpawnPosition(Pair<Integer, Integer> spawnTile) {
        this.spawnPosition = new Position().fromGridPosition(spawnTile);
        this.position = new Position(spawnPosition.x, spawnPosition.y);
    }

    public void update() {
        //TODO
    }

    public double distanceTo(Entity e) {
        return Math.sqrt(Math.pow(position.x - e.position.x, 2) + Math.pow(position.y - e.position.y, 2));
    }
}
