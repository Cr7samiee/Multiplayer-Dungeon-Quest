package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;

public class MainMenu extends JPanel {
    private Clip backgroundMusic;
    private boolean soundEnabled = true;
    private boolean minimapEnabled = true;
    private Image backgroundImage;
    private static final String VERSION = "1.1";

    public MainMenu(JFrame frame) {
        loadAssets();
        setupUI(frame);
        if (soundEnabled) playBackgroundMusic();
    }

    private void loadAssets() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/menu_bg.png")).getImage();
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }
    }

    private void setupUI(JFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 0, 15, 0);

        JLabel title = new JLabel("AMONG US LITE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 64));
        title.setForeground(new Color(255, 50, 50));
        title.setBorder(BorderFactory.createEmptyBorder(50, 0, 100, 0));

        JLabel versionLabel = new JLabel("Version " + VERSION, SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        versionLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        addButton(buttonPanel, "START GAME", Color.RED, e -> startGame(frame), gbc);
        addButton(buttonPanel, "SETTINGS", Color.CYAN, e -> showSettings(frame), gbc);
        addButton(buttonPanel, "EXIT", Color.GRAY, e -> System.exit(0), gbc);

        add(title, gbc);
        add(buttonPanel, gbc);
        add(versionLabel, gbc);
    }

    private void addButton(JPanel panel, String text, Color bgColor, ActionListener action, GridBagConstraints gbc) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 60));
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.addActionListener(action);
        button.addMouseListener(new ButtonHoverEffect(button));
        panel.add(button, gbc);
    }

    private void startGame(JFrame frame) {
        stopBackgroundMusic();
        frame.getContentPane().removeAll();

        // FIXED: Create game panel and ensure it gets focus
        MiniAmongUsLightingMap gamePanel = new MiniAmongUsLightingMap(frame, soundEnabled, minimapEnabled);
        frame.add(gamePanel);
        frame.revalidate();
        frame.repaint();

        // FIXED: Ensure the game panel gets keyboard focus
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
    }

    private void showSettings(JFrame frame) {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JCheckBox soundCheck = new JCheckBox("Enable Sound", soundEnabled);
        JCheckBox minimapCheck = new JCheckBox("Show Minimap", minimapEnabled);

        soundCheck.setFont(new Font("Arial", Font.PLAIN, 18));
        minimapCheck.setFont(new Font("Arial", Font.PLAIN, 18));

        gbc.gridy = 0;
        settingsPanel.add(soundCheck, gbc);
        gbc.gridy = 1;
        settingsPanel.add(minimapCheck, gbc);

        int result = JOptionPane.showConfirmDialog(frame, settingsPanel, "Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            soundEnabled = soundCheck.isSelected();
            minimapEnabled = minimapCheck.isSelected();
            if (soundEnabled) playBackgroundMusic();
            else stopBackgroundMusic();
        }
    }

    private void playBackgroundMusic() {
        try {
            if (backgroundMusic == null || !backgroundMusic.isOpen()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                        getClass().getResource("/sounds/menu_music.wav"));
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioStream);
            }
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Error loading music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setPaint(new GradientPaint(0, 0, new Color(10, 10, 30), 0, getHeight(), new Color(40, 40, 80)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class ButtonHoverEffect extends MouseAdapter {
        private final JButton button;
        private final Color original;

        public ButtonHoverEffect(JButton button) {
            this.button = button;
            this.original = button.getBackground();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(original.brighter());
            button.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(original);
            button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Among Us Lite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.add(new MainMenu(frame));
        frame.setVisible(true);
    }
}