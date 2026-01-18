package solitaire.gui;

import solitaire.model.Card;
import solitaire.model.SolitaireGame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class SolitaireGUI extends JPanel {

    private static final int CARD_WIDTH = 72;
    private static final int CARD_HEIGHT = 96;
    private static final int CARD_GAP = 25;
    private static final int CARD_OVERLAP = 25;
    private static final int FOUNDATION_GAP = 15;
    private static final int SECTION_GAP = 80;     private static final int SCORE_PANEL_HEIGHT = 60; 

    private int topSectionY = 70;
    private int tableauSectionY;
    private int leftMargin;

    private final SolitaireGame game;
    private Card selectedCard = null;
    private int selectedSource = -1;
    private int selectedTableauIndex = -1;
    private int selectedCardIndex = -1;
    private Rectangle stockRect, wasteRect;
    private Rectangle[] foundationRects = new Rectangle[4];
    private Rectangle[][] tableauRects = new Rectangle[7][];

    private Card draggedCard = null;
    private int dragStartX, dragStartY;
    private int dragCurrentX, dragCurrentY;
    private boolean isDragging = false;
    private int dragSourceType = -1;
    private int dragSourceIndex = -1;
    private int dragCardIndex = -1;
    private List<Card> draggedCards = new ArrayList<>();
    private Rectangle dragHighlightRect = null;

    private Timer scoreUpdateTimer;
    private Color scoreBackgroundColor = new Color(0, 0, 0, 120);
    private Font scoreFont = new Font("Arial", Font.BOLD, 14);
    private Font titleFont = new Font("Arial", Font.BOLD, 16);

    public SolitaireGUI() {
        setPreferredSize(new Dimension(1200, 850));
        setBackground(new Color(0, 100, 0));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) {
                    handleUndo();
                } else if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
                    handleNewGame();
                } else if (e.getKeyCode() == KeyEvent.VK_H) {
                    showHints();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
        game = new SolitaireGame();

        game.startGame();

        scoreUpdateTimer = new Timer(1000, e -> repaint());
        scoreUpdateTimer.start();

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e.getX(), e.getY());
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        calculateDynamicPositions();
        updateClickableAreas();
    }

    private void calculateDynamicPositions() {
        int panelWidth = getWidth();
        if (panelWidth == 0) {
            panelWidth = 1200; 
        }
        
        int topSectionWidth = CARD_WIDTH + CARD_GAP + CARD_WIDTH + CARD_GAP + CARD_GAP
                + (4 * CARD_WIDTH) + (3 * FOUNDATION_GAP);

       
        int tableauWidth = (7 * CARD_WIDTH) + (6 * CARD_GAP);

      
        int maxWidth = Math.max(topSectionWidth, tableauWidth);
        leftMargin = Math.max(20, (panelWidth - maxWidth) / 2);

   
        tableauSectionY = topSectionY + CARD_HEIGHT + SECTION_GAP;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        calculateDynamicPositions();
        updateClickableAreas();
    }

    private void handleMousePressed(int x, int y) {
        System.out.println("Mouse pressed at: " + x + ", " + y);

        Card clickedCard = getCardAtPosition(x, y);
        if (clickedCard != null) {
            startDrag(x, y, clickedCard);
        } else {
            handleNonDragClick(x, y);
        }
    }

    private void handleMouseDragged(int x, int y) {
        if (isDragging) {
            dragCurrentX = x;
            dragCurrentY = y;
            updateDragHighlight(x, y);
            repaint();
        }
    }

    private void handleMouseReleased(int x, int y) {
        if (isDragging) {
            completeDrag(x, y);
        }
        clearDragState();
        repaint();
    }

    private void handleMouseMoved(int x, int y) {
    }

    private void startDrag(int x, int y, Card card) {
        draggedCard = card;
        dragStartX = x;
        dragStartY = y;
        dragCurrentX = x;
        dragCurrentY = y;
        isDragging = true;

        determineDragSource(card);

        System.out.println("Started dragging: " + card.getRank() + " of " + card.getSuit());
        repaint();
    }

    private void determineDragSource(Card card) {
        if (card == game.getTopWasteCard()) {
            dragSourceType = 0;
            draggedCards.clear();
            draggedCards.add(card);
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (card == game.getTopFoundationCard(i)) {
                dragSourceType = 10 + i;
                draggedCards.clear();
                draggedCards.add(card);
                return;
            }
        }

        for (int col = 0; col < 7; col++) {
            List<Card> pile = game.getTableauPile(col);
            for (int cardIndex = 0; cardIndex < pile.size(); cardIndex++) {
                if (pile.get(cardIndex) == card && card.isFaceUp()) {
                    if (isValidSequenceFromIndex(pile, cardIndex)) {
                        dragSourceType = col + 1;
                        dragSourceIndex = col;
                        dragCardIndex = cardIndex;

                        draggedCards.clear();
                        for (int i = cardIndex; i < pile.size(); i++) {
                            draggedCards.add(pile.get(i));
                        }
                        return;
                    }
                }
            }
        }
    }

    private Card getCardAtPosition(int x, int y) {
        for (int col = 0; col < 7; col++) {
            if (tableauRects[col] != null) {
                List<Card> pile = game.getTableauPile(col);
                for (int cardIndex = pile.size() - 1; cardIndex >= 0; cardIndex--) {
                    if (tableauRects[col][cardIndex] != null
                            && tableauRects[col][cardIndex].contains(x, y)) {
                        Card card = pile.get(cardIndex);
                        if (card.isFaceUp()) {
                            return card;
                        }
                    }
                }
            }
        }

        if (wasteRect != null && wasteRect.contains(x, y)) {
            return game.getTopWasteCard();
        }

        for (int i = 0; i < 4; i++) {
            if (foundationRects[i] != null && foundationRects[i].contains(x, y)) {
                return game.getTopFoundationCard(i);
            }
        }

        return null;
    }

    private void updateDragHighlight(int x, int y) {
        dragHighlightRect = null;

        for (int i = 0; i < 4; i++) {
            if (foundationRects[i] != null && foundationRects[i].contains(x, y)) {
                if (canDropOnFoundation(i)) {
                    dragHighlightRect = foundationRects[i];
                    return;
                }
            }
        }

        for (int col = 0; col < 7; col++) {
            if (tableauRects[col] != null && tableauRects[col].length > 0) {
                Rectangle dropRect = getTableauDropRect(col);
                if (dropRect.contains(x, y)) {
                    if (canDropOnTableau(col)) {
                        dragHighlightRect = dropRect;
                        return;
                    }
                }
            }
        }
    }

    private Rectangle getTableauDropRect(int col) {
        List<Card> pile = game.getTableauPile(col);
        if (pile.isEmpty()) {
            return tableauRects[col][0];
        } else {
            Rectangle lastCard = tableauRects[col][pile.size() - 1];
            return new Rectangle(lastCard.x, lastCard.y + CARD_OVERLAP,
                    CARD_WIDTH, CARD_HEIGHT);
        }
    }

    private boolean canDropOnFoundation(int foundationIndex) {
        if (draggedCards.size() != 1) {
            return false;
        }

        Card card = draggedCards.get(0);
        Card.Suit foundationSuit = getFoundationSuit(foundationIndex);

        if (card.getSuit() != foundationSuit) {
            return false;
        }

        Card topFoundationCard = game.getTopFoundationCard(foundationIndex);
        if (topFoundationCard == null) {
            return card.getRank() == Card.Rank.ACE;
        } else {
            return card.getRank().ordinal() == topFoundationCard.getRank().ordinal() + 1;
        }
    }

    private boolean canDropOnTableau(int tableauIndex) {
        if (draggedCards.isEmpty()) {
            return false;
        }

        Card bottomCard = draggedCards.get(0);
        List<Card> targetPile = game.getTableauPile(tableauIndex);

        return canPlaceOnTableau(bottomCard, targetPile);
    }

    private void completeDrag(int x, int y) {
        if (!isDragging || draggedCards.isEmpty()) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (foundationRects[i] != null && foundationRects[i].contains(x, y)) {
                if (canDropOnFoundation(i)) {
                    executeMoveToFoundation(i);
                    return;
                }
            }
        }

        for (int col = 0; col < 7; col++) {
            Rectangle dropRect = getTableauDropRect(col);
            if (dropRect.contains(x, y)) {
                if (canDropOnTableau(col)) {
                    executeMoveToTableau(col);
                    return;
                }
            }
        }

        System.out.println("Invalid drop location");
    }

    private void executeMoveToFoundation(int foundationIndex) {
        game.saveStateBeforeMove("Move to Foundation " + foundationIndex);

        Card cardToMove = null;
        if (dragSourceType == 0) {
            cardToMove = game.removeFromWaste();
        } else if (dragSourceType >= 1 && dragSourceType <= 7) {
            cardToMove = game.removeFromTableau(dragSourceType - 1);
        } else if (dragSourceType >= 10 && dragSourceType <= 13) {
            cardToMove = game.removeFromFoundation(dragSourceType - 10);
        }

        if (cardToMove != null) {
            game.addToFoundation(cardToMove, foundationIndex);
            updateClickableAreas();

            if (game.isGameWon()) {
                showWinDialog();
            }
        }
    }

    private void executeMoveToTableau(int tableauIndex) {
        game.saveStateBeforeMove("Move to Tableau " + tableauIndex);

        if (dragSourceType >= 1 && dragSourceType <= 7) {
            int sourceCol = dragSourceType - 1;
            if (sourceCol != tableauIndex) {
                List<Card> cardsToMove = new ArrayList<>();
                List<Card> sourcePile = game.getTableauPile(sourceCol);

                for (int i = sourcePile.size() - 1; i >= dragCardIndex; i--) {
                    Card card = game.removeFromTableau(sourceCol);
                    if (card != null) {
                        cardsToMove.add(0, card);
                    }
                }

                for (Card card : cardsToMove) {
                    game.addToTableau(card, tableauIndex);
                }
            }
        } else if (dragSourceType == 0) {
            Card card = game.removeFromWaste();
            if (card != null) {
                game.addToTableau(card, tableauIndex);
            }
        } else if (dragSourceType >= 10 && dragSourceType <= 13) {
            Card card = game.removeFromFoundation(dragSourceType - 10);
            if (card != null) {
                game.addToTableau(card, tableauIndex);
            }
        }

        updateClickableAreas();
    }

    private void handleNonDragClick(int x, int y) {
        if (stockRect != null && stockRect.contains(x, y)) {
            if (game.drawFromStock()) {
                updateClickableAreas();
                repaint();
            }
        }
    }

    private void clearDragState() {
        isDragging = false;
        draggedCard = null;
        draggedCards.clear();
        dragSourceType = -1;
        dragSourceIndex = -1;
        dragCardIndex = -1;
        dragHighlightRect = null;
    }

    private void updateClickableAreas() {

        calculateDynamicPositions();

        int stockX = leftMargin;
        int stockY = topSectionY;
        stockRect = new Rectangle(stockX, stockY, CARD_WIDTH, CARD_HEIGHT);

        int wasteX = stockX + CARD_WIDTH + CARD_GAP;
        int wasteY = topSectionY;
        wasteRect = new Rectangle(wasteX, wasteY, CARD_WIDTH, CARD_HEIGHT);

        int foundationStartX = wasteX + CARD_WIDTH + CARD_GAP * 2;
        for (int i = 0; i < 4; i++) {
            int fx = foundationStartX + i * (CARD_WIDTH + FOUNDATION_GAP);
            int fy = topSectionY;
            foundationRects[i] = new Rectangle(fx, fy, CARD_WIDTH, CARD_HEIGHT);
        }

        for (int col = 0; col < 7; col++) {
            List<Card> pile = game.getTableauPile(col);
            int x = leftMargin + col * (CARD_WIDTH + CARD_GAP);
            int y = tableauSectionY;

            int rectCount = Math.max(1, pile.size());
            tableauRects[col] = new Rectangle[rectCount];

            if (pile.isEmpty()) {
                tableauRects[col][0] = new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);
            } else {
                for (int i = 0; i < pile.size(); i++) {
                    tableauRects[col][i] = new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);
                    y += CARD_OVERLAP;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground((Graphics2D) g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawScorePanel(g2);

        if (dragHighlightRect != null) {
            g2.setColor(new Color(255, 255, 0, 100));
            g2.fillRoundRect(dragHighlightRect.x - 2, dragHighlightRect.y - 2,
                    dragHighlightRect.width + 4, dragHighlightRect.height + 4, 12, 12);
            g2.setColor(new Color(255, 255, 0, 200));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(dragHighlightRect.x - 2, dragHighlightRect.y - 2,
                    dragHighlightRect.width + 4, dragHighlightRect.height + 4, 12, 12);
            g2.setStroke(new BasicStroke(1));
        }

        int stockX = leftMargin;
        int stockY = topSectionY;
        drawCardSlot(g2, stockX, stockY);
        Card stockCard = game.getTopStockCard();
        if (stockCard != null) {
            drawCard(g2, stockCard, stockX, stockY, false, false);
        }

        int wasteX = stockX + CARD_WIDTH + CARD_GAP;
        int wasteY = topSectionY;
        drawCardSlot(g2, wasteX, wasteY);
        Card wasteCard = game.getTopWasteCard();
        boolean wasteBeingDragged = (isDragging && dragSourceType == 0);
        if (wasteCard != null && !wasteBeingDragged) {
            drawCard(g2, wasteCard, wasteX, wasteY, true, false);
        }

        int foundationStartX = wasteX + CARD_WIDTH + CARD_GAP * 2;
        for (int i = 0; i < 4; i++) {
            int fx = foundationStartX + i * (CARD_WIDTH + FOUNDATION_GAP);
            int fy = topSectionY;
            drawCardSlot(g2, fx, fy);
            Card foundationCard = game.getTopFoundationCard(i);
            boolean foundationBeingDragged = (isDragging && dragSourceType == 10 + i);
            if (foundationCard != null && !foundationBeingDragged) {
                drawCard(g2, foundationCard, fx, fy, true, false);
            }
        }

        for (int i = 0; i < 7; i++) {
            List<Card> pile = game.getTableauPile(i);
            int x = leftMargin + i * (CARD_WIDTH + CARD_GAP);
            int y = tableauSectionY;

            if (pile.isEmpty()) {
                drawCardSlot(g2, x, y);
            }

            for (int j = 0; j < pile.size(); j++) {
                Card card = pile.get(j);

                boolean cardBeingDragged = (isDragging && dragSourceType == i + 1
                        && dragCardIndex >= 0 && j >= dragCardIndex);

                if (!cardBeingDragged) {
                    drawCard(g2, card, x, y, card.isFaceUp(), false);
                }
                y += CARD_OVERLAP;
            }
        }

        if (isDragging && !draggedCards.isEmpty()) {
            int dragX = dragCurrentX - CARD_WIDTH / 2;
            int dragY = dragCurrentY - CARD_HEIGHT / 2;

            for (int i = 0; i < draggedCards.size(); i++) {
                Card card = draggedCards.get(i);

                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(dragX + 6, dragY + 6 + (i * CARD_OVERLAP),
                        CARD_WIDTH, CARD_HEIGHT, 10, 10);

                drawCard(g2, card, dragX, dragY + (i * CARD_OVERLAP), true, true);
            }
        }

        drawBottomInfo(g2);
    }

    private String formatScore(int score) {
        return String.format("%,d", score);
    }

    private int getTotalFoundationCards() {
        int total = 0;
        for (int i = 0; i < 4; i++) {
            total += game.getFoundationSize(i);
        }
        return total;
    }

    private void drawCardSlot(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));
        g2.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRoundRect(x + 2, y + 2, CARD_WIDTH - 4, CARD_HEIGHT - 4, 8, 8);
    }

    private void drawBottomInfo(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        String info = "Controls: Ctrl+Z = Undo | Ctrl+N = New Game | H = Hints";
        if (selectedCard != null) {
            info += " | Selected: " + selectedCard.getRank() + " of " + selectedCard.getSuit();
        }

        int textWidth = fm.stringWidth(info);
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() - 15;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(info, textX + 1, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(info, textX, textY);
    }

    private void drawBackground(Graphics2D g2) {
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 120, 0),
                0, getHeight(), new Color(0, 80, 0)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(0, 0, 0, 8));
        for (int i = 0; i < getWidth(); i += 6) {
            for (int j = 0; j < getHeight(); j += 6) {
                if ((i + j) % 12 == 0) {
                    g2.fillRect(i, j, 2, 2);
                }
            }
        }
    }

    private boolean isValidSequenceFromIndex(List<Card> pile, int startIndex) {
        if (startIndex < 0 || startIndex >= pile.size()) {
            return false;
        }

        Card startCard = pile.get(startIndex);
        if (!startCard.isFaceUp()) {
            return false;
        }

        if (startIndex == pile.size() - 1) {
            return true;
        }

        for (int i = startIndex; i < pile.size() - 1; i++) {
            Card current = pile.get(i);
            Card next = pile.get(i + 1);

            if (!current.isFaceUp() || !next.isFaceUp()) {
                return false;
            }

            boolean differentColor = isRed(current) != isRed(next);
            boolean descending = current.getRank().ordinal() == next.getRank().ordinal() + 1;

            if (!differentColor || !descending) {
                return false;
            }
        }

        return true;
    }

    private boolean canPlaceOnTableau(Card card, List<Card> targetPile) {
        if (targetPile.isEmpty()) {
            return card.getRank() == Card.Rank.KING;
        }

        Card topCard = targetPile.get(targetPile.size() - 1);
        if (!topCard.isFaceUp()) {
            return false;
        }

        boolean differentColor = isRed(card) != isRed(topCard);
        boolean descending = card.getRank().ordinal() == topCard.getRank().ordinal() - 1;

        return differentColor && descending;
    }

    private boolean isRed(Card card) {
        return card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS;
    }

    private Card.Suit getFoundationSuit(int index) {
        Card.Suit[] suits = {Card.Suit.HEARTS, Card.Suit.SPADES, Card.Suit.DIAMONDS, Card.Suit.CLUBS};
        if (index >= 0 && index < 4) {
            return suits[index];
        }
        return null;
    }

    private void handleUndo() {
        if (game.canUndo()) {
            if (game.undo()) {
                updateClickableAreas();
                repaint();
                showStatusMessage("Move undone! Score: -50 points", Color.ORANGE);
            }
        } else {
            showStatusMessage("No moves to undo!", Color.RED);
        }
    }

    private void handleNewGame() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Start a new game? Current progress will be lost.",
                "New Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (scoreUpdateTimer != null) {
                scoreUpdateTimer.stop();
            }

            SolitaireGUI newGui = new SolitaireGUI();

            Container parent = getParent();
            if (parent != null) {
                parent.removeAll();
                parent.add(newGui);
                parent.revalidate();
                parent.repaint();
                newGui.requestFocusInWindow();
            }
        }
    }

    private void showHints() {
        List<String> hints = generateHints();
        if (hints.isEmpty()) {
            showStatusMessage("No obvious moves available. Try drawing from stock!", Color.BLUE);
        } else {
            StringBuilder hintMessage = new StringBuilder("Possible moves:\n");
            for (int i = 0; i < Math.min(3, hints.size()); i++) {
                hintMessage.append("• ").append(hints.get(i)).append("\n");
            }

            JOptionPane.showMessageDialog(
                    this,
                    hintMessage.toString(),
                    "Hints",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private List<String> generateHints() {
        List<String> hints = new ArrayList<>();

        Card wasteCard = game.getTopWasteCard();
        if (wasteCard != null) {
            for (int i = 0; i < 4; i++) {
                if (game.canMoveToFoundation(wasteCard, i)) {
                    hints.add("Move " + wasteCard.getRank() + " of " + wasteCard.getSuit()
                            + " from waste to foundation");
                }
            }
        }

        for (int col = 0; col < 7; col++) {
            List<Card> pile = game.getTableauPile(col);
            if (!pile.isEmpty()) {
                Card topCard = pile.get(pile.size() - 1);
                if (topCard.isFaceUp()) {
                    for (int i = 0; i < 4; i++) {
                        if (game.canMoveToFoundation(topCard, i)) {
                            hints.add("Move " + topCard.getRank() + " of " + topCard.getSuit()
                                    + " from tableau " + (col + 1) + " to foundation");
                        }
                    }
                }
            }
        }

        for (int sourceCol = 0; sourceCol < 7; sourceCol++) {
            List<Card> sourcePile = game.getTableauPile(sourceCol);
            for (int cardIndex = 0; cardIndex < sourcePile.size(); cardIndex++) {
                Card card = sourcePile.get(cardIndex);
                if (card.isFaceUp() && isValidSequenceFromIndex(sourcePile, cardIndex)) {
                    for (int targetCol = 0; targetCol < 7; targetCol++) {
                        if (sourceCol != targetCol) {
                            List<Card> targetPile = game.getTableauPile(targetCol);
                            if (canPlaceOnTableau(card, targetPile)) {
                                hints.add("Move " + card.getRank() + " of " + card.getSuit()
                                        + " from tableau " + (sourceCol + 1) + " to tableau " + (targetCol + 1));
                            }
                        }
                    }
                }
            }
        }

        return hints;
    }

    private void showWinDialog() {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                    "🎉 CONGRATULATIONS! 🎉\n\n"
                    + "You won the game!\n\n"
                    + "📊 FINAL STATISTICS:\n"
                    + "Score: %,d points\n"
                    + "Moves: %d\n"
                    + "Time: %s\n"
                    + "Perfect Score: %s\n\n"
                    + "🏆 ACHIEVEMENT UNLOCKED! 🏆\n"
                    + "%s",
                    game.getScore(),
                    game.getMoves(),
                    game.getFormattedTime(),
                    isPerfectScore() ? "YES! ⭐" : "Not quite, but great job!",
                    getAchievementText()
            );

            String[] options = {"New Game", "Exit"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    message,
                    "Game Won!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                handleNewGame();
            } else {
                System.exit(0);
            }
        });
    }

    private boolean isPerfectScore() {
        int targetScore = 5000; 
        int maxMoves = 200;     
        long maxTimeSeconds = 600; 

        boolean highScore = game.getScore() >= targetScore;
        boolean efficientMoves = game.getMoves() <= maxMoves;
        boolean fastTime = (game.getElapsedTime() / 1000) <= maxTimeSeconds;

        return highScore && efficientMoves && fastTime;
    }

    private String getAchievementText() {
        int score = game.getScore();
        int moves = game.getMoves();
        long timeSeconds = game.getElapsedTime() / 1000;

        if (isPerfectScore()) {
            return "SOLITAIRE MASTER - Perfect Game!";
        } else if (score >= 4000) {
            return "EXPERT PLAYER - Excellent score!";
        } else if (score >= 2500) {
            return "SKILLED PLAYER - Great job!";
        } else if (moves <= 150) {
            return "EFFICIENT STRATEGIST - Great move economy!";
        } else if (timeSeconds <= 300) {
            return "SPEED DEMON - Lightning fast!";
        } else {
            return "PERSISTENT WINNER - Victory achieved!";
        }
    }

    private void showStatusMessage(String message, Color color) {
        JLabel statusLabel = new JLabel(message);
        statusLabel.setForeground(color);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 0, 0, 180));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        statusLabel.setBounds(
                getWidth() / 2 - 150,
                getHeight() - 100,
                300,
                40
        );

        add(statusLabel);
        repaint();

        Timer removeTimer = new Timer(3000, e -> {
            remove(statusLabel);
            repaint();
        });
        removeTimer.setRepeats(false);
        removeTimer.start();
    }

    private void drawScorePanel(Graphics2D g2) {
        GradientPaint scoreBg = new GradientPaint(
                0, 10, new Color(0, 0, 0, 150),
                0, SCORE_PANEL_HEIGHT, new Color(0, 0, 0, 100)
        );
        g2.setPaint(scoreBg);
        g2.fillRoundRect(10, 10, getWidth() - 20, SCORE_PANEL_HEIGHT - 10, 15, 15);

        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(10, 10, getWidth() - 20, SCORE_PANEL_HEIGHT - 10, 15, 15);

        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(12, 12, getWidth() - 24, SCORE_PANEL_HEIGHT - 14, 13, 13);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(255, 215, 0)); 
        g2.setFont(titleFont);
        FontMetrics titleFm = g2.getFontMetrics();
        String title = "🃏 KLONDIKE SOLITAIRE 🃏";
        int titleX = (getWidth() - titleFm.stringWidth(title)) / 2;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(title, titleX + 1, 31);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(title, titleX, 30);

        g2.setFont(scoreFont);
        FontMetrics fm = g2.getFontMetrics();

        int y = 50;
        int sectionWidth = (getWidth() - 40) / 4; 

        String scoreText = "Score: " + formatScore(game.getScore());
        Color scoreColor = getScoreColor(game.getScore());
        g2.setColor(scoreColor);
        g2.drawString(scoreText, 25, y);

        String movesText = "Moves: " + game.getMoves();
        Color moveColor = game.getMoves() <= 150 ? new Color(0, 255, 0) : Color.WHITE;
        g2.setColor(moveColor);
        g2.drawString(movesText, 25 + sectionWidth, y);

        String timeText = "Time: " + game.getFormattedTime();
        long timeSeconds = game.getElapsedTime() / 1000;
        Color timeColor = timeSeconds <= 300 ? new Color(0, 255, 0)
                : timeSeconds <= 600 ? new Color(255, 255, 0) : Color.WHITE;
        g2.setColor(timeColor);
        g2.drawString(timeText, 25 + sectionWidth * 2, y);

        if (game.isGameWon()) {
            String winText = isPerfectScore() ? "⭐ PERFECT!" : "🏆 WON!";
            g2.setColor(isPerfectScore() ? new Color(255, 215, 0) : new Color(0, 255, 0));
            g2.drawString(winText, 25 + sectionWidth * 3, y);
        } else {
            String statusText = "Cards Left: " + (52 - getTotalFoundationCards());
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(statusText, 25 + sectionWidth * 3, y);

            drawProgressBar(g2, 25 + sectionWidth * 3, y + 15, 100, 8);
        }
    }

    private Color getScoreColor(int score) {
        if (score >= 5000) {
            return new Color(255, 215, 0);
        }
        if (score >= 3000) {
            return new Color(0, 255, 0);
        }
        if (score >= 1500) {
            return new Color(255, 255, 0);
        }
        if (score >= 500) {
            return Color.WHITE;
        }
        return new Color(255, 100, 100);
    }

    private void drawProgressBar(Graphics2D g2, int x, int y, int width, int height) {
        int foundationCards = getTotalFoundationCards();
        double progress = foundationCards / 52.0;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(x, y, width, height, 4, 4);

        int fillWidth = (int) (width * progress);
        Color progressColor = progress < 0.3 ? new Color(255, 100, 100)
                : progress < 0.7 ? new Color(255, 255, 0)
                        : new Color(0, 255, 0);
        g2.setColor(progressColor);
        g2.fillRoundRect(x, y, fillWidth, height, 4, 4);

        g2.setColor(new Color(255, 255, 255, 150));
        g2.drawRoundRect(x, y, width, height, 4, 4);
    }

    private void drawCard(Graphics2D g2, Card card, int x, int y, boolean showFaceUp, boolean selected) {
        if (card == null) {
            return;
        }

        String filename = "back.png";
        if (showFaceUp && card.isFaceUp()) {
            filename = card.getImageFileName();
        }

        Image img = CardImageCache.getCardImage(filename);
        if (img != null) {
            boolean isImportantCard = isImportantForFoundation(card);
            if (!selected) {
                Color shadowColor = isImportantCard
                        ? new Color(255, 215, 0, 60) : new Color(0, 0, 0, 100);
                g2.setColor(shadowColor);
                g2.fillRoundRect(x + 4, y + 4, CARD_WIDTH, CARD_HEIGHT, 10, 10);
            }

            if (isImportantCard && showFaceUp && card.isFaceUp()) {
                g2.setColor(new Color(255, 215, 0, 40));
                g2.fillRoundRect(x - 2, y - 2, CARD_WIDTH + 4, CARD_HEIGHT + 4, 12, 12);
            }

            if (selected) {
                long time = System.currentTimeMillis();
                int alpha = (int) (180 + 75 * Math.sin(time / 200.0));
                g2.setColor(new Color(255, 215, 0, alpha));
                g2.fillRoundRect(x - 3, y - 3, CARD_WIDTH + 6, CARD_HEIGHT + 6, 12, 12);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(255, 255, 0, 220));
                g2.drawRoundRect(x - 3, y - 3, CARD_WIDTH + 6, CARD_HEIGHT + 6, 12, 12);
                g2.setStroke(new BasicStroke(1));
            }

            Color borderColor = isImportantCard
                    ? new Color(255, 215, 0, 80) : new Color(255, 255, 255, 50);
            g2.setColor(borderColor);
            g2.drawRoundRect(x - 1, y - 1, CARD_WIDTH + 2, CARD_HEIGHT + 2, 8, 8);

            g2.drawImage(img, x, y, CARD_WIDTH, CARD_HEIGHT, this);

            if (showFaceUp && card.isFaceUp()
                    && (card.getRank() == Card.Rank.ACE || card.getRank() == Card.Rank.KING)) {
                g2.setColor(new Color(255, 215, 0, 200));
                g2.fillOval(x + CARD_WIDTH - 15, y + 5, 10, 10);
            }
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);

            if (showFaceUp && card.isFaceUp()) {
                g2.setColor(isRed(card) ? Color.RED : Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String cardText = card.getRank().toString().charAt(0)
                        + getSuitSymbol(card.getSuit());
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (CARD_WIDTH - fm.stringWidth(cardText)) / 2;
                int textY = y + (CARD_HEIGHT + fm.getHeight()) / 2;
                g2.drawString(cardText, textX, textY);
            }
        }
    }

    private boolean isImportantForFoundation(Card card) {
        if (card == null || !card.isFaceUp()) {
            return false;
        }

        if (card.getRank() == Card.Rank.ACE) {
            return true;
        }

        for (int i = 0; i < 4; i++) {
            Card topFoundation = game.getTopFoundationCard(i);
            if (topFoundation != null
                    && card.getSuit() == topFoundation.getSuit()
                    && card.getRank().ordinal() == topFoundation.getRank().ordinal() + 1) {
                return true;
            }
        }

        return false;
    }

    private String getSuitSymbol(Card.Suit suit) {
        switch (suit) {
            case HEARTS:
                return "♥";
            case DIAMONDS:
                return "♦";
            case CLUBS:
                return "♣";
            case SPADES:
                return "♠";
            default:
                return "?";
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (scoreUpdateTimer != null) {
            scoreUpdateTimer.stop();
        }
    }
}    