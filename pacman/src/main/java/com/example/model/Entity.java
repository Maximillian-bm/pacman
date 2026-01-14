package com.example.model;

import lombok.Getter;
import lombok.Setter;

public abstract class Entity {
    @Getter @Setter
    protected Position position;
    @Getter @Setter
    protected Direction direction;

    public void update() {
        //TODO
    }

    public double distanceTo(Entity e) {
        return Math.sqrt(Math.pow(position.x - e.position.x, 2) + Math.pow(position.y - e.position.y, 2));
    }
}