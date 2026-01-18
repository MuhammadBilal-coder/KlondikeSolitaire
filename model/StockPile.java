package solitaire.model;

public class StockPile extends Pile {

    public StockPile() {
        super();
    }

    public Card draw() {
        return cards.isEmpty() ? null : cards.pop();
    }

    @Override
    public boolean canAdd(Card card) {
       
        return true;
    }
}