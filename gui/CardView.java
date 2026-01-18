package solitaire.gui;

import solitaire.model.Card;
import javax.swing.*;
import java.awt.*;

public class CardView extends JPanel {

    private final Card cardModel;

    public CardView(Card card) {
        this.cardModel = card;
        setPreferredSize(new Dimension(80, 120));
        setSize(80, 120);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        if (cardModel.isFaceUp()) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(new Color(0, 0, 139));
        }

        g2d.fillRoundRect(0, 0, width - 3, height - 3, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, width - 3, height - 3, 10, 10);

        if (cardModel.isFaceUp()) {
            g2d.setColor(cardModel.isRed() ? Color.RED : Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String rank = cardModel.getRank().name().substring(0, 1);
            String suit = cardModel.getSuit().name().substring(0, 1);
            g2d.drawString(rank + suit, 10, 20);
        }

        g2d.dispose();
    }

    public Card getCardModel() {
        return cardModel;
    }
}
