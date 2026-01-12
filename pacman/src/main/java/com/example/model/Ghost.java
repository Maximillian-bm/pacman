package com.example.model;

import static com.example.model.Constants.PLAYER_SPEED;

import lombok.Getter;
import lombok.Setter;

public class Ghost extends Entity {
    @Getter
    private GhostType type;
    @Getter
    @Setter
    private Position position;
    private Direction direction;
    @Getter
    @Setter
    private static double GHOSTSPEED;
    @Getter
    @Setter
    private static boolean ghostScatterMode = true;
    @Getter
    @Setter
    private static double ghostChaseTimer = 0.0;
    @Getter
    @Setter
    private static double frightenedTimerSec = 0.0;

    public Ghost(GhostType type) {
        this.type = type;
    }
}
