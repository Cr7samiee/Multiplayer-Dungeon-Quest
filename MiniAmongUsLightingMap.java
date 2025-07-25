package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.sound.sampled.*;
import java.util.ArrayList;

public class MiniAmongUsLightingMap extends JPanel implements KeyListener {
    private enum GameState { PLAYING, MAP_VIEW, PAUSED, GAME_OVER, GAME_WON }
    private GameState gameState = GameState.PLAYING;
    private GameMap gameMap;
    private Point2D.Double playerPos = new Point2D.Double(175, 175); // Start inside a room
    private Point2D.Double playerVelocity = new Point2D.Double(0, 0);
    private boolean soundEnabled;
    private boolean showMiniMap;
    private long startTime;
    private int timeLimit = 300;
    private Clip movementSound;
    private Clip backgroundMusic;
    private Timer gameTimer;
    private ArrayList<String> taskMessages;
    private long lastTaskMessageTime;

    // FIXED: Add key state tracking for smoother movement
    private boolean[] keyPressed = new boolean[256];
    private final double MOVE_SPEED = 3.0;

    public MiniAmongUsLightingMap(JFrame frame, boolean soundEnabled, boolean showMiniMap) {
        this.soundEnabled = soundEnabled;
        this.showMiniMap = showMiniMap;
        gameMap = new GameMap();
        taskMessages = new ArrayList<>();
        setupGame(frame);
        loadSounds();
        startTimer();
    }

