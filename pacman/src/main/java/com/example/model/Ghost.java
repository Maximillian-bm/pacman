package com.example.model;

import static com.example.model.Constants.PLAYER_SPEED;

import lombok.Getter;
import lombok.Setter;

public class Ghost extends Entity {
    @Getter
    private GhostType type;

    @Getter @Setter
    private Position spawnPosition; 

    @Getter @Setter
    private double respawnTimer = 0.0;

    public Ghost(GhostType type) {
        this.type = type;
        super.direction = Direction.WEST;
    }

    public Ghost copy() {
        Ghost copy = new Ghost(this.type);
        copy.position = this.position != null ? new Position(this.position.x, this.position.y) : null;
        copy.direction = this.direction;
        copy.spawnPosition = this.spawnPosition != null ? new Position(this.spawnPosition.x, this.spawnPosition.y) : null;
        copy.respawnTimer = this.respawnTimer;
        return copy;
    }
}
