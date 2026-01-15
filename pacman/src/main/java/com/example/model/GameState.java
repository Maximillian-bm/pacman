package com.example.model;

import java.util.*;

public record GameState(
    int clock,
    List<Player> players,
    List<Ghost> ghosts,
    TileType[][] tiles,
    Player winner,
    PowerupState powerupState
) { }
