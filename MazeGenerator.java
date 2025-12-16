package ch.epfl.cs107.icmaze;

import java.util.Random;
import ch.epfl.cs107.play.math.DiscreteCoordinates;

/**
 * Utility class for generating rectangular mazes using the recursive division algorithm.
 * Complete implementation required by the exercise.
 *
 * The output is an int[height][width] array where:
 *  - 0 = path
 *  - 1 = wall
 */
public final class MazeGenerator {
    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final Random random = RandomGenerator.rng;

    private MazeGenerator(){}

    /**
     * Print the maze (provided in the skeleton).
     */
    public static void printMaze(int[][] grid, DiscreteCoordinates start, DiscreteCoordinates end) {
        int height = grid.length;
        int width = grid[0].length;

        // Print top border
        System.out.print("┌");
        for (int i = 0; i < width; i++) {
            System.out.print("───");
        }
        System.out.println("┐");

        // Print maze rows
        for (int y = 0; y < height; y++) {
            System.out.print("│");
            for (int x = 0; x < width; x++) {
                if (x == start.x && y == start.y) System.out.print(" S ");
                else if (x == end.x && y == end.y) System.out.print(" E ");
                else System.out.print(grid[y][x] == WALL ? "███" : "   ");
            }
            System.out.println("│");
        }

        // Print bottom border
        System.out.print("└");
        for (int i = 0; i < width; i++) {
            System.out.print("───");
        }
        System.out.println("┘");
    }

    /**
     * Returns a random odd number in [1, max] (assuming max > 0).
     */
    private static int randomOdd(int max) {
        return 1 + 2 * random.nextInt((max + 1) / 2);
    }

    /**
     * Returns a random even number in [0, max] (assuming max >= 0).
     */
    private static int randomEven(int max) {
        return 2 * random.nextInt((max + 1) / 2);
    }

    /**
     * Create a maze using recursive division.
     *
     * width, height: dimensions in number of cells (must be >= 3 ideally)
     * difficulty: minimal region dimension to keep dividing (higher -> larger corridors)
     *
     * Returns an int[height][width] grid (row-major: grid[row][col]).
     */
    public static int[][] createMaze(int width, int height, int difficulty) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width/height must be > 0");
        }
        // initialize all cells to PATH first
        int[][] grid = new int[height][width];
        for (int r = 0; r < height; ++r) {
            for (int c = 0; c < width; ++c) {
                grid[r][c] = PATH;
            }
        }

        // surround perimeter with walls to avoid out-of-bounds passages
        for (int x = 0; x < width; ++x) {
            grid[0][x] = WALL;
            grid[height - 1][x] = WALL;
        }
        for (int y = 0; y < height; ++y) {
            grid[y][0] = WALL;
            grid[y][width - 1] = WALL;
        }

        // Start recursive division on the inner area (1..width-2, 1..height-2)
        divide(grid, 1, 1, width - 2, height - 2, difficulty);

        return grid;
    }

    /**
     * Recursive division:
     * region defined by top-left (rx,ry) and size rw x rh (in cells).
     */
    private static void divide(int[][] grid, int rx, int ry, int rw, int rh, int difficulty) {
        // base case: stop if region too small to divide further
        if (rw <= difficulty || rh <= difficulty) {
            return;
        }

        // choose orientation: prefer dividing the longer side
        boolean horizontal = rh >= rw;

        if (horizontal) {
            // choose a wall row - must be at an odd offset relative to rx/ry region to keep cell pattern
            // wallY is an index in grid (absolute)
            int wallY = ry + randomOdd(rh - 1); // ensures at least 1 cell margin
            // choose a random passage column within the region (even index)
            int passageX = rx + randomEven(rw - 1);

            // Build horizontal wall across region at wallY
            for (int x = rx; x < rx + rw; ++x) {
                grid[wallY][x] = WALL;
            }
            // Carve one passage
            grid[wallY][passageX] = PATH;

            // Recurse top and bottom regions
            int topHeight = wallY - ry;
            int bottomHeight = ry + rh - (wallY + 1);
            // top region: rx, ry, rw, topHeight
            if (topHeight > 0) divide(grid, rx, ry, rw, topHeight, difficulty);
            // bottom region: rx, wallY+1, rw, bottomHeight
            if (bottomHeight > 0) divide(grid, rx, wallY + 1, rw, bottomHeight, difficulty);

        } else {
            // vertical wall
            int wallX = rx + randomOdd(rw - 1);
            int passageY = ry + randomEven(rh - 1);

            for (int y = ry; y < ry + rh; ++y) {
                grid[y][wallX] = WALL;
            }
            // carve passage
            grid[passageY][wallX] = PATH;

            int leftWidth = wallX - rx;
            int rightWidth = rx + rw - (wallX + 1);
            if (leftWidth > 0) divide(grid, rx, ry, leftWidth, rh, difficulty);
            if (rightWidth > 0) divide(grid, wallX + 1, ry, rightWidth, rh, difficulty);
        }
    }
}