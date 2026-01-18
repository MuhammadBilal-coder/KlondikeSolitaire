package solitaire.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffleDeck();
   
        System.out.println("Deck created with " + cards.size() + " cards");
    }

    private void initializeDeck() {

        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    private void shuffleDeck() {
       
        for (int i = 0; i < 3; i++) {
            Collections.shuffle(cards);
        }
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(cards.size() - 1);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    // Reset deck with new shuffle
    public void reset() {
        cards.clear();
        initializeDeck();
        shuffleDeck();
    }

    public void printRemainingCards() {
        System.out.println("Remaining cards in deck: " + cards.size());
        for (Card card : cards) {
            System.out.println(card.getRank() + " of " + card.getSuit());
        }
    }
}