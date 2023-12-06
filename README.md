# Killer Robots Defense Game

## Overview
The game is a Java-based application featuring a strategic interaction between robots and walls. Robots, initialized with unique attributes, move randomly towards a citadel on a grid. Walls, with health attributes, can be weakened over time. The GUI, implemented using SwingArena, displays the game elements and facilitates user interactions. The game involves coordinating robot movements, managing wall health, and strategically navigating the grid. The overarching goal is likely to guide robots to a citadel while dealing with wall challenges. The application offers a visually engaging experience with graphical representation and user-friendly interactions. This project involves creating a single-player game where the player defends against an army of killer robots. The game is played on a 9x9 grid, initially containing a central citadel that the player must protect. The player's score increases over time and for each robot destroyed due to a wall impact.

## Status Information
Two pieces of information are shown at all times:

Current number of queued-up wall-building commands.
Player's score.
Game Logic

## Robot Characteristics
Each robot has a unique ID and a delay value for movement.
Robots move randomly but "prefer" to move towards the citadel.
Robots cannot move into an occupied square or outside the grid.
Robots take 400 milliseconds to perform a move and can destroy walls on impact.

## Events
Robots appear randomly in corners every 1500 milliseconds.
The game ends if a robot moves into the citadel square.
Players can build fortress walls to block robots.

## Scoring
Score starts at 0 and increases by 10 points per second.
Destroying a robot increases the score by 100 points.

## Implementation Details
### Blocking Queue
A blocking queue is used to manage wall-building commands. This ensures synchronized communication between threads when queuing up and processing wall-building tasks.

### Thread Pool
A thread pool is implemented for parallelizing tasks such as robot movement and wall destruction. It optimizes performance by managing multiple threads, each handling specific game functionalities.

### Thread Safety
Thread safety is maintained by careful synchronization, avoiding unnecessary locking, and ensuring that critical sections of the code are protected from concurrent access.

### Graceful Thread Termination
The implementation follows best practices to allow all threads to end gracefully when the game concludes. This ensures a clean exit without resorting to system-level termination.

### Task Allocation
Tasks are appropriately allocated to threads based on their nature. Separate threads handle potentially long-running or blocking operations, optimizing the game's responsiveness.

### Logging
Events such as robot creation, wall building, and impacts are logged in an on-screen text area for player feedback.

## Usage
Clone the repository.
Run the Java code to view the GUI.
Click on empty squares to build walls, (only 10 walls can be built at a time) 

![image](https://github.com/NethmiSilva/Robot-Arena-Game/assets/91644460/5eb95c13-2e4a-42f9-b421-1769afcc1145)
