package solitaire.model;

import java.awt.Dimension;
import javax.swing.*;

import solitaire.gui.SolitaireGUI;

public class KlondikeSolitaire {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Default look and feel applied.");
            }

         
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);

        
            Timer timer = new Timer(3000, e -> {
                splash.dispose(); 

                JFrame frame = new JFrame("Klondike Solitaire");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(true);
                SolitaireGUI gui = new SolitaireGUI();
                frame.add(gui);
                frame.pack();
                frame.setLocationRelativeTo(null); 
                frame.setMinimumSize(new Dimension(1024, 768));
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);

                System.out.println("Klondike Solitaire started successfully!");
            });

            timer.setRepeats(false);
            timer.start();
        });
    }
}
