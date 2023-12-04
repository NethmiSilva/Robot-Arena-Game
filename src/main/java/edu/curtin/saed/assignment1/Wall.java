package edu.curtin.saed.assignment1;

public class Wall {

    /* Wall health */
    private int health;

    /* Constructor */
    public Wall(int initialHealth) {
        health = initialHealth;
    }

    /* Getters and setters for wall health */
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    /* Method to weaken health upon collison */
    public void weaken() {
        health--;
    }

    /* Method to check if wall is detroyed */
    public boolean isDestroyed() {
        return health <= 0;
    }

}
