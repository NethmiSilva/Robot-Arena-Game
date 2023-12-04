package edu.curtin.saed.assignment1;

import java.util.Objects;

public class Coordinates {

    /* Grid x postition and y position */
    private int x;
    private int y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /* Getters for grid position */
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /*
     * store unique objects with distinct x and y values
     */

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /*
     * check for equality based on x and y values.
     */

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;

        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Coordinates that = (Coordinates) object;
        return x == that.x && y == that.y;
    }

}
