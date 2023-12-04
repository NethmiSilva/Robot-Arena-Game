package edu.curtin.saed.assignment1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List; // So that 'List' means java.util.List and not java.awt.List.
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.URL;

/**
 * A Swing GUI element that displays a grid on which you can draw images, text
 * and lines.
 */
public class SwingArena extends JPanel {

    /* Grid size and coordinates */

    private int gridWidth = 9;
    private int gridHeight = 9;
    private double gridSquareSize; // Auto-calculated

    /**
     * Target X Coordinate for the Citadel position.
     */
    private int targetXCoord = (gridWidth - 1) / 2;

    /**
     * Target Y Coordinate for the Citadel position.
     */
    private int targetYCoord = (gridHeight - 1) / 2;

    /*
     * Game end check
     * Volatile for thread safety because it is a shared variable amoungst threads
     */
    private volatile boolean gameEnded = false;

    /* Image icons for gui */
    private static final String WALL_IMAGE = "181478.png";
    private static final String CITADEL = "rg1024-isometric-tower.png";
    private ImageIcon wallImageIcon;
    private ImageIcon[][] imageIcons; // Two-dimensional array to store image icons

    /* JLabel for scores */
    private JLabel scoreLabel;

    /* JTextArea for logging details */
    private JTextArea logger;

    /* Wall build variables */
    private final int maxWalls = 10;
    private int builtWalls = 0;

    private Map<Coordinates, Wall> walls;

    /* List of robots */
    private List<Robot> robots = new ArrayList<>();

    /* Class variables */
    private ArenaCheck arenaCheck;
    private RobotGeneration robotGenerator;
    private RobotMovement robotMovement;
    private WallLogic wallLogic;

    /* Threads for tasks */
    private Thread scoreThread;
    private Thread robotGenerationThread;
    private Thread robotMovementThread;
    private Thread wallBuildThread;

    /* Object for synchronization of threads */
    private Object mutex = new Object();

    /* Blocking queues */
    private BlockingQueue<Robot> generationQueue; // Queue for robot generation commands
    private BlockingQueue<Coordinates> wallBuildQueue; // Queue for wall-building commands

    private List<ArenaListener> listeners = null;

    /**
     * Creates a new arena object
     */
    public SwingArena(JTextArea logger, JLabel scoreLabel, JLabel queueLabel, Map<Coordinates, Wall> walls) {

        this.logger = logger;
        this.walls = walls;
        this.scoreLabel = scoreLabel;

        imageIcons = new ImageIcon[gridWidth][gridHeight];

        generationQueue = new LinkedBlockingQueue<>();
        wallBuildQueue = new LinkedBlockingQueue<>();

        this.wallLogic = new WallLogic(wallBuildQueue, gridHeight, gridWidth, queueLabel, walls, logger);
        arenaCheck = new ArenaCheck(robots);
        robotGenerator = new RobotGeneration(robots, gridWidth, gridHeight);
        robotMovement = new RobotMovement(robots, gridWidth, gridHeight, logger, wallLogic);
        URL url = getClass().getClassLoader().getResource(CITADEL);
        if (url == null) {
            throw new AssertionError("Cannot find image file 1554047213.png");
        }

        URL wallImageUrl = getClass().getClassLoader().getResource(WALL_IMAGE);
        if (wallImageUrl == null) {
            throw new AssertionError("Cannot find wall image file 181478.png");
        }
        wallImageIcon = new ImageIcon(wallImageUrl);

        startThreads();

    }

    public void initializeListeners() {

        addListener(new ArenaListener() {
            @Override
            public void squareClicked(int x, int y) {
                System.out.println("Arena click at (" + x + "," + y + ")");

                Coordinates clickedCoords = new Coordinates(x, y);
                logger.append(
                        "Wall creation attempted at: " + clickedCoords.getX() + "," + clickedCoords.getY()
                                + "\n");

            }

        });
    }

