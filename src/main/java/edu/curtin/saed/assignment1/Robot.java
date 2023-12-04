package edu.curtin.saed.assignment1;

import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.ImageIcon;

public class Robot {

    // Robot uniqueId and delayValue
    private int uniqueId;
    private int delayValue;

    // Maintain unique delay values
    private static Set<Integer> robotDelayValues = new HashSet<>();

    // Robot grid Position
    private int gridX;
    private int gridY;

    // Robot last grid Position
    private int lastGridX;
    private int lastGridY;

    // Robot Image
    private ImageIcon imageIcon;
    private static final String IMAGE_FILE = "1554047213.png";

    // Track robot Movement and appearence on the grid.
    private long moveEnd;
    private long lastAppearanceTime;

    public Robot(int uniqueId, int gridX, int gridY, long moveEnd) {
        this.uniqueId = uniqueId;
        this.delayValue = generateUniqueDelayValue();
        this.gridX = gridX;
        this.gridY = gridY;
        this.lastAppearanceTime = System.currentTimeMillis();
        this.moveEnd = moveEnd;
        URL url = getClass().getClassLoader().getResource(IMAGE_FILE);
        imageIcon = new ImageIcon(url);
    }

    /* Getters and setters for uniqueId */
    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    /* Getters for delay value */
    public int getDelayValue() {
        return delayValue;
    }

    /*
     * Generate a unique delay value for
     * the robot's movement.
     */
    private int generateUniqueDelayValue() {
        int delay;
        do {
            delay = new Random().nextInt(1501) + 500;
        } while (robotDelayValues.contains(delay));

        robotDelayValues.add(delay);
        return delay;
    }

    /*
     * Getter and Setters
     * for the current grid position of the robot.
     */
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    /*
     * Getter and Setters
     * for the previous grid position of the robot.
     */

    public int getLastGridX() {
        return lastGridX;
    }

    public int getLastGridY() {
        return lastGridY;
    }

    public void setLastGridX(int lastGridX) {
        this.lastGridX = lastGridX;
    }

    public void setLastGridY(int lastGridY) {
        this.lastGridY = lastGridY;
    }

    /* Get robot image Icon */
    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    /*
     * Getters and setters for robot
     * movement time
     */
    public long getMoveEnd() {
        return moveEnd;
    }

    public void setMoveEnd(long moveEnd) {
        this.moveEnd = moveEnd;
    }

    /*
     * Getters and setters for robots
     * last appearence
     */
    public long getLastAppearanceTime() {
        return lastAppearanceTime;
    }

    public void setLastAppearanceTime(long lastAppearanceTime) {
        this.lastAppearanceTime = lastAppearanceTime;
    }

}
