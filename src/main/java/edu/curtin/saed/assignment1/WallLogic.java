package edu.curtin.saed.assignment1;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class WallLogic {

    /* Grid size */
    private int gridWidth;
    private int gridHeight;

    /* Queue Label to update */
    private JLabel queueLabel;

    /* Wall queue */
    private BlockingQueue<Coordinates> wallQueue;
    private int noOfwallsInQueue = 0;

    private Map<Coordinates, Wall> walls;
    private JTextArea logger;

    /* Constructor */
    public WallLogic(BlockingQueue<Coordinates> wallQueue, int gridWidth, int gridHeight, JLabel queueLabel,
            Map<Coordinates, Wall> walls,
            JTextArea logger) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.wallQueue = wallQueue;
        this.queueLabel = queueLabel;
        this.walls = walls;
        this.logger = logger;
    }

    /*
     * Wall collison logic:
     * one collision: wall weakends
     * two collisions: wall gets destroyed
     */
    public void wallLogic(int gridX, int gridY, ImageIcon[][] imageIcons) {
        Wall wall = walls.get(new Coordinates(gridX, gridY));
        int wallHealth = wall.getHealth();
        wall.weaken();
        int weakenedHealth = wall.getHealth();
        logger.append("Wall, " + gridX + ", " + gridY + " health = " + wallHealth + " -> " + weakenedHealth + "\n");

        if (wall.isDestroyed()) {
            imageIcons[gridX][gridY] = null;
            walls.remove(new Coordinates(gridX, gridY));
            wallQueue.remove(new Coordinates(gridX, gridY));

            noOfwallsInQueue--;
            updateQueueLabel();
        }
    }

    /* Wall build */
    public void buildWall(int gridX, int gridY, Object lock) {
        wallQueue.add(new Coordinates(gridX, gridY));
        noOfwallsInQueue++;
        updateQueueLabel();

    }

    /* Queue label is updated with number of walls in queue */
    private void updateQueueLabel() {
        if (queueLabel != null) {
            queueLabel.setText("Queued Commands: " + noOfwallsInQueue);
        }
    }

    /*
     * Check if wall build is within the grid and
     * if no.of walls hasnt exceeded
     */
    public boolean canBuildWallCheck(int gridX, int gridY, int maxWalls) {
        boolean withinGridBounds = false;
        boolean enoughWallsAvailable = false;

        if (gridX < gridWidth && gridY < gridHeight) {
            withinGridBounds = true;
        }

        if (noOfwallsInQueue < maxWalls) {
            enoughWallsAvailable = true;
        }

        return withinGridBounds && enoughWallsAvailable;
    }

}
