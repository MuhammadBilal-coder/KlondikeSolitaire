
package solitaire.model;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.ArrayList;

public class DraggableCard extends JPanel implements DragGestureListener, DragSourceListener, Transferable {

    private static final long serialVersionUID = 1L;

    
    private String suit;
    private String rank;
    private boolean faceUp;
    private BufferedImage cardImage;
    private DragSource dragSource;
    private boolean isDragging = false;
    private Point dragOffset;

   
    private Timer animationTimer;
    private Point targetPosition;
    private Point currentPosition;
    private int animationSpeed = 20;
    private double animationProgress = 0.0;

    
    private boolean isHighlighted = false;
    private Color highlightColor = new Color(255, 255, 0, 100);
    private Color shadowColor = new Color(0, 0, 0, 50);

    public DraggableCard(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;

        setPreferredSize(new Dimension(80, 120));
        setSize(80, 120);

     
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);

       
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isDragging) {
                    setHighlighted(true);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isDragging) {
                    setHighlighted(false);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

       
        if (isDragging) {
            g2d.setColor(shadowColor);
            g2d.fillRoundRect(3, 3, width - 3, height - 3, 10, 10);
        }

        if (faceUp) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(new Color(0, 0, 139)); 
        }
        g2d.fillRoundRect(0, 0, width - 3, height - 3, 10, 10);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, width - 3, height - 3, 10, 10);

        if (faceUp) {
            drawCardFace(g2d, width, height);
        } else {
            drawCardBack(g2d, width, height);
        }

        if (isHighlighted) {
            g2d.setColor(highlightColor);
            g2d.fillRoundRect(0, 0, width - 3, height - 3, 10, 10);
        }

        g2d.dispose();
    }

    private void drawCardFace(Graphics2D g2d, int width, int height) {

        Color suitColor = (suit.equals("Hearts") || suit.equals("Diamonds")) ? Color.RED : Color.BLACK;
        g2d.setColor(suitColor);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int rankWidth = fm.stringWidth(rank);
        g2d.drawString(rank, 5, 20);
        g2d.drawString(rank, width - rankWidth - 5, height - 5);

        String suitSymbol = getSuitSymbol();
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        int symbolWidth = fm.stringWidth(suitSymbol);
        int symbolHeight = fm.getHeight();

        g2d.drawString(suitSymbol, (width - symbolWidth) / 2, (height + symbolHeight) / 2);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(suitSymbol, 5, 35);

        g2d.rotate(Math.PI, width / 2.0, height / 2.0);
        g2d.drawString(suitSymbol, 5, 35);
    }

    private void drawCardBack(Graphics2D g2d, int width, int height) {
       
        g2d.setColor(Color.WHITE);
        for (int i = 10; i < width - 10; i += 15) {
            for (int j = 10; j < height - 10; j += 15) {
                g2d.fillOval(i, j, 5, 5);
            }
        }
    }

    private String getSuitSymbol() {
        switch (suit) {
            case "Hearts":
                return "♥";
            case "Diamonds":
                return "♦";
            case "Clubs":
                return "♣";
            case "Spades":
                return "♠";
            default:
                return "?";
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        dragOffset = dge.getDragOrigin();
        isDragging = true;
        setCursor(new Cursor(Cursor.MOVE_CURSOR));

        BufferedImage dragImage = createDragImage();
        Point imageOffset = new Point(-dragOffset.x, -dragOffset.y);

        dge.startDrag(null, dragImage, imageOffset, this, this);
    }

    private BufferedImage createDragImage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        paint(g2d);
        g2d.dispose();
        return image;
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
       
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
        
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
        
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
       
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        isDragging = false;
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        if (!dsde.getDropSuccess()) {
            
            animateToPosition(getLocation());
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return suit + ":" + rank;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public void animateToPosition(Point target) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        targetPosition = target;
        currentPosition = getLocation();
        animationProgress = 0.0;

        animationTimer = new Timer(16, new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                animationProgress += 0.1;

                if (animationProgress >= 1.0) {
                    animationProgress = 1.0;
                    animationTimer.stop();
                }

                double eased = easeInOutQuad(animationProgress);

                int x = (int) (currentPosition.x + (targetPosition.x - currentPosition.x) * eased);
                int y = (int) (currentPosition.y + (targetPosition.y - currentPosition.y) * eased);

                setLocation(x, y);
                repaint();
            }
        });

        animationTimer.start();
    }

    private double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    // Getters and setters
    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        repaint();
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        repaint();
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public boolean isDragging() {
        return isDragging;
    }
}

