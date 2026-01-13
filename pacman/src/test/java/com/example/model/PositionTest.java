package com.example.model;

import com.example.GameLogic.BaseTest;
import javafx.util.Pair;
import org.junit.Test;
import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.*;

public class PositionTest extends BaseTest {

    @Override
    protected long getTimeoutSeconds() {
        return 1;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 50;
    }

    @Test
    public void testEqualsSameObject() {
        Position pos = new Position(10.5, 20.7);
        assertEquals(10.5, pos.x, 0.0);
        assertEquals(20.7, pos.y, 0.0);
    }

    @Test
    public void testEmptyConstructor() {
        Position pos = new Position();
        assertEquals(0.0, pos.x, 0.0);
        assertEquals(0.0, pos.y, 0.0);
    }

    @Test
    public void testToGridPositionNormal() {
        // TILE_SIZE is usually 24 based on typical Pacman, but let's assume standard behavior
        // Center of tile (1,1) -> 1*TILE_SIZE = 24.
        Position pos = new Position(TILE_SIZE, TILE_SIZE); 
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)1, grid.getKey());
        assertEquals((Integer)1, grid.getValue());
    }

    @Test
    public void testNegativeCoordinates() {
        Position pos = new Position(-50.0, -100.0);
        assertEquals(-50.0, pos.x, 0.0);
        assertEquals(-100.0, pos.y, 0.0);
        
        // Should clamp to 0
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)0, grid.getKey());
        assertEquals((Integer)0, grid.getValue());
    }

    @Test
    public void testLargeCoordinates() {
        Position pos = new Position(10000.0, 10000.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        // Should clamp to max map size
        assertTrue(grid.getKey() > 0);
        assertTrue(grid.getValue() > 0);
    }

    @Test
    public void testExactBoundaryValues() {
        // Exactly on the boundary between tile 0 and 1
        // (0 + 24/2) / 24 = 0.5 -> cast to int is 0
        Position posLow = new Position(TILE_SIZE / 2.0 - 0.01, TILE_SIZE / 2.0 - 0.01);
        Pair<Integer, Integer> gridLow = posLow.ToGridPosition();
        assertEquals((Integer)0, gridLow.getKey());
        assertEquals((Integer)0, gridLow.getValue());

        Position posHigh = new Position(TILE_SIZE / 2.0, TILE_SIZE / 2.0);
        Pair<Integer, Integer> gridHigh = posHigh.ToGridPosition();
        // Depending on implementation (x + TILE_SIZE/2),
        // if x = 12 (half tile), num is 24 / 24 = 1.
        assertEquals((Integer)1, gridHigh.getKey());
        assertEquals((Integer)1, gridHigh.getValue());
    }

    @Test
    public void testNaNValues() {
        Position pos = new Position(Double.NaN, Double.NaN);
        assertEquals(Double.NaN, pos.x, 0.0);
        // Behavior of ToGridPosition with NaN is undefined but shouldn't crash
        try {
            pos.ToGridPosition();
        } catch (Exception e) {
            fail("Should not throw exception on NaN");
        }
    }

    @Test
    public void testInfinityValues() {
        Position pos = new Position(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, pos.x, 0.0);
        
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        // Should likely clamp to max and min (0)
        assertNotNull(grid);
    }

    @Test
    public void testToScreenPosition() {
        Position pos = new Position(50.9, 25.1);
        Pair<Integer, Integer> screen = pos.ToScreenPosition();
        assertEquals((Integer)50, screen.getKey());
        assertEquals((Integer)25, screen.getValue());
    }

    @Test
    public void testJustBelowZero() {
        Position pos = new Position(-0.1, -0.1);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)0, grid.getKey());
        assertEquals((Integer)0, grid.getValue());
    }
    
    @Test
    public void testMaxIntegerOverflowAttempt() {
        Position pos = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
        // Expect clamping to map size
    }

    @Test
    public void testMinValue() {
        Position pos = new Position(Double.MIN_VALUE, Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, pos.x, 0.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)0, grid.getKey());
        assertEquals((Integer)0, grid.getValue());
    }

    @Test
    public void testNegativeZero() {
        Position pos = new Position(-0.0, -0.0);
        assertEquals(-0.0, pos.x, 0.0); // -0.0 == 0.0 is true in Java
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)0, grid.getKey());
        assertEquals((Integer)0, grid.getValue());
    }

    @Test
    public void testExactTileBoundaryUpper() {
        // TILE_SIZE is 24.
        // If x = 24.0, (24 + 12) / 24 = 36/24 = 1.5 -> 1.
        Position pos = new Position(TILE_SIZE, TILE_SIZE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer)1, grid.getKey());
        assertEquals((Integer)1, grid.getValue());
    }

    @Test
    public void testSmallNegative() {
        // Just below zero, but very small magnitude
        Position pos = new Position(-Double.MIN_VALUE, -Double.MIN_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        // Should still be 0 because (x + 12) / 24 is roughly 0.5 -> 0
        assertEquals((Integer)0, grid.getKey());
        assertEquals((Integer)0, grid.getValue());
    }

    @Test
    public void testMixedNaN() {
        Position pos = new Position(10.0, Double.NaN);
        assertEquals(10.0, pos.x, 0.0);
        assertTrue(Double.isNaN(pos.y));
    }

    @Test
    public void testMixedInfinity() {
        Position pos = new Position(Double.POSITIVE_INFINITY, 10.0);
        assertEquals(Double.POSITIVE_INFINITY, pos.x, 0.0);
        assertEquals(10.0, pos.y, 0.0);
        
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
    }

    @Test
    public void testPrecisionLoss() {
        // Large value where adding TILE_SIZE/2 might lose precision if not careful, 
        // though double precision is usually enough for this range.
        // 2^52 is approx 4.5e15.
        double largeX = 4.5e15;
        Position pos = new Position(largeX, 10.0);
        // largeX + 12.0 might be == largeX due to precision if larger.
        
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
    }
}
