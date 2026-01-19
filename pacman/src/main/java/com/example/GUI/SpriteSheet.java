package com.example.GUI;

import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public enum SpriteSheet {
    OBJECT_SHEET(new Image(Objects.requireNonNull(SpriteSheet.class.getResource("/tilesets/pacman-sprite-sheet.png")).toExternalForm()), 50),
    WALL_SHEET(new Image(Objects.requireNonNull(SpriteSheet.class.getResource("/tilesets/chompermazetiles.png")).toExternalForm()), 32),

    PLAYER_YELLOW(createColoredPlayerImage(Color.rgb(255, 241, 0)), 50),   // Player 0
    PLAYER_RED(createColoredPlayerImage(Color.rgb(255, 0, 0)), 50),        // Player 1
    PLAYER_GREEN(createColoredPlayerImage(Color.rgb(0, 255, 0)), 50),      // Player 2
    PLAYER_BLUE(createColoredPlayerImage(Color.rgb(0, 0, 255)), 50),       // Player 3
    PLAYER_WHITE(createColoredPlayerImage(Color.rgb(255, 255, 255)), 50),  // Player with powerup

    WALL_COLORED(createColoredWallImage(Color.rgb(52, 0, 254)), 32);

    public final Image image;
    public final int pixelsPerTile;

    SpriteSheet(Image image, int pixelsPerTile) {
        this.image = image;
        this.pixelsPerTile = pixelsPerTile;
    }

    private static Image createColoredPlayerImage(Color color) {
        Image image = new Image(Objects.requireNonNull(SpriteSheet.class.getResource("/tilesets/pacman-sprite-sheet.png")).toExternalForm());
        int W = (int) image.getWidth();
        int H = (int) image.getHeight();
        WritableImage outputImage = new WritableImage(W, H);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        int nr = (int) (color.getRed() * 255);
        int ng = (int) (color.getGreen() * 255);
        int nb = (int) (color.getBlue() * 255);
        // Yellow (the player) - original color to replace
        int or = 255;
        int og = 241;
        int ob = 0;
        for (int y = 0; y < H; y++) {
            for (int x = 350; x < 900; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (g == og && r == or && b == ob) {
                    r = nr;
                    g = ng;
                    b = nb;
                }
                argb = (a << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, argb);
            }
        }
        return outputImage;
    }

    private static Image createColoredWallImage(Color color) {
        Image image = new Image(Objects.requireNonNull(SpriteSheet.class.getResource("/tilesets/chompermazetiles.png")).toExternalForm());
        int W = (int) image.getWidth();
        int H = (int) image.getHeight();
        WritableImage outputImage = new WritableImage(W, H);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        int nr = (int) (color.getRed() * 255);
        int ng = (int) (color.getGreen() * 255);
        int nb = (int) (color.getBlue() * 255);
        // White - original color to replace
        int or = 255;
        int og = 255;
        int ob = 255;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (g == og && r == or && b == ob) {
                    r = nr;
                    g = ng;
                    b = nb;
                }
                argb = (a << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, argb);
            }
        }
        return outputImage;
    }

    public static SpriteSheet getPlayerSheet(Color color) {
        if (color.equals(Color.rgb(255, 241, 0))) return PLAYER_YELLOW;
        if (color.equals(Color.rgb(255, 0, 0))) return PLAYER_RED;
        if (color.equals(Color.rgb(0, 255, 0))) return PLAYER_GREEN;
        if (color.equals(Color.rgb(0, 0, 255))) return PLAYER_BLUE;
        if (color.equals(Color.rgb(255, 255, 255))) return PLAYER_WHITE;
        return PLAYER_YELLOW; // Default fallback
    }
}
