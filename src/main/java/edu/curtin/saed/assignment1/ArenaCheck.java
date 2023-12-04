package edu.curtin.saed.assignment1;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;

public class ArenaCheck {

    /* Robots list */
    private List<Robot> robots;

    /* Arena check constructor */
    public ArenaCheck(List<Robot> robots) {
        this.robots = robots;
    }

    /* Method to clear the grid */
    public void clearGrid(int x, int y, boolean[][] gridSquare) {
        gridSquare[x][y] = false;
    }

    /* Make the object occupy the grid */
    public void occupyGrid(int x, int y, long moveEndTime, boolean[][] gridSquare) {
        gridSquare[x][y] = true;

        long delay = moveEndTime - System.currentTimeMillis();
        Runnable clearGrid = new Runnable() {
            @Override
            public void run() {
                clearGrid(x, y, gridSquare);

            }

        };

        /* Scheduled Thread pool to clear grid after move */
        /*
         * Reference to learn:
         * https://www.geeksforgeeks.org/scheduledthreadpoolexecutor-class-in-java/
         * 
         */

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(clearGrid, delay, TimeUnit.MILLISECONDS);

        executor.shutdown();
    }

    /* Check if a robot is present in the grid */
    public boolean isPositionOccupiedByRobot(int x, int y) {
        for (Robot robot : robots) {
            if (robot.getGridX() == x && robot.getGridY() == y) {
                return true;
            }
        }
        return false;
    }

    /* Check if a wall is present in the grid */
    public boolean isPositionOccupiedByWall(int x, int y, Map<Coordinates, Wall> walls) {
        return walls.containsKey(new Coordinates(x, y));
    }

    /* Method to check if its a valid position */
    public boolean isValidPosition(int x, int y, int gridWidth, int gridHeight) {
        boolean withinXBounds = false;
        boolean withinYBounds = false;
        if (x >= 0 && x < gridWidth) {
            withinXBounds = true;
        }

        if (y >= 0 && y < gridHeight) {
            withinYBounds = true;
        }

        boolean notOccupiedByRobot = !isPositionOccupiedByRobot(x, y);

        return withinXBounds && withinYBounds && notOccupiedByRobot;
    }

    /* Method to set image to object */
    public ImageIcon loadImageIcon(String filename) {
        URL url = getClass().getClassLoader().getResource(filename);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            throw new AssertionError("Cannot find image file: " + filename);
        }
    }

}
