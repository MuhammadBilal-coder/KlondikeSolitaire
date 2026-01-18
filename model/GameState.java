package solitaire.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameState {

    private List<Card> stockCards;
    private List<Card> wasteCards;
    private List<Card>[] foundationCards;
    private List<Card>[] tableauCards;
    private String moveDescription;
    
    // Add these fields to save score and moves
    private int savedScore;
    private int savedMoves;

    @SuppressWarnings("unchecked")
    public GameState(SolitaireGame game, String description) {
        this.moveDescription = description;
        
        // Save score and moves BEFORE any changes
        this.savedScore = game.getScore();
        this.savedMoves = game.getMoves();

        stockCards = new ArrayList<>();
        Stack<Card> stock = game.getStockPile().getCards();
        for (Card card : stock) {
            stockCards.add(copyCard(card));
        }

        wasteCards = new ArrayList<>();
        Stack<Card> waste = game.getWastePile().getCards();
        for (Card card : waste) {
            wasteCards.add(copyCard(card));
        }

        foundationCards = new List[4];
        for (int i = 0; i < 4; i++) {
            foundationCards[i] = new ArrayList<>();
            Stack<Card> foundation = game.getFoundationPile(i).getCards();
            for (Card card : foundation) {
                foundationCards[i].add(copyCard(card));
            }
        }

        tableauCards = new List[7];
        for (int i = 0; i < 7; i++) {
            tableauCards[i] = new ArrayList<>();
            Stack<Card> tableau = game.getTableauPileObject(i).getCards();
            for (Card card : tableau) {
                tableauCards[i].add(copyCard(card));
            }
        }
    }

    private Card copyCard(Card original) {
        Card copy = new Card(original.getSuit(), original.getRank());
        copy.setFaceUp(original.isFaceUp());
        return copy;
    }

    public void restoreGameState(SolitaireGame game) {
        // Clear all piles
        game.getStockPile().getCards().clear();
        game.getWastePile().getCards().clear();
        for (int i = 0; i < 4; i++) {
            game.getFoundationPile(i).getCards().clear();
        }
        for (int i = 0; i < 7; i++) {
            game.getTableauPileObject(i).getCards().clear();
        }

        // Restore all card positions
        for (Card card : stockCards) {
            game.getStockPile().addCard(copyCard(card));
        }

        for (Card card : wasteCards) {
            game.getWastePile().addCard(copyCard(card));
        }

        for (int i = 0; i < 4; i++) {
            for (Card card : foundationCards[i]) {
                game.getFoundationPile(i).addCard(copyCard(card));
            }
        }

        for (int i = 0; i < 7; i++) {
            for (Card card : tableauCards[i]) {
                game.getTableauPileObject(i).addCard(copyCard(card));
            }
        }

        // Restore score and moves - this is the key fix!
        game.setScore(savedScore);
        game.setMoves(savedMoves);

        System.out.println("Game state restored: " + moveDescription + " (Score: " + savedScore + ", Moves: " + savedMoves + ")");
    }

    public List<Card> getStockCards() {
        return stockCards;
    }

    public List<Card> getWasteCards() {
        return wasteCards;
    }

    public List<Card>[] getFoundationCards() {
        return foundationCards;
    }

    public List<Card>[] getTableauCards() {
        return tableauCards;
    }

    public String getMoveDescription() {
        return moveDescription;
    }
    
    public int getSavedScore() {
        return savedScore;
    }
    
    public int getSavedMoves() {
        return savedMoves;
    }
}