    /* Update score label method */
    private void updateScoreLabel(JLabel scoreLabel) {
        /* Synchronized called to ensure thread safety */

        synchronized (mutex) {

            String labelText = scoreLabel.getText().trim(); // Trim leading/trailing spaces
            int currentScore = Integer.parseInt(labelText.replace("Score: ", ""));
            int newScore = currentScore + 10;
            SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + newScore));
        }
    }

    /* Method to start threads allocated for tasks */
    private void startThreads() {
        /* Thread for robot generation */
        Runnable robotGenerator = new Runnable() {
            @Override
            public void run() {
                /*
                 * Checks whether game has ended or
                 * whether thread was interupted before running
                 */
                while (!gameEnded && !Thread.currentThread().isInterrupted()) {

                    /* Synchronized called to ensure thread safety */
                    synchronized (mutex) {
                        generateRobot();
                        /* Mutex notifies other threads */
                        mutex.notifyAll();
                        if (Thread.interrupted()) {
                            break;
                        }

                    }
                    /*
                     * Thread sleep so that every
                     * 1500 ms a robot is generated
                     */
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        robotGenerationThread = new Thread(robotGenerator, "Robot generation thread");
        robotGenerationThread.start();

        /* Thread to update score */
        Runnable scoreUpdater = new Runnable() {
            @Override
            public void run() {
                /*
                 * Checks whether game has ended or
                 * whether thread was interupted before running
                 */
                while (!gameEnded && !Thread.currentThread().isInterrupted()) {
                    /* Synchronized called to ensure thread safety */

                    synchronized (mutex) {
                        updateScoreLabel(scoreLabel);
                        if (Thread.interrupted()) {
                            break;
                        }
                    }

                    /* Thread sleep called so that score updates every second */
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        scoreThread = new Thread(scoreUpdater, "Score update thread");
        scoreThread.start();

        /* Thread for robot movement */
        Runnable robotMover = new Runnable() {
            @Override
            public void run() {
                /*
                 * Checks whether game has ended or
                 * whether thread was interupted before running
                 */
                while (!gameEnded && !Thread.currentThread().isInterrupted()) {
                    /* Synchronized called to ensure thread safety */

                    synchronized (mutex) {

                        Robot robot = generationQueue.poll();
                        if (robot != null) {
                            robots.add(robot);
                        }
                        moveRobot();

                        /* Mutex notifies other threads */
                        mutex.notifyAll();

                        if (Thread.interrupted()) {
                            break;
                        }
                    }

                }
            }
        };
        robotMovementThread = new Thread(robotMover, "Robot movement thread");
        robotMovementThread.start();

    }

    /*
     * Checks if the grid square
     * has a robot or a wall
     */
    public boolean checkGridOccupied(int x, int y) {
        /* Synchronized called to ensure thread safety */

        synchronized (mutex) {
            for (Robot robot : robots) {
                if (robot.getGridX() == x && robot.getGridY() == y) {
                    return true;
                }
            }

            // Check if there is a wall at the specified coordinates
            return arenaCheck.isPositionOccupiedByWall(x, y, walls);
        }
    }

    /**
     * Adds a callback for when the user clicks on a grid square within the arena.
     * The callback
     * (of type ArenaListener) receives the grid (x,y) coordinates as parameters to
     * the
     * 'squareClicked()' method.
     */

    public void addListener(ArenaListener newListener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    int gridX = (int) ((double) event.getX() / gridSquareSize);
                    int gridY = (int) ((double) event.getY() / gridSquareSize);

                    /*
                     * addMouseListener calls build wall when square is clicked by user
                     * if grid is not occupied and number of walls built < max walls
                     */
                    if (wallLogic.canBuildWallCheck(gridX, gridY, maxWalls) && !checkGridOccupied(gridX, gridY)) {

                        /* Runnable is created for wall building commands */
                        Runnable wallBuild = new Runnable() {
                            @Override
                            public void run() {
                                /* Synchronized called to ensure thread safety */

                                synchronized (mutex) {
                                    /* If game ends, thread is interrupted */
                                    if (gameEnded) {
                                        wallBuildThread.interrupt();
                                    }

                                    /* Checks if thread was interupted before running the code */
                                    if (!Thread.currentThread().isInterrupted()) {
                                        wallLogic.buildWall(gridX, gridY, mutex);
                                        Wall wall = new Wall(2);
                                        walls.put(new Coordinates(gridX, gridY), wall);
                                        wallBuildQueue.add(new Coordinates(gridX, gridY));

                                        imageIcons[gridX][gridY] = wallImageIcon;

                                    }

                                }

                                /*
                                 * Thread sleep is called so that walls are built per
                                 * 2000 ms
                                 */
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }

                        };

                        /* New thread created and assigned for wall building */
                        wallBuildThread = new Thread(wallBuild, "Wall build thread");
                        wallBuildThread.start();

                        /* Repaint is called after each wall build */
                        SwingUtilities.invokeLater(() -> repaint());
                    }

                    if (gridX < gridWidth && gridY < gridHeight) {
                        for (ArenaListener listener : listeners) {
                            listener.squareClicked(gridX, gridY);
                        }
                    }
                }
            });
        }
        listeners.add(newListener);
    }

    /* Robot movement method */
    private void moveRobot() {
        /* Synchronized called to ensure thread safety */
        synchronized (mutex) {
            boolean[][] gridSquare = new boolean[gridWidth][gridHeight];

            /*
             * Image icon set with grid height and width to be sent to
             * robot movement function
             */
            if (imageIcons == null) {
                imageIcons = new ImageIcon[gridWidth][gridHeight];
            }

            if (!gameEnded) {

                /* Move robots method called */
                robotMovement.moveRobots(walls, scoreLabel, imageIcons);

                /* Handles wall collision image setting */
                while (!wallBuildQueue.isEmpty()) {
                    Coordinates wallCoords = wallBuildQueue.poll();
                    if (wallCoords != null && !checkGridOccupied(wallCoords.getX(), wallCoords.getY())) {

                        /*
                         * Check if the maximum limit of walls has been reached
                         * before building walls
                         */
                        if (builtWalls < maxWalls) {
                            Wall wall = new Wall(2);
                            walls.put(wallCoords, wall);
                            int x = wallCoords.getX();
                            int y = wallCoords.getY();

                            /* Update to draw a wall */
                            imageIcons[x][y] = wallImageIcon;
                            builtWalls++;
                            SwingUtilities.invokeLater(() -> repaint());

                            builtWalls++; // Increment the number of built walls
                        }
                    }

                }

                /* Game over logic */

                Iterator<Robot> iterator = robots.iterator();
                /*
                 * Iterate over each robto in the list
                 * till game has not ended
                 */
                while (iterator.hasNext() && gameEnded == false) {
                    Robot robot = iterator.next();

                    /*
                     * If robot reaches targetGridSquare
                     * game ends
                     */
                    if (robot.getGridX() == targetXCoord && robot.getGridY() == targetYCoord) {
                        gameEnded = true;

                        /* Game over logged */
                        SwingUtilities.invokeLater(() -> {
                            logger.append("Game Over!\n");
                            logger.append("Robot " + robot.getUniqueId() + " reached the target!\n");
                        });

                        /*
                         * Iterate over every grid square and clear
                         * the robots
                         */
                        for (int x = 0; x < gridWidth; x++) {
                            for (int y = 0; y < gridHeight; y++) {
                                arenaCheck.clearGrid(x, y, gridSquare);
                            }
                        }

                        iterator.remove();
                        robots.clear();

                        /*
                         * All threads are interupted
                         * and closed
                         */
                        scoreThread.interrupt();
                        robotGenerationThread.interrupt();
                        robotMovementThread.interrupt();
                        wallBuildThread.interrupt();

                    }
                }
            }
            SwingUtilities.invokeLater(() -> repaint());

        }

    }

    /* Robot generation method */
    private void generateRobot() {
        /* Synchronized called to ensure thread safety */

        synchronized (mutex) {
            Robot newRobot = robotGenerator.generateRobot(robots, walls, logger, imageIcons);
            generationQueue.add(newRobot);
        }
        /* Grid is repainted after every move */
        SwingUtilities.invokeLater(() -> repaint());
    }

    /**
     * This method is called in order to redraw the screen, either because the user
     * is manipulating
     * the window, OR because you've called 'repaint()'.
     *
     * You will need to modify the last part of this method; specifically the
     * sequence of calls to
     * the other 'draw...()' methods. You shouldn't need to modify anything else
     * about it.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gfx = (Graphics2D) g;
        gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // First, calculate how big each grid cell should be, in pixels. (We do need to
        // do this
        // every time we repaint the arena, because the size can change.)
        gridSquareSize = Math.min(
                (double) getWidth() / (double) gridWidth,
                (double) getHeight() / (double) gridHeight);

        int arenaPixelWidth = (int) ((double) gridWidth * gridSquareSize);
        int arenaPixelHeight = (int) ((double) gridHeight * gridSquareSize);

        // Draw the arena grid lines. This may help for debugging purposes, and just
        // generally
        // to see what's going on.
        gfx.setColor(Color.GRAY);
        gfx.drawRect(0, 0, arenaPixelWidth - 1, arenaPixelHeight - 1); // Outer edge

        for (int gridX = 1; gridX < gridWidth; gridX++) // Internal vertical grid lines
        {
            int x = (int) ((double) gridX * gridSquareSize);
            gfx.drawLine(x, 0, x, arenaPixelHeight);
        }

        for (int gridY = 1; gridY < gridHeight; gridY++) // Internal horizontal grid lines
        {
            int y = (int) ((double) gridY * gridSquareSize);
            gfx.drawLine(0, y, arenaPixelWidth, y);
        }

        // Invoke helper methods to draw things at the current location.
        // ** You will need to adapt this to the requirements of your application. **
        synchronized (mutex) {

            /* Draw walls */
            while (!wallBuildQueue.isEmpty()) {
                Coordinates wallCoords = wallBuildQueue.poll();
                int x = wallCoords.getX();
                int y = wallCoords.getY();
                drawImage(gfx, wallImageIcon, x, y);
            }

            /*
             * Draw robots
             * 
             */
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    if (imageIcons[x][y] != null) {
                        drawImage(gfx, imageIcons[x][y], x, y);
                    }

                    for (Robot robot : robots) {
                        int gridX = robot.getGridX();
                        int gridY = robot.getGridY();
                        if (gridX == x && gridY == y) {
                            drawImage(gfx, robot.getImageIcon(), gridX, gridY);
                            drawLabel(gfx, String.valueOf(robot.getUniqueId()), gridX, gridY);

                        }
                    }
                }
            }

            /* Draw citadel */

            ImageIcon targetImageIcon = arenaCheck.loadImageIcon(CITADEL);
            drawImage(gfx, targetImageIcon, targetXCoord, targetYCoord);

        }

    }

    /**
     * Draw an image in a specific grid location. *Only* call this from within
     * paintComponent().
     *
     * Note that the grid location can be fractional, so that (for instance), you
     * can draw an image
     * at location (3.5,4), and it will appear on the boundary between grid cells
     * (3,4) and (4,4).
     * 
     * You shouldn't need to modify this method.
     */
    private void drawImage(Graphics2D gfx, ImageIcon icon, double gridX, double gridY) {
        // Get the pixel coordinates representing the centre of where the image is to be
        // drawn.
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;

        // We also need to know how "big" to make the image. The image file has a
        // natural width
        // and height, but that's not necessarily the size we want to draw it on the
        // screen. We
        // do, however, want to preserve its aspect ratio.
        double fullSizePixelWidth = (double) icon.getIconWidth();
        double fullSizePixelHeight = (double) icon.getIconHeight();

        double displayedPixelWidth, displayedPixelHeight;
        if (fullSizePixelWidth > fullSizePixelHeight) {
            // Here, the image is wider than it is high, so we'll display it such that it's
            // as
            // wide as a full grid cell, and the height will be set to preserve the aspect
            // ratio.
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        } else {
            // Otherwise, it's the other way around -- full height, and width is set to
            // preserve the aspect ratio.
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        // Actually put the image on the screen.
        gfx.drawImage(icon.getImage(),
                (int) (x - displayedPixelWidth / 2.0), // Top-left pixel coordinates.
                (int) (y - displayedPixelHeight / 2.0),
                (int) displayedPixelWidth, // Size of displayed image.
                (int) displayedPixelHeight,
                null);
    }

    /**
     * Displays a string of text underneath a specific grid location. *Only* call
     * this from within
     * paintComponent().
     *
     * You shouldn't need to modify this method.
     */
    private void drawLabel(Graphics2D gfx, String label, double gridX, double gridY) {
        gfx.setColor(Color.BLUE);
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(label,
                (int) ((gridX + 0.5) * gridSquareSize - (double) fm.stringWidth(label) / 2.0),
                (int) ((gridY + 1.0) * gridSquareSize) + fm.getHeight());
    }

}
