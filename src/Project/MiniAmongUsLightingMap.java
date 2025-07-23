package Project;

import Project.map.GameMap;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

class MiniAmongUsLightingMap extends JPanel implements KeyListener {
    private enum GameState { PLAYING, MAP_VIEW }
    private GameState gameState = GameState.PLAYING;
    private GameMap gameMap;
    private int playerX = 300;
    private int playerY = 300;
    private boolean showMiniMap = true;

    public MiniAmongUsLightingMap() {
        JFrame frame = new JFrame("Mini Among Us with Lighting and Decorations");
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setVisible(true);

        gameMap = new GameMap();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameState == GameState.MAP_VIEW) {
            // Full map view
            gameMap.drawFullMap(g2d, new Point(playerX, playerY));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Press M to return to game", 300, 30);
        } else {
            // Normal game view
            gameMap.drawFullMap(g2d, new Point(playerX, playerY));

            // Lighting effect
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            RadialGradientPaint light = new RadialGradientPaint(
                    new Point2D.Double(playerX, playerY), 200,
                    new float[]{0.0f, 0.8f, 1.0f},
                    new Color[]{new Color(0, 0, 0, 0),
                            new Color(0, 0, 0, 0),
                            new Color(0, 0, 0, 220)});
            g2d.setPaint(light);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw player
            g2d.setColor(Color.BLUE);
            g2d.fillOval(playerX - 10, playerY - 10, 20, 20);

            // Draw mini-map if enabled
            if (showMiniMap) {
                gameMap.drawMiniMap(g2d, new Point(playerX, playerY), getWidth(), getHeight());
            }

            // Draw controls hint
            g2d.setColor(Color.WHITE);
            g2d.drawString("Press M for map view", 10, 20);
        }
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: playerX -= 5; break;
            case KeyEvent.VK_RIGHT: playerX += 5; break;
            case KeyEvent.VK_UP: playerY -= 5; break;
            case KeyEvent.VK_DOWN: playerY += 5; break;
            case KeyEvent.VK_M:
                gameState = (gameState == GameState.PLAYING) ? GameState.MAP_VIEW : GameState.PLAYING;
                break;
            case KeyEvent.VK_N:
                showMiniMap = !showMiniMap;
                break;
        }
        repaint();
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new MiniAmongUsLightingMap();
    }
}