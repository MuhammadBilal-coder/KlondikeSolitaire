package solitaire.model;

public class TableauPile extends Pile {

    @Override
    public boolean canAdd(Card card) {
        if (card == null) return false;
       
        if (cards.isEmpty()) {
            boolean canAdd = card.getRank() == Card.Rank.KING;
            if (!canAdd) {
                System.out.println("Empty tableau requires KING, got " + card.getRank());
            }
            return canAdd;
        }

        Card top = topCard();
        if (top == null) return false;
        
        if (!card.isFaceUp()) {
            System.out.println("Cannot place face-down card on tableau");
            return false;
        }
        
        boolean differentColor = isRed(card) != isRed(top);
        if (!differentColor) {
            System.out.println("Cards must be different colors. " + 
                             card.getSuit() + " and " + top.getSuit() + " are same color");
            return false;
        }
        
        boolean oneLower = card.getRank().ordinal() == top.getRank().ordinal() - 1;
        if (!oneLower) {
            System.out.println("Invalid rank sequence: trying to place " + card.getRank() + 
                             " on " + top.getRank() + ". Should be " + 
                             Card.Rank.values()[top.getRank().ordinal() - 1]);
            return false;
        }

        return true;
    }

    private boolean isRed(Card card) {
        return card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS;
    }
}