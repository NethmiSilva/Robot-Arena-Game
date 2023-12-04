package edu.curtin.saed.assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class RobotMovement {

    /* Grid size */
    private int gridWidth;
    private int gridHeight;

    /* List of robots */
    private List<Robot> robots;

    /* Logger */
    private JTextArea logger;

    /* Arena check before movement */
    private ArenaCheck arenaCheck;

    private WallLogic wallLogic;

    /* Constructor */
    public RobotMovement(List<Robot> robots, int gridWidth, int gridHeight, JTextArea logger, WallLogic wallLogic) {
        this.robots = robots;
        this.logger = logger;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.wallLogic = wallLogic;
        arenaCheck = new ArenaCheck(robots);
    }

    /* Move in the x axis */
    private int moveX(int currentXCoord, int targetXCoord, boolean moveHorizontally) {
        int newXCoord = currentXCoord;

        if (moveHorizontally) {
            if (currentXCoord < targetXCoord) {
                newXCoord = currentXCoord + 1; // Move up
                if (currentXCoord > targetXCoord) {
                    newXCoord = targetXCoord; // Ensure we don't overshoot
                }
            } else if (currentXCoord > targetXCoord) {
                newXCoord = currentXCoord - 1; // Move up
                if (currentXCoord < targetXCoord) {
                    newXCoord = targetXCoord;
                }
            }
        }

        return newXCoord;
    }

    /* Move in the y axis */
    private int moveY(int currentYCoord, int targetYCoord, boolean moveVertically) {
        int newYCoord = currentYCoord;

        if (moveVertically) {
            if (currentYCoord < targetYCoord) {
                newYCoord = currentYCoord + 1; // Move up
            } else if (currentYCoord > targetYCoord) {
                newYCoord = currentYCoord - 1; // Move down
            } else {
                newYCoord = currentYCoord; // Stay in the same position
            }
        }

        return newYCoord;
    }

    /*
     * If there is a wall in the new grid square the robot is about to move to
     * wall collision is logged and score increases by 100
     */
    private void wallCollisionLog(int newXCoord, int newYCoord, JLabel scoreLabel) {
        logger.append("Robot collided with wall at (" + newXCoord + ", " + newYCoord + ")\n");
        int currentScore = Integer.parseInt(scoreLabel.getText().replace("Score: ", ""));
        int newScore = currentScore + 100;
        scoreLabel.setText("Score: " + newScore);
    }

    public void moveRobots(Map<Coordinates, Wall> walls, JLabel scoreLabel, ImageIcon[][] imageIcons) {

        long currentTime = System.currentTimeMillis();
        boolean[][] gridSquares = new boolean[gridWidth][gridHeight];
        List<Robot> robotsToRemove = new ArrayList<>(); /* List of robots generated to be removed */

        for (Robot robot : robots) {
            long timeDifference = currentTime - robot.getLastAppearanceTime();
            int delayValue = robot.getDelayValue();

            /* Checks time with delay value before movement */
            if (timeDifference >= delayValue) {

                /* Last appearence time changed */
                robot.setLastAppearanceTime(currentTime);

                /* Get robot's positon */
                int currentXCoord = robot.getGridX();
                int currentYCoord = robot.getGridY();

                /* Citadel position */
                int targetXCoord = (gridWidth - 1) / 2;
                int targetYCoord = (gridHeight - 1) / 2;

                /*
                 * Random generator to check whether to move horizontally or
                 * vertically
                 */
                boolean moveHorizontally = new Random().nextBoolean();
                boolean moveVertically = !moveHorizontally;

                int newXCoord = moveX(currentXCoord, targetXCoord, moveHorizontally);
                int newYCoord = moveY(currentYCoord, targetYCoord, moveVertically);

                /* Check if new grid is within the grid and not occupied by robot */
                if (arenaCheck.isValidPosition(newXCoord, newYCoord, gridWidth, gridHeight)) {

                    /* Checks if new grid square has a wall */
                    if (arenaCheck.isPositionOccupiedByWall(newXCoord, newYCoord, walls)) {
                        wallCollisionLog(newXCoord, newYCoord, scoreLabel);

                        /* Delete robot and clear grid square */
                        robotsToRemove.add(robot);
                        arenaCheck.clearGrid(currentXCoord, currentYCoord, gridSquares);

                        /*
                         * Change image to a broken wall
                         * and wall logic is called
                         */
                        imageIcons[newXCoord][newYCoord] = arenaCheck.loadImageIcon("181479.png");
                        wallLogic.wallLogic(newXCoord, newYCoord, imageIcons);

                        /* Set logger status */
                        logger.append("Robot died: " + robot.getUniqueId() + "\n");

                    } else {

                        long moveEndTime = robot.getMoveEnd();
                        arenaCheck.occupyGrid(currentXCoord, currentYCoord, moveEndTime, gridSquares);
                        arenaCheck.occupyGrid(newXCoord, newYCoord, moveEndTime, gridSquares);

                        /*
                         * Set new grid sqaure and
                         * last grid square for the robot
                         */
                        robot.setLastGridX(currentXCoord);
                        robot.setLastGridY(currentYCoord);
                        robot.setGridX(newXCoord);
                        robot.setGridY(newYCoord);

                        /*
                         * Clear the occupancy of the original grid square
                         * after the move is complete
                         */
                        arenaCheck.clearGrid(currentXCoord, currentYCoord, gridSquares);
                    }

                }

            }

        }

        /* Removes robots that died */
        robots.removeAll(robotsToRemove);

    }

}
