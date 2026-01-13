package com.example.model;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.common.BaseTest;
import javafx.util.Pair;
import org.junit.Test;

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

        Position pos = new Position(TILE_SIZE, TILE_SIZE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 1, grid.getKey());
        assertEquals((Integer) 1, grid.getValue());
    }

    @Test
    public void testNegativeCoordinates() {
        Position pos = new Position(-50.0, -100.0);
        assertEquals(-50.0, pos.x, 0.0);
        assertEquals(-100.0, pos.y, 0.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    public void testLargeCoordinates() {
        Position pos = new Position(10000.0, 10000.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertTrue(grid.getKey() > 0);
        assertTrue(grid.getValue() > 0);
    }

    @Test
    public void testExactBoundaryValues() {

        Position posLow = new Position(TILE_SIZE / 2.0 - 0.01, TILE_SIZE / 2.0 - 0.01);
        Pair<Integer, Integer> gridLow = posLow.ToGridPosition();
        assertEquals((Integer) 0, gridLow.getKey());
        assertEquals((Integer) 0, gridLow.getValue());

        Position posHigh = new Position(TILE_SIZE / 2.0, TILE_SIZE / 2.0);
        Pair<Integer, Integer> gridHigh = posHigh.ToGridPosition();

        assertEquals((Integer) 1, gridHigh.getKey());
        assertEquals((Integer) 1, gridHigh.getValue());
    }

    @Test
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
    public void testInfinityValues() {
        Position pos = new Position(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, pos.x, 0.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertNotNull(grid);
    }

    @Test
    public void testToScreenPosition() {
        Position pos = new Position(50.9, 25.1);
        Pair<Integer, Integer> screen = pos.ToScreenPosition();
        assertEquals((Integer) 50, screen.getKey());
        assertEquals((Integer) 25, screen.getValue());
    }

    @Test
    public void testJustBelowZero() {
        Position pos = new Position(-0.1, -0.1);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    public void testMaxIntegerOverflowAttempt() {
        Position pos = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);

    }

    @Test
    public void testMinValue() {
        Position pos = new Position(Double.MIN_VALUE, Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, pos.x, 0.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    public void testNegativeZero() {
        Position pos = new Position(-0.0, -0.0);
        assertEquals(-0.0, pos.x, 0.0);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
    }

    @Test
    public void testExactTileBoundaryUpper() {

        Position pos = new Position(TILE_SIZE, TILE_SIZE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertEquals((Integer) 1, grid.getKey());
        assertEquals((Integer) 1, grid.getValue());
    }

    @Test
    public void testSmallNegative() {

        Position pos = new Position(-Double.MIN_VALUE, -Double.MIN_VALUE);
        Pair<Integer, Integer> grid = pos.ToGridPosition();

        assertEquals((Integer) 0, grid.getKey());
        assertEquals((Integer) 0, grid.getValue());
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

        double largeX = 4.5e15;
        Position pos = new Position(largeX, 10.0);

        Pair<Integer, Integer> grid = pos.ToGridPosition();
        assertNotNull(grid);
    }
}
