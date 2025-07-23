package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

class MiniAmongUsLightingMap extends JPanel implements KeyListener {
    int playerX = 300;
    int playerY = 300;

    public MiniAmongUsLightingMap() {
        JFrame frame = new JFrame("Mini Among Us with Lighting and Decorations");
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setVisible(true);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw rooms and decorations
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = i * 250 + 50;
                int y = j * 250 + 50;
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(x, y, 200, 200);

                // Decorate rooms
                g2d.setColor(Color.ORANGE);
                g2d.fillRect(x + 50, y + 50, 30, 30); // Example task panel
                g2d.setColor(Color.BLACK);
                g2d.drawString("Room " + (i * 3 + j + 1), x + 70, y + 20);
            }
        }

        // Dim entire map
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Radial gradient effect (light around player)
        float[] dist = {0.0f, 0.8f, 1.0f};
        Color[] colors = {new Color(0, 0, 0, 0),new Color(0, 0, 0, 0), new Color(0, 0, 0, 220)};
        RadialGradientPaint light = new RadialGradientPaint(new Point2D.Double(playerX, playerY), 200, dist, colors);
        g2d.setPaint(light);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw player
        g2d.setColor(Color.BLUE);
        g2d.fillOval(playerX - 10, playerY - 10, 20, 20);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) playerX -= 5;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) playerX += 5;
        if (e.getKeyCode() == KeyEvent.VK_UP) playerY -= 5;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) playerY += 5;
        repaint();
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new MiniAmongUsLightingMap();
    }
}