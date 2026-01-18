package solitaire.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class CardImageCache {
    private static final Map<String, Image> images = new HashMap<>();
    private static final String IMAGE_DIR = "assets/cards/";

    public static Image getCardImage(String filename) {
        if (!images.containsKey(filename)) {
            ImageIcon icon = new ImageIcon(IMAGE_DIR + filename);

            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
                images.put(filename, icon.getImage());
            } 
            else if (filename.equals("back.png")) {
                images.put(filename, createBluePatternedBack());
            }
            else {
                images.put(filename, createCardPlaceholder());
            }
        }
        return images.get(filename);
    }

    private static Image createBluePatternedBack() {
        int width = 72;
        int height = 96;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(70, 130, 200),
            width, height, new Color(30, 80, 150)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, 8, 8);

        g2.setColor(new Color(100, 150, 220, 180));

        for (int x = 0; x < width; x += 15) {
            for (int y = 0; y < height; y += 15) {
                drawDiamond(g2, x + 7, y + 7, 4);
            }
        }

        g2.setColor(new Color(20, 60, 120));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, width-2, height-2, 8, 8);

        g2.setColor(new Color(120, 160, 220, 100));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(4, 4, width-8, height-8, 6, 6);

        g2.dispose();
        return img;
    }

    private static void drawDiamond(Graphics2D g2, int x, int y, int size) {
        int[] xPoints = {x, x + size, x, x - size};
        int[] yPoints = {y - size, y, y + size, y};
        g2.fillPolygon(xPoints, yPoints, 4);
    }

    private static Image createCardPlaceholder() {
        int width = 72;
        int height = 96;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, 8, 8);

        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, width-2, height-2, 8, 8);

        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        String text = "?";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2 - 5);

        g2.dispose();
        return img;
    }
}
