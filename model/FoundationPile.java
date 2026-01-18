package solitaire.model;

public class FoundationPile extends Pile {
    private final Card.Suit suit;

    public FoundationPile(Card.Suit suit) {
        this.suit = suit;
    }

    @Override
    public boolean canAdd(Card card) {
        if (card == null) return false;

        if (card.getSuit() != suit) {
            System.out.println("Card suit " + card.getSuit() + " doesn't match foundation suit " + suit);
            return false;
        }

        Card top = topCard();

        if (top == null) {
            boolean canAdd = card.getRank() == Card.Rank.ACE;
            if (!canAdd) {
                System.out.println("Empty foundation requires ACE, got " + card.getRank());
            }
            return canAdd;
        }

        boolean canAdd = card.getRank().ordinal() == top.getRank().ordinal() + 1;
        if (!canAdd) {
            System.out.println("Invalid sequence: trying to place " + card.getRank() + 
                             " on " + top.getRank() + ". Expected: " + 
                             Card.Rank.values()[top.getRank().ordinal() + 1]);
        }
        return canAdd;
    }

    public Card.Suit getSuit() {
        return suit;
    }
}
