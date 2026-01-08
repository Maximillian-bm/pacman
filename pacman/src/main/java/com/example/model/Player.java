package com.example.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Player extends Entity {
    @Getter
    private final int id;
    @Getter
    private final ImageView imageView;

    @Getter
    private int
            points = 0,
            health = Constants.PLAYER_HEALTH,
            lives = Constants.PLAYER_LIVES;

    @Getter
    @Setter
    private Position position = new Position();
    @Getter
    @Setter
    private Direction direction = Direction.EAST;

    private boolean isEnergized;

    public Player(int id) {
        this.id = id;

        // Feel free to move all this:
        Image tilesetImage = new Image(Objects.requireNonNull(getClass().getResource("/tilesets/pacman-sprite-sheet.png")).toExternalForm());
        ImageView imageView = new ImageView(tilesetImage);

        // Use viewport instead of clip for proper sprite selection
        imageView.setViewport(new javafx.geometry.Rectangle2D(100, 50, 50, 50));
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);

        /* Animation guide:
         * https://frostybee.github.io/fx-course/ui/fx-images/
         */
        this.imageView = imageView;
    }

    /*
     * From: "https://pacman.fandom.com/wiki/Power_Pellet"
     * If consecutive ghosts are eaten during the same Energizer effect, they will give out 400, 800 and 1,600 point bonuses for each of the consecutive ghosts in order.
     */
    private int ghostsEatenThisEnergizer;

    public void addTilePoints(TileType tileType) {
        points += tileType.points;
    }

    public void eatGhost() {
        points += switch (ghostsEatenThisEnergizer) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            case 3 -> 1600;
            default -> 3200;
        };

        ghostsEatenThisEnergizer++;
    }

    public void loseHealth() {
        if (health > 0) health--;

        if (health <= 0 && lives > 0) {
            lives--;

            if (lives > 0) health = Constants.PLAYER_HEALTH;
        }
    }

    // Instead of calling a function/event like 'die()' in loseHealth, we can just check if a player is dead like this:
    public boolean isDead() {
        return lives <= 0;
    }
}