class DropZonePile extends JPanel implements DropTargetListener {

    private static final long serialVersionUID = 1L;

    public enum PileType {
        FOUNDATION, 
        TABLEAU, 
        STOCK, 
        WASTE  
    }

    private ArrayList<DraggableCard> cards;
    private DropTarget dropTarget;
    private boolean isValidDropZone = false;
    private Color validDropColor = new Color(0, 255, 0, 50);
    private Color invalidDropColor = new Color(255, 0, 0, 50);
    private PileType pileType;
    private int maxVisibleCards = 3; 

    public DropZonePile(PileType type) {
        this.pileType = type;
        cards = new ArrayList<>();
        setLayout(null);

        
        switch (type) {
            case FOUNDATION:
                setPreferredSize(new Dimension(100, 150));
                break;
            case TABLEAU:
                setPreferredSize(new Dimension(100, 300));
                break;
            case STOCK:
            case WASTE:
                setPreferredSize(new Dimension(100, 150));
                break;
        }

        dropTarget = new DropTarget(this, this);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

       
        if (isValidDropZone) {
            g2d.setColor(validDropColor);
            g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{5, 5}, 0));
            g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
        }

        if (cards.isEmpty()) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.fillRoundRect(10, 10, 80, 120, 10, 10);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{10, 10}, 0));
            g2d.drawRoundRect(10, 10, 80, 120, 10, 10);
        }

        g2d.dispose();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (canAcceptCard(dtde)) {
            isValidDropZone = true;
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            isValidDropZone = false;
            dtde.rejectDrag();
        }
        repaint();
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        
        if (canAcceptCard(dtde)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        isValidDropZone = false;
        repaint();
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (canAcceptDrop(dtde)) {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);

              
                String cardData = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                String[] parts = cardData.split(":");

              
                DraggableCard newCard = new DraggableCard(parts[0], parts[1]);
                addCard(newCard);

                dtde.dropComplete(true);
            } else {
                dtde.rejectDrop();
                dtde.dropComplete(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
            dtde.dropComplete(false);
        }

        isValidDropZone = false;
        repaint();
    }

    private boolean canAcceptCard(DropTargetDragEvent dtde) {
        try {
            String cardData = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
            String[] parts = cardData.split(":");
            String suit = parts[0];
            String rank = parts[1];

            return isValidMove(suit, rank);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canAcceptDrop(DropTargetDropEvent dtde) {
        try {
            String cardData = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
            String[] parts = cardData.split(":");
            String suit = parts[0];
            String rank = parts[1];

            return isValidMove(suit, rank);
        } catch (Exception e) {
            return false;
        }
    }

  
    boolean isValidMove(String suit, String rank) {
      
        if (pileType == PileType.FOUNDATION) {
            if (cards.isEmpty()) {
                return rank.equals("A"); 
            }

            DraggableCard topCard = getTopCard();
            if (!topCard.getSuit().equals(suit)) {
                return false; 
            }

            return isNextRankInSequence(topCard.getRank(), rank);
        }

        if (pileType == PileType.TABLEAU) {
            if (cards.isEmpty()) {
                return rank.equals("K"); 
            }

            DraggableCard topCard = getTopCard();
            if (isSameColor(topCard.getSuit(), suit)) {
                return false;             }

            return isPreviousRankInSequence(topCard.getRank(), rank);
        }

        if (pileType == PileType.WASTE) {
            return false; 
        }

        return true;     }

    private boolean isNextRankInSequence(String currentRank, String newRank) {
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        int currentIndex = getRankIndex(currentRank, ranks);
        int newIndex = getRankIndex(newRank, ranks);

        return newIndex == currentIndex + 1;
    }

    private boolean isPreviousRankInSequence(String currentRank, String newRank) {
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        int currentIndex = getRankIndex(currentRank, ranks);
        int newIndex = getRankIndex(newRank, ranks);

        return newIndex == currentIndex - 1;
    }

    private int getRankIndex(String rank, String[] ranks) {
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].equals(rank)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSameColor(String suit1, String suit2) {
        boolean suit1IsRed = suit1.equals("Hearts") || suit1.equals("Diamonds");
        boolean suit2IsRed = suit2.equals("Hearts") || suit2.equals("Diamonds");
        return suit1IsRed == suit2IsRed;
    }

    public void addCard(DraggableCard card) {
        cards.add(card);
        add(card);

        positionCards();

        card.setFaceUp(shouldCardBeFaceUp(card));

        revalidate();
        repaint();
    }

    private void positionCards() {
        for (int i = 0; i < cards.size(); i++) {
            DraggableCard card = cards.get(i);

            switch (pileType) {
                case FOUNDATION:
                case STOCK:
                case WASTE:
                 
                    card.setLocation(10, 10);
                    break;

                case TABLEAU:
               
                    int yOffset = 10 + (i * 25); 
                    card.setLocation(10, yOffset);

                    if (i < cards.size() - maxVisibleCards) {
                        card.setFaceUp(false);
                    }
                    break;
            }
        }
    }

    private boolean shouldCardBeFaceUp(DraggableCard card) {
        switch (pileType) {
            case FOUNDATION:
            case WASTE:
                return true;
            case STOCK:
                return false;
            case TABLEAU:
      
                return cards.indexOf(card) >= cards.size() - maxVisibleCards;
            default:
                return true;
        }
    }

    public ArrayList<DraggableCard> getMovableCards(DraggableCard clickedCard) {
        ArrayList<DraggableCard> movableCards = new ArrayList<>();

        if (pileType != PileType.TABLEAU) {
       
            if (cards.contains(clickedCard) && clickedCard == getTopCard()) {
                movableCards.add(clickedCard);
            }
            return movableCards;
        }

        int startIndex = cards.indexOf(clickedCard);
        if (startIndex == -1 || !clickedCard.isFaceUp()) {
            return movableCards; 
        }

        boolean validSequence = true;
        for (int i = startIndex; i < cards.size() - 1; i++) {
            DraggableCard current = cards.get(i);
            DraggableCard next = cards.get(i + 1);

            if (isSameColor(current.getSuit(), next.getSuit())
                    || !isPreviousRankInSequence(current.getRank(), next.getRank())) {
                validSequence = false;
                break;
            }
        }

        if (validSequence) {
            for (int i = startIndex; i < cards.size(); i++) {
                movableCards.add(cards.get(i));
            }
        }

        return movableCards;
    }

    public void removeCard(DraggableCard card) {
        cards.remove(card);
        remove(card);
        revalidate();
        repaint();
    }

    public DraggableCard getTopCard() {
        return cards.isEmpty() ? null : cards.get(cards.size() - 1);
    }

    public int getCardCount() {
        return cards.size();
    }

    public PileType getPileType() {
        return pileType;
    }

    public boolean canAutoMoveToFoundation() {
        if (cards.isEmpty()) {
            return false;
        }

        DraggableCard topCard = getTopCard();
        return topCard.isFaceUp() && (pileType == PileType.TABLEAU || pileType == PileType.WASTE);
    }

    public boolean isComplete() {
        if (pileType == PileType.FOUNDATION) {
            return cards.size() == 13 && getTopCard().getRank().equals("K");
        }
        return false;
    }

    public void flipTopCard() {
        if (!cards.isEmpty() && pileType == PileType.TABLEAU) {
            DraggableCard topCard = getTopCard();
            if (!topCard.isFaceUp()) {
                topCard.setFaceUp(true);

            
                Timer flipTimer = new Timer(50, null);
                final double[] scaleX = {1.0};

                flipTimer.addActionListener(new ActionListener() {
                    int frame = 0;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame++;
                        if (frame <= 5) {
                            scaleX[0] = 1.0 - (frame * 0.2);
                        } else if (frame <= 10) {
                            scaleX[0] = (frame - 5) * 0.2;
                        } else {
                            scaleX[0] = 1.0;
                            flipTimer.stop();
                        }

                        topCard.repaint();
                    }
                });

                flipTimer.start();
            }
        }
    }
}


