package solitaire.model;

public class Card {

    public enum Suit {
        HEARTS, SPADES, DIAMONDS, CLUBS
    }

    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN,
        EIGHT, NINE, TEN, JACK, QUEEN, KING
    }

    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    public boolean isBlack() {
        return suit == Suit.SPADES || suit == Suit.CLUBS;
    }

    public int getRankValue() {
        return rank.ordinal() + 1;
    }

    public boolean canStackOn(Card other) {
        return this.isRed() != other.isRed()
                && this.getRankValue() == other.getRankValue() - 1;
    }

    public boolean canMoveToFoundation(Card foundationTop) {
        if (foundationTop == null) {
            return this.rank == Rank.ACE;
        }
        return this.suit == foundationTop.suit
                && this.getRankValue() == foundationTop.getRankValue() + 1;
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }

    public String getImageFileName() {
        return getRankShort() + getSuitShort() + ".png";
    }

    private String getSuitShort() {
        switch (suit) {
            case HEARTS:
                return "H";
            case SPADES:
                return "S";
            case DIAMONDS:
                return "D";
            case CLUBS:
                return "C";
        }
        return "";
    }

    private String getRankShort() {
        switch (rank) {
            case ACE:
                return "A";
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "10";
            case JACK:
                return "J";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
        }
        return "";
    }
}
