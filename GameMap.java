package Project;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class GameMap {
    private ArrayList<Rectangle2D.Double> rooms;
    private ArrayList<Rectangle2D.Double> vents;
    private ArrayList<Ellipse2D.Double> tasks;
    private ArrayList<Boolean> taskStatus;
    private ArrayList<String> taskNames;
    private ArrayList<Rectangle2D.Double> walls;
    private Map<Integer, Integer> ventConnections;

    private final Color[] roomColors = {
            new Color(200, 200, 200), // Default
            new Color(180, 230, 180), // Storage
            new Color(230, 180, 180), // Reactor
            new Color(180, 180, 230)  // Admin
    };

    private final String[] roomNames = {
            "Cafeteria", "Weapons", "Navigation",
            "O2", "Shields", "Communications",
            "Storage", "Admin", "Reactor"
    };

    public GameMap() {
        rooms = new ArrayList<>();
        vents = new ArrayList<>();
        tasks = new ArrayList<>();
        taskStatus = new ArrayList<>();
        taskNames = new ArrayList<>();
        walls = new ArrayList<>();
        ventConnections = new HashMap<>();
        initializeMap();
    }

    private void initializeMap() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double x = i * 300 + 50; // Increased room spacing
                double y = j * 300 + 50;
                rooms.add(new Rectangle2D.Double(x, y, 250, 250)); // Larger rooms

                if (i < 2) vents.add(new Rectangle2D.Double(x + 200, y + 80, 50, 40));
                if (j < 2) vents.add(new Rectangle2D.Double(x + 80, y + 200, 40, 50));

                tasks.add(new Ellipse2D.Double(x + 60, y + 60, 30, 30));
                taskStatus.add(false);
                taskNames.add("Task " + (tasks.size()));

                if (i == 1 && j == 1) {
                    walls.add(new Rectangle2D.Double(x + 50, y + 30, 100, 20));
                    walls.add(new Rectangle2D.Double(x + 30, y + 80, 20, 80));
                }
            }
        }

        for (int i = 0; i < vents.size(); i++) {
            ventConnections.put(i, (i + 3) % vents.size());
        }
    }

    public void drawFullMap(Graphics2D g2d, Point playerPos) {
        AffineTransform oldTransform = g2d.getTransform();
        // Center map on player - Fixed centering calculation
        g2d.translate(400 - playerPos.x, 400 - playerPos.y);

        for (int i = 0; i < rooms.size(); i++) {
            g2d.setColor(roomColors[i % roomColors.length]);
            g2d.fill(rooms.get(i));
            g2d.setColor(Color.BLACK);
            g2d.draw(rooms.get(i));

            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.setColor(Color.BLACK);
            Rectangle2D room = rooms.get(i);
            String name = roomNames[i];
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(name, (float)(room.getX() + 10), (float)(room.getY() + 20));
        }

        g2d.setColor(new Color(100, 100, 100));
        for (Rectangle2D wall : walls) {
            g2d.fill(wall);
            g2d.setColor(Color.BLACK);
            g2d.draw(wall);
        }

        g2d.setColor(new Color(80, 80, 80));
        for (Rectangle2D vent : vents) {
            g2d.fill(vent);
            g2d.setColor(Color.BLACK);
            g2d.draw(vent);
        }

        for (int i = 0; i < tasks.size(); i++) {
            g2d.setColor(taskStatus.get(i) ? Color.GREEN : Color.ORANGE);
            g2d.fill(tasks.get(i));
            g2d.setColor(Color.BLACK);
            g2d.draw(tasks.get(i));
        }

        g2d.setTransform(oldTransform);
    }

    public void drawMiniMap(Graphics2D g2d, Point playerPos, int width, int height) {
        AffineTransform oldTransform = g2d.getTransform();

        int miniMapSize = Math.min(width, height) / 4;
        int miniMapX = width - miniMapSize - 20;
        int miniMapY = 20;

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(miniMapX, miniMapY, miniMapSize, miniMapSize, 20, 20);

        g2d.translate(miniMapX + miniMapSize / 2 - playerPos.x * 0.25, miniMapY + miniMapSize / 2 - playerPos.y * 0.25);
        double scale = miniMapSize / 1000.0; // Adjusted scale
        g2d.scale(scale, scale);

        for (int i = 0; i < rooms.size(); i++) {
            g2d.setColor(roomColors[i % roomColors.length].darker());
            g2d.fill(rooms.get(i));
        }

        g2d.setColor(new Color(80, 80, 80, 150));
        for (Rectangle2D vent : vents) {
            g2d.fill(vent);
        }

        for (int i = 0; i < tasks.size(); i++) {
            g2d.setColor(taskStatus.get(i) ? new Color(0, 255, 0, 150) : new Color(255, 165, 0, 150));
            g2d.fill(tasks.get(i));
        }

        g2d.setColor(Color.RED);
        g2d.fillOval(playerPos.x - 5, playerPos.y - 5, 10, 10);

        g2d.setTransform(oldTransform);
    }

    // FIXED: More flexible movement checking
    public boolean canMoveTo(Point position) {
        // Check if position is within any room
        for (Rectangle2D room : rooms) {
            if (room.contains(position.x, position.y)) {
                // Check if position intersects with any walls
                for (Rectangle2D wall : walls) {
                    if (wall.contains(position.x, position.y)) {
                        System.out.println("Movement blocked by wall at: " + position);
                        return false;
                    }
                }
                return true;
            }
        }

        // Allow movement in corridors between rooms (expanded bounds)
        if (position.x >= 40 && position.x <= 960 && position.y >= 40 && position.y <= 960) {
            // Check walls only
            for (Rectangle2D wall : walls) {
                if (wall.contains(position.x, position.y)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public boolean interactWithTask(Point playerPos) {
        for (int i = 0; i < tasks.size(); i++) {
            if (!taskStatus.get(i) && tasks.get(i).contains(playerPos)) {
                taskStatus.set(i, true);
                return true;
            }
        }
        return false;
    }

    public boolean interactWithVent(Point playerPos, Point[] ventDestination) {
        for (int i = 0; i < vents.size(); i++) {
            if (vents.get(i).contains(playerPos)) {
                int connectedVent = ventConnections.get(i);
                Rectangle2D vent = vents.get(connectedVent);
                ventDestination[0] = new Point(
                        (int)(vent.getX() + vent.getWidth()/2),
                        (int)(vent.getY() + vent.getHeight()/2));
                return true;
            }
        }
        return false;
    }

    public int getCompletedTasks() {
        return (int) taskStatus.stream().filter(status -> status).count();
    }

    public String getTaskName(int index) {
        return index < taskNames.size() ? taskNames.get(index) : "Unknown Task";
    }

    // Helper method to get map dimensions
    public int getWidth() {
        return 1000;
    }

    public int getHeight() {
        return 1000;
    }
}