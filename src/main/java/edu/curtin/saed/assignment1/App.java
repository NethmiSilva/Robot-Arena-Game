package edu.curtin.saed.assignment1;

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class App {

    private static Map<Coordinates, Wall> walls;

    private static SwingArena arena;
    private static JTextArea logger;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Example App (Swing)");
            JToolBar toolbar = new JToolBar();
            JLabel commandsLabel = new JLabel("    Queued Commands: 0");
            JLabel label = new JLabel("Score: 0");
            toolbar.add(label);
            toolbar.add(commandsLabel);

            logger = new JTextArea();
            JScrollPane loggerArea = new JScrollPane(logger);
            loggerArea.setBorder(BorderFactory.createEtchedBorder());

            walls = new HashMap<>();
            arena = new SwingArena(logger, label, commandsLabel, walls);
            arena.initializeListeners();

            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, arena, loggerArea);

            Container contentPane = window.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(toolbar, BorderLayout.NORTH);
            contentPane.add(splitPane, BorderLayout.CENTER);
            logger.append("Game Start!" + "\n");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setPreferredSize(new Dimension(800, 800));
            window.pack();
            window.setVisible(true);

            splitPane.setDividerLocation(0.75);
        });

    }
}
