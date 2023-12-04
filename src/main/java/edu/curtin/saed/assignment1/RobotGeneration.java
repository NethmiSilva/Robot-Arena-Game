package edu.curtin.saed.assignment1;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;

public class RobotGeneration {

    /* Grid size */
    private int gridWidth;
    private int gridHeight;

    /* Robot unique id */
    private int uniqueId;

    /* To check arena before generating */
    private ArenaCheck arenaCheck;

    public RobotGeneration(List<Robot> robots, int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.uniqueId = 1;/* Id starts with 1 */
        arenaCheck = new ArenaCheck(robots);
    }

    /*
     * Randomly choose a corner (0, 1, 2, or 3)
     * Number 0 = grid (0,0)
     * Number 1 = grid (0, 8)
     * Number 2 = grid (8, 0)
     * Number 3 = grid (8,8)
     */
    private int getRandomCorner() {
        int corner = new Random().nextInt(4);
        return corner;
    }

    /* Get x coordinate according to the randome number */
    private int getXCoordinate(int corner) {
        int xCoord;
        switch (corner) {
            case 0:
                xCoord = 0;
                break;
            case 1:
                xCoord = 0;
                break;
            case 2:
                xCoord = gridWidth - 1;
                break;
            case 3:
                xCoord = gridWidth - 1;
                break;
            default:
                throw new IllegalStateException("Invalid corner value");
        }
        return xCoord;
    }

    /* Get y coordinate according to the random number */
    private int getYCoordinate(int corner) {
        int yCoord;
        switch (corner) {
            case 0:
                yCoord = 0;
                break;

            case 1:
                yCoord = gridHeight - 1;
                break;

            case 2:
                yCoord = 0;
                break;

            case 3:
                yCoord = gridHeight - 1;
                break;

            default:
                throw new IllegalStateException("Invalid corner value");
        }
        return yCoord;
    }

    /* Robot generated till grid sqaure not occupied */
    public Robot generateRobot(List<Robot> robots, Map<Coordinates, Wall> walls, JTextArea logger,
            ImageIcon[][] imageIcons) {
        int x, y;

        do {
            int corner = getRandomCorner();
            x = getXCoordinate(corner);
            y = getYCoordinate(corner);
        } while (arenaCheck.isPositionOccupiedByRobot(x, y));

        /* Each robot takes 400ms to move */
        long moveEnd = System.currentTimeMillis() + 400;
        Robot newRobot = updateArenaLog(x, y, moveEnd, logger, robots);

        return newRobot;
    }

    /* Update log status */
    private Robot updateArenaLog(int x, int y, long moveEnd, JTextArea logger, List<Robot> robots) {
        Robot newRobot = createRobot(x, y, moveEnd, robots);

        logger.append("Robot " + newRobot.getUniqueId() + " created at " + x + "," + y + "\n");

        return newRobot;
    }

    /*
     * New robot created with unique id
     * Grid is set to be occupied till move ends
     */
    private Robot createRobot(int x, int y, long moveEnd, List<Robot> robots) {
        Robot newRobot = new Robot(uniqueId, x, y, moveEnd);
        boolean[][] gridSquares = new boolean[gridWidth][gridHeight];
        /* Increment id for each new robot */
        uniqueId++;
        robots.add(newRobot);
        arenaCheck.occupyGrid(x, y, moveEnd, gridSquares);
        return newRobot;
    }

}
