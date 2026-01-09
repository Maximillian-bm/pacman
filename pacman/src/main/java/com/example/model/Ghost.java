package com.example.model;

import lombok.Getter;
import lombok.Setter;

public class Ghost extends Entity {
    public GhostType type;
    @Getter
    @Setter
    public Position position;
    public Direction direction;
}
