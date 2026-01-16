package com.example.model;

import javafx.util.Pair;

import java.util.List;
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
        ), List.of(
            new Pair<>(1, 1),
            new Pair<>(17, 1),
            new Pair<>(1, 20),
            new Pair<>(17, 20)
        ));

        public final int[][] tileValues;
        public final Map<GhostType, Pair<Integer, Integer>> ghostSpawnTiles;
        public final List<Pair<Integer, Integer>> playerSpawnTiles;

        Levels(int[][] tileValues, Map<GhostType, Pair<Integer, Integer>> ghostSpawnTiles,
               List<Pair<Integer, Integer>> playerSpawnTiles) {
            this.tileValues = tileValues;
            this.ghostSpawnTiles = ghostSpawnTiles;
            this.playerSpawnTiles = playerSpawnTiles;
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

    public static Pair<Integer, Integer> getGhostSpawnTile(GhostType ghostType) {
        return currentLevel.ghostSpawnTiles.get(ghostType);
    }

    public static Pair<Integer, Integer> getPlayerSpawnTile(int playerId) {
        return currentLevel.playerSpawnTiles.get(playerId);
    }

    public static void incrementLevel() {
        currentLevel = Levels.values()[(currentLevel.ordinal() + 1) % Levels.values().length];
    }
}
