package solitaire.model;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public abstract class Pile {
    protected Stack<Card> cards;

    public Pile() {
        cards = new Stack<>();
    }

    public void addCard(Card card) {
        cards.push(card);
    }

    public Card topCard() {
        return cards.isEmpty() ? null : cards.peek();
    }

    public Card removeCard() {
        return cards.isEmpty() ? null : cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public List<Card> asList() {
        return new ArrayList<>(cards);
    }

    public abstract boolean canAdd(Card card);  
}
