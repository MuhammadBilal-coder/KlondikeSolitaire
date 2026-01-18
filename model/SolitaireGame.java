package solitaire.model;

import java.util.Stack;
import solitaire.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SolitaireGame {

    private final Deck deck;
    private final StockPile stock;
    private final WastePile waste;
    private final FoundationPile[] foundations;
    private final TableauPile[] tableaus;
    private int totalCards = 0; 

    private int score = 0;
    private int moves = 0;
    private long startTime;
    private long elapsedTime = 0;
    private boolean gameStarted = false;

    private Stack<GameState> undoStack;
    private static final int MAX_UNDO_STATES = 50;

    public SolitaireGame() {
        deck = new Deck();
        stock = new StockPile();
        waste = new WastePile();
        foundations = new FoundationPile[4];
        tableaus = new TableauPile[7];
        undoStack = new Stack<>();
        
        startTime = System.currentTimeMillis();
        score = 0;
        moves = 0;

        foundations[0] = new FoundationPile(Card.Suit.HEARTS);
        foundations[1] = new FoundationPile(Card.Suit.SPADES);
        foundations[2] = new FoundationPile(Card.Suit.DIAMONDS);
        foundations[3] = new FoundationPile(Card.Suit.CLUBS);

        for (int i = 0; i < 7; i++) {
            tableaus[i] = new TableauPile();
        }

        setupGame();

        System.out.println("Game initialized with " + getTotalCardsCount() + " cards");
        printGameState();
    }

    private void setupGame() {
       
        for (int col = 0; col < 7; col++) {
            for (int row = 0; row <= col; row++) {
                Card card = deck.drawCard();
                if (card != null) {
                   
                    if (row == col) {
                        card.setFaceUp(true);
                    } else {
                        card.setFaceUp(false);
                    }
                    tableaus[col].addCard(card);
                    totalCards++;
                }
            }
        }

        while (!deck.isEmpty()) {
            Card card = deck.drawCard();
            if (card != null) {
                card.setFaceUp(false); 
                stock.addCard(card);
                totalCards++;
            }
        }
    }

    
    private void updateScore(String moveType) {
        switch (moveType) {
            case "WASTE_TO_FOUNDATION":
                score += 10;
                break;
            case "TABLEAU_TO_FOUNDATION":
                score += 10;
                break;
            case "FOUNDATION_TO_TABLEAU":
                score -= 15; 
                break;
            case "TURN_OVER_TABLEAU_CARD":
                score += 5;
                break;
            case "RECYCLE_WASTE":
                score -= 100; 
                break;
            case "UNDO":
                score -= 50;
                break;
            default:
               
                break;
        }
        
        if (score < 0) {
            score = 0;
        }
        
        moves++;
        
        updateElapsedTime();
    }

    // NEW METHOD: Update score without incrementing moves
    private void updateScoreOnly(String moveType) {
        switch (moveType) {
            case "WASTE_TO_FOUNDATION":
                score += 10;
                break;
            case "TABLEAU_TO_FOUNDATION":
                score += 10;
                break;
            case "FOUNDATION_TO_TABLEAU":
                score -= 15; 
                break;
            case "TURN_OVER_TABLEAU_CARD":
                score += 5;
                break;
            case "RECYCLE_WASTE":
                score -= 100; 
                break;
            case "UNDO":
                score -= 50;
                break;
            default:
                break;
        }
        
        if (score < 0) {
            score = 0;
        }
    }

    
    private void calculateWinBonus() {
        if (isGameWon()) {
  
            long gameTimeInSeconds = elapsedTime / 1000;
            int timeBonus = Math.max(0, 10000 - (int)(gameTimeInSeconds * 2));
            
            int moveBonus = Math.max(0, 1000 - (moves * 5));
            
            score += timeBonus + moveBonus;
            
            System.out.println("Game completed!");
            System.out.println("Time bonus: " + timeBonus);
            System.out.println("Move bonus: " + moveBonus);
            System.out.println("Final score: " + score);
        }
    }

   
    private void updateElapsedTime() {
        if (gameStarted) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }

   
    public void startGame() {
        if (!gameStarted) {
            startTime = System.currentTimeMillis();
            gameStarted = true;
        }
    }

  
    public String getFormattedTime() {
        updateElapsedTime();
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%d:%02d", minutes, seconds % 60);
        }
    }

    public int getScore() {
        return score;
    }
    
    public int getMoves() {
        return moves;
    }
    
    public long getElapsedTime() {
        updateElapsedTime();
        return elapsedTime;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    private void saveGameState(String description) {
        
        if (undoStack.size() >= MAX_UNDO_STATES) {
            
            Stack<GameState> tempStack = new Stack<>();
            for (int i = 0; i < MAX_UNDO_STATES - 1; i++) {
                tempStack.push(undoStack.pop());
            }
            undoStack.clear();
            while (!tempStack.isEmpty()) {
                undoStack.add(0, tempStack.pop());
            }
        }

        GameState state = new GameState(this, description);
        undoStack.push(state);
        System.out.println("Saved game state: " + description + " (Stack size: " + undoStack.size() + ")");
    }

    public boolean canUndo() {
        return undoStack.size() > 1; 
    }

    public boolean undo() {
        if (!canUndo()) {
            System.out.println("Cannot undo - no previous states available");
            return false;
        }

        // Remove current state
        GameState currentState = undoStack.pop();
        System.out.println("Undoing: " + currentState.getMoveDescription());

        // Restore to previous state (this will restore score and moves automatically)
        GameState previousState = undoStack.peek();
        previousState.restoreGameState(this);

        // Apply undo penalty ONLY to score, not moves
        updateScoreOnly("UNDO");

        System.out.println("Restored to: " + previousState.getMoveDescription());
        return true;
    }

    public boolean drawFromStock() {
        startGame(); 
        
        if (stock.isEmpty()) {
           
            if (!waste.isEmpty()) {
                saveGameState("Recycle Waste to Stock");
                updateScore("RECYCLE_WASTE");
                
                while (!waste.isEmpty()) {
                    Card card = waste.removeCard();
                    if (card != null) {
                        card.setFaceUp(false);
                        stock.addCard(card);
                    }
                }
                return true;
            }
            return false;
        } else {
          
            saveGameState("Draw from Stock");
            Card card = stock.removeCard();
            if (card != null) {
                card.setFaceUp(true);
                waste.addCard(card);
                moves++; 
                return true;
            }
        }

        return false;
    }

    public boolean canMoveToFoundation(Card card, int foundationIndex) {
        if (foundationIndex < 0 || foundationIndex >= 4 || card == null) {
            return false;
        }
        return foundations[foundationIndex].canAdd(card);
    }

    public boolean canMoveToTableau(Card card, int tableauIndex) {
        if (tableauIndex < 0 || tableauIndex >= 7 || card == null) {
            return false;
        }
        return tableaus[tableauIndex].canAdd(card);
    }

    public boolean moveToFoundation(Card card, int foundationIndex) {
        if (foundationIndex < 0 || foundationIndex >= 4 || card == null) {
            System.out.println("Invalid foundation index or null card");
            return false;
        }

        if (foundations[foundationIndex].canAdd(card)) {
            foundations[foundationIndex].addCard(card);
            System.out.println("Successfully moved " + card.getRank() + " of " + card.getSuit() + " to foundation " + foundationIndex);
            return true;
        } else {
            System.out.println("Cannot add " + card.getRank() + " of " + card.getSuit() + " to foundation " + foundationIndex);
            return false;
        }
    }

    public boolean moveToTableau(Card card, int tableauIndex) {
        if (tableauIndex < 0 || tableauIndex >= 7 || card == null) {
            System.out.println("Invalid tableau index or null card");
            return false;
        }

        if (tableaus[tableauIndex].canAdd(card)) {
            tableaus[tableauIndex].addCard(card);
            System.out.println("Successfully moved " + card.getRank() + " of " + card.getSuit() + " to tableau " + tableauIndex);
            return true;
        } else {
            System.out.println("Cannot add " + card.getRank() + " of " + card.getSuit() + " to tableau " + tableauIndex);
            return false;
        }
    }


    public boolean canMoveSequenceFromTableau(int tableauIndex, int startIndex) {
        if (tableauIndex < 0 || tableauIndex >= 7) {
            return false;
        }

        List<Card> pile = getTableauPileAsList(tableauIndex);
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

    // Move sequence of cards from one tableau to another
    public boolean moveSequenceFromTableau(int sourceTableau, int startIndex, int targetTableau) {
        if (sourceTableau < 0 || sourceTableau >= 7 || targetTableau < 0 || targetTableau >= 7) {
            return false;
        }

        if (sourceTableau == targetTableau) {
            return false;
        }

        List<Card> sourcePile = getTableauPileAsList(sourceTableau);
        if (startIndex < 0 || startIndex >= sourcePile.size()) {
            return false;
        }

        if (!canMoveSequenceFromTableau(sourceTableau, startIndex)) {
            return false;
        }

        Card bottomCard = sourcePile.get(startIndex);
        if (!canMoveToTableau(bottomCard, targetTableau)) {
            return false;
        }

        List<Card> cardsToMove = new ArrayList<>();

        for (int i = sourcePile.size() - 1; i >= startIndex; i--) {
            Card card = removeFromTableau(sourceTableau);
            if (card != null) {
                cardsToMove.add(0, card); 
            }
        }

        for (Card card : cardsToMove) {
            addToTableau(card, targetTableau);
        }

        System.out.println("Moved sequence of " + cardsToMove.size() + " cards from tableau " + sourceTableau + " to tableau " + targetTableau);
        return true;
    }

    private boolean isRed(Card card) {
        return card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS;
    }

    public boolean isGameWon() {
        boolean won = true;
        for (FoundationPile foundation : foundations) {
            if (foundation.size() != 13) { 
                won = false;
                break;
            }
        }
        
        if (won) {
            calculateWinBonus();
        }
        
        return won;
    }

 
    public Card removeFromWaste() {
        return waste.removeCard();
    }

    public Card removeFromTableau(int index) {
        if (index >= 0 && index < 7) {
            Card removed = tableaus[index].removeCard();
  
            if (removed != null && !tableaus[index].isEmpty()) {
                Card newTop = tableaus[index].topCard();
                if (newTop != null && !newTop.isFaceUp()) {
                    newTop.setFaceUp(true);
                    updateScore("TURN_OVER_TABLEAU_CARD");
                    System.out.println("Flipped card: " + newTop.getRank() + " of " + newTop.getSuit());
                }
            }
            return removed;
        }
        return null;
    }

    public Card removeFromFoundation(int index) {
        if (index >= 0 && index < 4) {
            return foundations[index].removeCard();
        }
        return null;
    }

    public void addToFoundation(Card card, int index) {
        if (index >= 0 && index < 4 && card != null) {
            foundations[index].addCard(card);
        }
    }

    public void addToTableau(Card card, int index) {
        if (index >= 0 && index < 7 && card != null) {
            tableaus[index].addCard(card);
        }
    }

    public void addToWaste(Card card) {
        if (card != null) {
            waste.addCard(card);
        }
    }

    public void addToStock(Card card) {
        if (card != null) {
            stock.addCard(card);
        }
    }

    // FIXED: This method was causing the double undo issue
    public void saveStateBeforeMove(String moveDescription) {
        // Save state BEFORE making the move
        saveGameState(moveDescription);
        
        // Apply score and move updates AFTER saving the state
        if (moveDescription.contains("Foundation")) {
            if (moveDescription.contains("Waste")) {
                updateScore("WASTE_TO_FOUNDATION");
            } else if (moveDescription.contains("Tableau")) {
                updateScore("TABLEAU_TO_FOUNDATION");
            }
        } else if (moveDescription.contains("Tableau") && moveDescription.contains("Foundation")) {
            updateScore("FOUNDATION_TO_TABLEAU");
        } else {
            // For other moves, just increment moves counter
            moves++;
        }
    }

    public List<Card> getTableauPile(int index) {
        if (index >= 0 && index < 7) {
            return tableaus[index].asList();
        }
        return new ArrayList<>();
    }

    public List<Card> getTableauPileAsList(int index) {
        if (index >= 0 && index < 7) {
            return tableaus[index].asList();
        }
        return new ArrayList<>();
    }

    public Card getTopStockCard() {
        return stock.topCard();
    }

    public Card getTopWasteCard() {
        return waste.topCard();
    }

    public Card getTopFoundationCard(int index) {
        if (index >= 0 && index < 4) {
            return foundations[index].topCard();
        }
        return null;
    }

    public boolean isStockEmpty() {
        return stock.isEmpty();
    }

    public boolean isWasteEmpty() {
        return waste.isEmpty();
    }

    public int getStockSize() {
        return stock.size();
    }

    public int getWasteSize() {
        return waste.size();
    }

    public int getFoundationSize(int index) {
        if (index >= 0 && index < 4) {
            return foundations[index].size();
        }
        return 0;
    }

    public int getTableauSize(int index) {
        if (index >= 0 && index < 7) {
            return tableaus[index].size();
        }
        return 0;
    }

    public Card.Suit getFoundationSuit(int index) {
        if (index >= 0 && index < 4) {
            return foundations[index].getSuit();
        }
        return null;
    }

    public StockPile getStockPile() {
        return stock;
    }

    public WastePile getWastePile() {
        return waste;
    }

    public FoundationPile getFoundationPile(int index) {
        if (index >= 0 && index < 4) {
            return foundations[index];
        }
        return null;
    }

    public TableauPile getTableauPileObject(int index) {
        if (index >= 0 && index < 7) {
            return tableaus[index];
        }
        return null;
    }

    public int getTotalCardsCount() {
        int total = 0;
        total += stock.size();
        total += waste.size();
        for (int i = 0; i < 4; i++) {
            total += foundations[i].size();
        }
        for (int i = 0; i < 7; i++) {
            total += tableaus[i].size();
        }
        return total;
    }

    public void printGameState() {
        System.out.println("=== GAME STATE ===");
        System.out.println("Score: " + score + " | Moves: " + moves + " | Time: " + getFormattedTime());
        System.out.println("Stock: " + stock.size() + " cards");
        System.out.println("Waste: " + waste.size() + " cards");
        System.out.println("Foundations: "
                + foundations[0].size() + ", " + foundations[1].size() + ", "
                + foundations[2].size() + ", " + foundations[3].size());
        System.out.print("Tableau: ");
        for (int i = 0; i < 7; i++) {
            System.out.print(tableaus[i].size() + " ");
        }
        System.out.println();
        System.out.println("Total cards: " + getTotalCardsCount());
        System.out.println("Undo states: " + undoStack.size());
        System.out.println("==================");
    }
}