package com.example.model;

import lombok.Getter;
import lombok.Setter;

public class Ghost extends Entity {
    @Getter
    private final GhostType type;

    @Getter @Setter
    private Position spawnPosition;

    @Getter @Setter
    private double respawnTimer = 0.0;

    public Ghost(GhostType type) {
        this.type = type;
        this.spawnPosition = new Position().fromGridPosition(Maps.getGhostPosition(type));
        this.position = new Position(this.spawnPosition.x, this.spawnPosition.y);
        super.direction = Direction.WEST;
    }

    public Ghost copy() {
        Ghost copy = new Ghost(this.type);
        copy.position = this.position != null ? new Position(this.position.x, this.position.y) : null;
        copy.direction = this.direction;
        copy.respawnTimer = this.respawnTimer;
        return copy;
    }
}
