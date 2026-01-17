package com.example.model;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public class Maps {
    private enum Level {
        LEVEL_1(new int[][] {
          // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27
     /* 0*/ {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
     /* 1*/ {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
     /* 2*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /* 3*/ {1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1},
     /* 4*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /* 5*/ {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     /* 6*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /* 7*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /* 8*/ {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1},
     /* 9*/ {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /*10*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /*11*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0},
     /*12*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0},
     /*13*/ {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0},
     /*14*/ {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
     /*15*/ {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0},
     /*16*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /*17*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0},
     /*18*/ {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /*19*/ {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /*20*/ {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
     /*21*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /*22*/ {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
     /*23*/ {1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
     /*24*/ {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /*25*/ {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
     /*26*/ {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1},
     /*27*/ {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
     /*28*/ {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
     /*29*/ {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     /*30*/ {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        }, Map.of(
            // Ghost spawn tiles
            GhostType.RED, new Pair<>(8, 10),
            GhostType.PINK, new Pair<>(8, 10),
            GhostType.CYAN, new Pair<>(9, 10),
            GhostType.ORANGE, new Pair<>(10, 10),
            GhostType.PURPLE, new Pair<>(10, 10)
        ), List.of(
            // Player spawn tiles
            new Pair<>(1, 1),
            new Pair<>(17, 1),
            new Pair<>(1, 20),
            new Pair<>(17, 20)
        ), List.of(
            // Total points for fruit spawn thresholds
            new Pair<>(16730, TileType.KEY),
            new Pair<>(10582, TileType.BELL),
            new Pair<>(6435, TileType.GALAXIAN),
            new Pair<>(4288, TileType.MELON),
            new Pair<>(2741, TileType.APPLE),
            new Pair<>(1594, TileType.ORANGE),
            new Pair<>(847, TileType.STRAWBERRY),
            new Pair<>(500, TileType.CHERRY)
        ));

        public final int[][] tileValues;
        public final Map<GhostType, Pair<Integer, Integer>> ghostSpawnTiles;
        public final List<Pair<Integer, Integer>> playerSpawnTiles;
        public final List<Pair<Integer, TileType>> fruitThresholds;

        Level(int[][] tileValues, Map<GhostType, Pair<Integer, Integer>> ghostSpawnTiles,
              List<Pair<Integer, Integer>> playerSpawnTiles, List<Pair<Integer, TileType>> fruitThresholds) {
            this.tileValues = tileValues;
            this.ghostSpawnTiles = ghostSpawnTiles;
            this.playerSpawnTiles = playerSpawnTiles;
            this.fruitThresholds = fruitThresholds;
        }

        public TileType getFruitToSpawn(int totalPoints) {
            for (Pair<Integer, TileType> threshold : fruitThresholds) {
                if (totalPoints >= threshold.getKey()) {
                    return threshold.getValue();
                }
            }
            return null;
        }
    }

    private static Level currentLevel = Level.LEVEL_1;

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

    public static TileType getFruitToSpawn(int totalPoints) {
        return currentLevel.getFruitToSpawn(totalPoints);
    }

    public static void incrementLevel() {
        currentLevel = Level.values()[(currentLevel.ordinal() + 1) % Level.values().length];
    }
}
