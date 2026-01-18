package solitaire.model;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame {

    private Timer resizeTimer;
    private int targetWidth = 700;
    private int targetHeight = 450;
    private int currentWidth = 200;
    private int currentHeight = 150;

    public SplashScreen() {
        setUndecorated(true);
        setSize(currentWidth, currentHeight);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(10, 40, 100));
        setLayout(new BorderLayout());

        JLabel title = new JLabel(" KLONDIKE SOLITAIRE ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel loading = new JLabel("Loading...", SwingConstants.CENTER);
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loading.setForeground(Color.LIGHT_GRAY);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(title, BorderLayout.CENTER);
        centerPanel.add(loading, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        startResizeAnimation();
    }

    private void startResizeAnimation() {
        int delay = 10;
        resizeTimer = new Timer(delay, e -> {
            currentWidth += 10;
            currentHeight += 6;

            if (currentWidth >= targetWidth && currentHeight >= targetHeight) {
                resizeTimer.stop();
                setSize(targetWidth, targetHeight);
                setLocationRelativeTo(null);
            } else {
                setSize(currentWidth, currentHeight);
                setLocationRelativeTo(null);
            }
        });
        resizeTimer.start();
    }
}
