package com.example.model;

import lombok.Getter;

public class Ghost extends Entity {
    @Getter
    private final GhostType type;

    public Ghost(GhostType type) {
        this.type = type;
        initializeSpawnPosition(Maps.getGhostSpawnTile(type));
        this.direction = Direction.WEST;
    }

    public Ghost copy() {
        Ghost copy = new Ghost(this.type);
        copy.position = this.position != null ? new Position(this.position.x, this.position.y) : null;
        copy.direction = this.direction;
        copy.respawnTimer = this.respawnTimer;
        return copy;
    }
}
