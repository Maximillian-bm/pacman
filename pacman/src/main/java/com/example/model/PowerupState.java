package com.example.model;

import static com.example.model.Constants.PLAYER_SPEED;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class PowerupState {
    @Getter
    @Setter
    private double ghostSpeed = PLAYER_SPEED * 0.8;
    @Getter
    @Setter
    private boolean ghostScatterMode = true;
    @Getter
    @Setter
    private double ghostChaseTimer = 0.0;
    @Getter
    @Setter
    private double frightenedTimerSec = 0.0;

    @Getter
    @Setter
    private int powerOwnerId = -1;

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
        if (owner == null)
            return;
        this.powerOwnerId = owner.getId();
        owner.resetGhostsEatenThisEnergizer();
    }

    public boolean clearPowerIfOwnerInvalid(List<Player> players) {
        if (this.powerOwnerId == -1)
            return false;

        Player owner = null;
        if (players != null) {
            for (Player p : players) {
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
