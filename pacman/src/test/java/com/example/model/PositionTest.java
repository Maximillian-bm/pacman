package com.example.model;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.example.common.BaseTest;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Position and Coordinate Conversion Logic Tests")
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
    @DisplayName("Position object should correctly store and retrieve coordinates")
    public void testEqualsSameObject() {
        Position pos = new Position(10.5, 20.7);
        assertEquals(10.5, pos.x, 0.0);
        assertEquals(20.7, pos.y, 0.0);
    }

    @Test
    @DisplayName("Empty constructor should initialize position at (0,0)")
    public void testEmptyConstructor() {
        Position pos = new Position();
        assertEquals(0.0, pos.x, 0.0);
        assertEquals(0.0, pos.y, 0.0);
    }

    @Test
    @DisplayName("Coordinate to grid conversion should work for normal tile-aligned positions")
    public void testToGridPositionNormal() {

        Position pos = new Position(TILE_SIZE, TILE_SIZE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 1, grid.getKey());
        assertEquals((Integer) 1, grid.getValue());
    }

    @Test
    @DisplayName("Negative coordinates should map to grid index 0 or handle correctly")
    public void testNegativeCoordinates() {
        Position pos = new Position(-50.0, -100.0);
        assertEquals(-50.0, pos.x, 0.0);
        assertEquals(-100.0, pos.y, 0.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    @DisplayName("Conversion should handle extremely large coordinate values")
    public void testLargeCoordinates() {
        Position pos = new Position(10000.0, 10000.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertEquals((Integer) (int)(10000.0 / TILE_SIZE + 0.5), grid.getKey(), "Large X coordinate grid mapping");
        assertEquals((Integer) (int)(10000.0 / TILE_SIZE + 0.5), grid.getValue(), "Large Y coordinate grid mapping");
    }

    @Test
    @DisplayName("Conversion should correctly round at tile boundaries")
    public void testExactBoundaryValues() {

        Position posLow = new Position(TILE_SIZE / 2.0 - 0.01, TILE_SIZE / 2.0 - 0.01);
        Pair<Integer, Integer> gridLow = posLow.ToGridPosition();
        assertEquals((Integer) 0, gridLow.getKey(), "Just below boundary should map to 0");
        assertEquals((Integer) 0, gridLow.getValue(), "Just below boundary should map to 0");

        Position posHigh = new Position(TILE_SIZE / 2.0, TILE_SIZE / 2.0);
        Pair<Integer, Integer> gridHigh = posHigh.ToGridPosition();

        assertEquals((Integer) 1, gridHigh.getKey(), "Exactly at boundary should map to 1");
        assertEquals((Integer) 1, gridHigh.getValue(), "Exactly at boundary should map to 1");
        
        Position posRight = new Position(TILE_SIZE + TILE_SIZE / 2.0 - 0.01, TILE_SIZE);
        Pair<Integer, Integer> gridRight = posRight.ToGridPosition();
        assertEquals((Integer) 1, gridRight.getKey(), "Just below next boundary should map to 1");
        assertEquals((Integer) 1, gridRight.getValue(), "Center of tile 1 should map to 1");
    }

    @Test
    @DisplayName("Position should handle NaN coordinate values gracefully")
    public void testNaNValues() {
        Position pos = new Position(Double.NaN, Double.NaN);
        assertEquals(Double.NaN, pos.x, 0.0);

        try {
            pos.ToGridPosition();
        } catch (Exception e) {
            fail("Should not throw exception on NaN");
        }
    }

    @Test
    @DisplayName("Position should handle infinite coordinate values")
    public void testInfinityValues() {
        Position pos = new Position(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, pos.x, 0.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertNotNull(grid);
    }

    @Test
    @DisplayName("Coordinate to screen pixel conversion should be accurate")
    public void testToScreenPosition() {
        Position pos = new Position(50.9, 25.1);
        Pair<Integer, Integer> screen = pos.ToScreenPosition();
        assertEquals((Integer) 50, screen.getKey());
        assertEquals((Integer) 25, screen.getValue());
    }

    @Test
    @DisplayName("Coordinates just below zero should map to grid index 0")
    public void testJustBelowZero() {
        Position pos = new Position(-0.1, -0.1);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    @DisplayName("Conversion should handle Double.MAX_VALUE without overflow crashing")
    public void testMaxIntegerOverflowAttempt() {
        Position pos = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);

    }

    @Test
    @DisplayName("Conversion should handle Double.MIN_VALUE correctly")
    public void testMinValue() {
        Position pos = new Position(Double.MIN_VALUE, Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, pos.x, 0.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    @DisplayName("Negative zero coordinates should be treated as zero")
    public void testNegativeZero() {
        Position pos = new Position(-0.0, -0.0);
        assertEquals(-0.0, pos.x, 0.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    @DisplayName("Positions exactly at tile boundaries should map to the next tile index")
    public void testExactTileBoundaryUpper() {

        Position pos = new Position(TILE_SIZE, TILE_SIZE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 1, grid.getKey());
        assertEquals((Integer) 1, grid.getValue());
    }

    @Test
    @DisplayName("Extremely small negative values should be handled by grid conversion")
    public void testSmallNegative() {

        Position pos = new Position(-Double.MIN_VALUE, -Double.MIN_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    @DisplayName("Position should store mixed valid and NaN coordinates")
    public void testMixedNaN() {
        Position pos = new Position(10.0, Double.NaN);
        assertEquals(10.0, pos.x, 0.0);
        assertTrue(Double.isNaN(pos.y));
    }

    @Test
    @DisplayName("Position should store mixed valid and infinite coordinates")
    public void testMixedInfinity() {
        Position pos = new Position(Double.POSITIVE_INFINITY, 10.0);
        assertEquals(Double.POSITIVE_INFINITY, pos.x, 0.0);
        assertEquals(10.0, pos.y, 0.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
    }

    @Test
    @DisplayName("Conversion should remain robust against floating point precision loss at large values")
    public void testPrecisionLoss() {

        double largeX = 4.5e15;
        Position pos = new Position(largeX, 10.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
    }
}
