package Project.map;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class GameMap {
    private ArrayList<Rectangle2D.Double> rooms;
    private ArrayList<Rectangle2D.Double> vents;
    private ArrayList<Ellipse2D.Double> tasks;
    private Color roomColor = new Color(200, 200, 200);
    private Color ventColor = new Color(100, 100, 100);
    private Color taskColor = new Color(255, 165, 0);

    public GameMap() {
        rooms = new ArrayList<>();
        vents = new ArrayList<>();
        tasks = new ArrayList<>();
        initializeMap();
    }

    private void initializeMap() {
        // Create 3x3 grid of rooms
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double x = i * 250 + 50;
                double y = j * 250 + 50;
                rooms.add(new Rectangle2D.Double(x, y, 200, 200));

                // Add vents between rooms
                if (i < 2) vents.add(new Rectangle2D.Double(x + 200, y + 80, 50, 40));
                if (j < 2) vents.add(new Rectangle2D.Double(x + 80, y + 200, 40, 50));

                // Add tasks (circular)
                tasks.add(new Ellipse2D.Double(x + 60, y + 60, 30, 30));
            }
        }
    }

    public void drawFullMap(Graphics2D g2d, Point playerPos) {
        // Draw all map elements
        g2d.setColor(roomColor);
        for (Rectangle2D room : rooms) g2d.fill(room);

        g2d.setColor(ventColor);
        for (Rectangle2D vent : vents) g2d.fill(vent);

        g2d.setColor(taskColor);
        for (Ellipse2D task : tasks) g2d.fill(task);

        // Draw player position
        g2d.setColor(Color.BLUE);
        g2d.fillOval(playerPos.x - 10, playerPos.y - 10, 20, 20);
    }

    public void drawMiniMap(Graphics2D g2d, Point playerPos, int width, int height) {
        // Save original transform
        AffineTransform oldTransform = g2d.getTransform();

        // Set up mini-map area (top-right corner)
        int miniMapWidth = 150;
        int miniMapHeight = 150;
        int miniMapX = width - miniMapWidth - 20;
        int miniMapY = 20;

        // Draw mini-map background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(miniMapX, miniMapY, miniMapWidth, miniMapHeight);

        // Scale and position the mini-map
        g2d.translate(miniMapX, miniMapY);
        double scale = 0.2;
        g2d.scale(scale, scale);

        // Draw mini-map elements
        g2d.setColor(roomColor);
        for (Rectangle2D room : rooms) g2d.fill(room);

        g2d.setColor(ventColor);
        for (Rectangle2D vent : vents) g2d.fill(vent);

        // Draw player on mini-map
        g2d.setColor(Color.RED);
        g2d.fillOval(playerPos.x - 5, playerPos.y - 5, 10, 10);

        // Restore original transform
        g2d.setTransform(oldTransform);

        // Draw mini-map border
        g2d.setColor(Color.WHITE);
        g2d.drawRect(miniMapX, miniMapY, miniMapWidth, miniMapHeight);
    }
}