    private void setupGame(JFrame frame) {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // FIXED: Force focus and add mouse listener to ensure focus
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        startTime = System.currentTimeMillis();
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });

        // FIXED: Improved movement timer with key state checking
        Timer movementTimer = new Timer(16, e -> {
            if (gameState == GameState.PLAYING || gameState == GameState.MAP_VIEW) {
                updateMovementFromKeys();
                updatePlayerPosition();
                repaint();
            }
        });
        movementTimer.start();
    }

    // FIXED: New method to handle continuous movement
    private void updateMovementFromKeys() {
        playerVelocity.x = 0;
        playerVelocity.y = 0;

        if (keyPressed[KeyEvent.VK_LEFT] || keyPressed[KeyEvent.VK_A]) {
            playerVelocity.x = -MOVE_SPEED;
        }
        if (keyPressed[KeyEvent.VK_RIGHT] || keyPressed[KeyEvent.VK_D]) {
            playerVelocity.x = MOVE_SPEED;
        }
        if (keyPressed[KeyEvent.VK_UP] || keyPressed[KeyEvent.VK_W]) {
            playerVelocity.y = -MOVE_SPEED;
        }
        if (keyPressed[KeyEvent.VK_DOWN] || keyPressed[KeyEvent.VK_S]) {
            playerVelocity.y = MOVE_SPEED;
        }
    }

    private void loadSounds() {
        try {
            AudioInputStream moveStream = AudioSystem.getAudioInputStream(
                    getClass().getResource("/sounds/move.wav"));
            movementSound = AudioSystem.getClip();
            movementSound.open(moveStream);

            AudioInputStream musicStream = AudioSystem.getAudioInputStream(
                    getClass().getResource("/sounds/game_music.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(musicStream);
            if (soundEnabled) {
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.out.println("Error loading sounds: " + e.getMessage());
        }
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            if (gameState == GameState.PLAYING) {
                repaint();
                if (getRemainingTime() <= 0) {
                    gameState = GameState.GAME_OVER;
                }
                if (gameMap.getCompletedTasks() >= 9) {
                    gameState = GameState.GAME_WON;
                }
            }
        });
        gameTimer.start();
    }

    private int getRemainingTime() {
        int remaining = timeLimit - (int)((System.currentTimeMillis() - startTime) / 1000);
        return Math.max(0, remaining);
    }

    private void updatePlayerPosition() {
        if (playerVelocity.x == 0 && playerVelocity.y == 0) return;

        // FIXED: Check both x and y movement separately for better collision
        Point2D.Double newPos = new Point2D.Double(playerPos.x, playerPos.y);

        // Try horizontal movement
        if (playerVelocity.x != 0) {
            Point testX = new Point((int)(playerPos.x + playerVelocity.x), (int)playerPos.y);
            if (gameMap.canMoveTo(testX)) {
                newPos.x += playerVelocity.x;
            }
        }

        // Try vertical movement
        if (playerVelocity.y != 0) {
            Point testY = new Point((int)newPos.x, (int)(playerPos.y + playerVelocity.y));
            if (gameMap.canMoveTo(testY)) {
                newPos.y += playerVelocity.y;
            }
        }

        // Update position if it changed
        if (newPos.x != playerPos.x || newPos.y != playerPos.y) {
            playerPos = newPos;
            if (soundEnabled && movementSound != null && !movementSound.isRunning()) {
                movementSound.setFramePosition(0);
                movementSound.start();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case PLAYING:
                drawGameView(g2d);
                break;
            case MAP_VIEW:
                drawMapView(g2d);
                break;
            case PAUSED:
                drawGameView(g2d);
                drawPauseOverlay(g2d); // FIXED: Add pause overlay
                break;
            case GAME_OVER:
                drawGameOverView(g2d);
                break;
            case GAME_WON:
                drawGameWonView(g2d);
                break;
        }
    }

    private void drawGameView(Graphics2D g2d) {
        gameMap.drawFullMap(g2d, new Point((int)playerPos.x, (int)playerPos.y));
        drawLightingEffect(g2d);
        drawPlayer(g2d);
        if (showMiniMap) gameMap.drawMiniMap(g2d, new Point((int)playerPos.x, (int)playerPos.y), getWidth(), getHeight());
        drawHUD(g2d);
        drawTaskMessages(g2d);
    }

    // FIXED: Add pause overlay instead of dialog
    private void drawPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String pauseText = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(pauseText)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(pauseText, x, y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String resumeText = "Press P to resume or ESC for menu";
        fm = g2d.getFontMetrics();
        x = (getWidth() - fm.stringWidth(resumeText)) / 2;
        y += 50;
        g2d.drawString(resumeText, x, y);
    }

    private void drawMapView(Graphics2D g2d) {
        gameMap.drawFullMap(g2d, new Point((int)playerPos.x, (int)playerPos.y));
        drawPlayer(g2d);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "Press M to return to game";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight()/2 + 200);
    }

    private void drawGameOverView(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));

        String gameOverText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 50;
        g2d.drawString(gameOverText, x, y);

        g2d.setColor(Color.WHITE);
        String tasksText = "Tasks: " + gameMap.getCompletedTasks() + "/9";
        x = (getWidth() - fm.stringWidth(tasksText)) / 2;
        y += 70;
        g2d.drawString(tasksText, x, y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String returnMenuText = "Press ESC to return to menu";
        x = (getWidth() - fm.stringWidth(returnMenuText)) / 2;
        y += 60;
        g2d.drawString(returnMenuText, x, y);
    }

    private void drawGameWonView(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));

        String winText = "VICTORY!";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(winText)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 50;
        g2d.drawString(winText, x, y);

        g2d.setColor(Color.WHITE);
        String timeText = "Time Remaining: " + getRemainingTime() + "s";
        x = (getWidth() - fm.stringWidth(timeText)) / 2;
        y += 70;
        g2d.drawString(timeText, x, y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String returnMenuText = "Press ESC to return to menu";
        x = (getWidth() - fm.stringWidth(returnMenuText)) / 2;
        y += 60;
        g2d.drawString(returnMenuText, x, y);
    }

    private void drawLightingEffect(Graphics2D g2d) {
        if (gameState != GameState.PLAYING) return;

        // FIXED: Improved lighting calculation
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Transform player position to screen coordinates
        int screenX = getWidth() / 2;
        int screenY = getHeight() / 2;

        RadialGradientPaint light = new RadialGradientPaint(
                screenX, screenY, 200,
                new float[]{0.0f, 0.8f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 100),
                        new Color(0, 0, 0, 220)});
        g2d.setPaint(light);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawPlayer(Graphics2D g2d) {
        double scale = 1.0 + 0.05 * Math.sin(System.currentTimeMillis() / 200.0);
        AffineTransform oldTransform = g2d.getTransform();

        // FIXED: Draw player at screen center since map moves around player
        int screenX = getWidth() / 2;
        int screenY = getHeight() / 2;

        g2d.translate(screenX, screenY);
        g2d.scale(scale, scale);

        g2d.setColor(Color.BLUE);
        g2d.fillOval(-15, -20, 30, 40);
        g2d.setColor(new Color(0, 191, 255));
        g2d.fillRect(-10, -30, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(-15, -20, 30, 40);

        g2d.setTransform(oldTransform);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillRoundRect(10, 10, 300, 140, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Time Left: " + getRemainingTime() + "s", 20, 35);
        g2d.drawString("Tasks: " + gameMap.getCompletedTasks() + "/9", 20, 65);
        g2d.drawString("Controls:", 20, 95);
        g2d.drawString("WASD/Arrow - Move", 20, 115);
        g2d.drawString("M - Map | P - Pause | E - Interact", 20, 135);
    }

    private void drawTaskMessages(Graphics2D g2d) {
        long currentTime = System.currentTimeMillis();
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        int y = getHeight() - 100;

        for (int i = taskMessages.size() - 1; i >= 0; i--) {
            if (currentTime - lastTaskMessageTime > 3000) {
                taskMessages.remove(i);
                continue;
            }
            String message = taskMessages.get(i);
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.drawString(message, x, y);
            y -= 25;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed: " + e.getKeyCode());

        // FIXED: Set key state for movement keys
        if (e.getKeyCode() < keyPressed.length) {
            keyPressed[e.getKeyCode()] = true;
        }

        // Handle special keys
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (gameState == GameState.GAME_OVER || gameState == GameState.GAME_WON) {
                returnToMenu();
                return;
            } else if (gameState == GameState.PAUSED || gameState == GameState.MAP_VIEW) {
                gameState = GameState.PLAYING;
                if (gameTimer != null) gameTimer.start();
                if (soundEnabled && backgroundMusic != null) backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                return;
            }
        }

        if (gameState != GameState.GAME_OVER && gameState != GameState.GAME_WON) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_M:
                    toggleMapView();
                    break;
                case KeyEvent.VK_P:
                    togglePause(); // FIXED: Use toggle instead of dialog
                    break;
                case KeyEvent.VK_E:
                    handleInteraction();
                    break;
            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // FIXED: Clear key state for movement keys
        if (e.getKeyCode() < keyPressed.length) {
            keyPressed[e.getKeyCode()] = false;
        }
    }

    private void toggleMapView() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.MAP_VIEW;
            if (gameTimer != null) gameTimer.stop();
        } else if (gameState == GameState.MAP_VIEW) {
            gameState = GameState.PLAYING;
            if (gameTimer != null) gameTimer.start();
        }
    }

    // FIXED: Simple pause toggle instead of dialog
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            if (gameTimer != null) gameTimer.stop();
            if (backgroundMusic != null) backgroundMusic.stop();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            if (gameTimer != null) gameTimer.start();
            if (soundEnabled && backgroundMusic != null) backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void handleInteraction() {
        if (gameState != GameState.PLAYING) return;

        if (gameMap.interactWithTask(new Point((int)playerPos.x, (int)playerPos.y))) {
            if (soundEnabled) playSound("/sounds/task_complete.wav");
            taskMessages.add("Task Completed! " + gameMap.getCompletedTasks() + "/9");
            lastTaskMessageTime = System.currentTimeMillis();
        }

        Point[] ventDestination = new Point[1];
        if (gameMap.interactWithVent(new Point((int)playerPos.x, (int)playerPos.y), ventDestination)) {
            if (ventDestination[0] != null) {
                playerPos = new Point2D.Double(ventDestination[0].x, ventDestination[0].y);
                if (soundEnabled) playSound("/sounds/vent.wav");
                taskMessages.add("Vent Used!");
                lastTaskMessageTime = System.currentTimeMillis();
            }
        }
    }

    private void returnToMenu() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        if (movementSound != null && movementSound.isOpen()) {
            movementSound.close();
        }
        if (backgroundMusic != null && backgroundMusic.isOpen()) {
            backgroundMusic.close();
        }

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.add(new MainMenu(frame));
        frame.revalidate();
        frame.repaint();
    }

    private void playSound(String soundFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    getClass().getResource(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}