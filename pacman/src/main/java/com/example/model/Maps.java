package com.example.model;

import javafx.util.Pair;

import java.util.Map;

public class Maps {
    private enum Levels {
        LEVEL_1(new int[][] {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
            {1,3,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,3,1},
            {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,2,1,1,1,1,1,2,1,2,1,1,2,1},
            {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
            {1,1,1,1,2,1,1,1,0,1,0,1,1,1,2,1,1,1,1},
            {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
            {1,1,1,1,2,1,0,1,1,0,1,1,0,1,2,1,1,1,1},
            {0,0,0,0,2,0,0,1,0,0,0,1,0,0,2,0,0,0,0},
            {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
            {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
            {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
            {1,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
            {1,3,2,1,2,2,2,2,2,2,2,2,2,2,2,1,2,3,1},
            {1,1,2,1,2,1,2,1,1,1,1,1,2,1,2,1,2,1,1},
            {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
            {1,2,1,1,1,1,1,1,2,1,2,1,1,1,1,1,1,2,1},
            {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        }, Map.of(
            GhostType.RED, new Pair<>(8, 10),
            GhostType.PINK, new Pair<>(8, 10),
            GhostType.CYAN, new Pair<>(9, 10),
            GhostType.ORANGE, new Pair<>(10, 10),
            GhostType.PURPLE, new Pair<>(10, 10)
        ));

        public final int[][] tileValues;
        public final Map<GhostType, Pair<Integer, Integer>> ghostPositions;

        Levels(int[][] tileValues, Map<GhostType, Pair<Integer, Integer>> ghostPositions) {
            this.tileValues = tileValues;
            this.ghostPositions = ghostPositions;
        }
    }

    private static Levels currentLevel = Levels.LEVEL_1;

    public static TileType[][] getCurrentLevelTiles() {
        int[][] tileValues = currentLevel.tileValues;

        TileType[][] tm = new TileType[tileValues.length][tileValues[0].length];

        for (int y = 0; y < tileValues.length; y++) {
            for (int x = 0; x < tileValues[0].length; x++) {
                tm[y][x] = TileType.values()[tileValues[y][x]];
            }
        }
        return tm;
    }

    public static Pair<Integer, Integer> getGhostPosition(GhostType ghostType) {
        return currentLevel.ghostPositions.get(ghostType);
    }

    public static void incrementLevel() {
        currentLevel = Levels.values()[(currentLevel.ordinal() + 1) % Levels.values().length];
    }
}