class SolitaireGameManager {

    private ArrayList<DropZonePile> foundations;
    private ArrayList<DropZonePile> tableau;
    private DropZonePile stock;
    private DropZonePile waste;
    private ArrayList<DraggableCard> deck;
    private int score = 0;
    private long startTime;
    private Timer gameTimer;

    public SolitaireGameManager() {
        initializeGame();
    }

    private void initializeGame() {
     
        foundations = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            foundations.add(new DropZonePile(DropZonePile.PileType.FOUNDATION));
        }

        tableau = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            tableau.add(new DropZonePile(DropZonePile.PileType.TABLEAU));
        }

        stock = new DropZonePile(DropZonePile.PileType.STOCK);
        waste = new DropZonePile(DropZonePile.PileType.WASTE);

        createDeck();
        shuffleDeck();
        dealCards();

        startTime = System.currentTimeMillis();
        startGameTimer();
    }

    private void createDeck() {
        deck = new ArrayList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new DraggableCard(suit, rank));
            }
        }
    }

    private void shuffleDeck() {
        for (int i = deck.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            DraggableCard temp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, temp);
        }
    }

    private void dealCards() {
        int cardIndex = 0;

        for (int pile = 0; pile < 7; pile++) {
            for (int card = 0; card <= pile; card++) {
                if (cardIndex < deck.size()) {
                    DraggableCard cardToDeal = deck.get(cardIndex++);
                    tableau.get(pile).addCard(cardToDeal);

                   
                    cardToDeal.setFaceUp(card == pile);
                }
            }
        }

       
        while (cardIndex < deck.size()) {
            stock.addCard(deck.get(cardIndex++));
        }
    }

    private void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            
        });
        gameTimer.start();
    }

    public boolean isGameWon() {
        for (DropZonePile foundation : foundations) {
            if (!foundation.isComplete()) {
                return false;
            }
        }
        return true;
    }

    // Auto-complete when possible
    public void autoComplete() {
        boolean foundMove = true;
        while (foundMove && !isGameWon()) {
            foundMove = false;

          
            for (DropZonePile tablePile : tableau) {
                if (tablePile.canAutoMoveToFoundation()) {
             
                    DraggableCard card = tablePile.getTopCard();
                    for (DropZonePile foundation : foundations) {
                        if (foundation.isValidMove(card.getSuit(), card.getRank())) {
                            tablePile.removeCard(card);
                            foundation.addCard(card);
                            tablePile.flipTopCard();
                            foundMove = true;
                            break;
                        }
                    }
                    if (foundMove) {
                        break;
                    }
                }
            }

            if (!foundMove && waste.canAutoMoveToFoundation()) {
                DraggableCard card = waste.getTopCard();
                for (DropZonePile foundation : foundations) {
                    if (foundation.isValidMove(card.getSuit(), card.getRank())) {
                        waste.removeCard(card);
                        foundation.addCard(card);
                        foundMove = true;
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<DropZonePile> getFoundations() {
        return foundations;
    }

    public ArrayList<DropZonePile> getTableau() {
        return tableau;
    }

    public DropZonePile getStock() {
        return stock;
    }

    public DropZonePile getWaste() {
        return waste;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }
}
