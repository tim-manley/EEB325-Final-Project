public class Card implements Comparable<Card> {
    private final Suit suit;
    private final int rank; // uses 1-based ordering, so the 2 of clubs has rank 2, queen 12, ace 14, etc.
    private final int pointValue;

    // constructor
    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        if (suit == Suit.HEARTS) pointValue = 1;                        // each heart is one point
        else if (suit == Suit.SPADES && rank == 12) pointValue = 13;    // queen of spades
        else pointValue = 0;
    }

    // getters
    public Suit getSuit() {
        return this.suit;
    }

    public int getRank() {
        return this.rank;
    }

    public int getPointValue() {
        return this.pointValue;
    }

    // no trumps, so this card is less than (-1) another card if it is out of that card's suit.
    // means we always compare the current card to the card that led the trick.
    public int compareTo(Card other) {
        if (this.suit == other.suit) return this.rank - other.rank;
        return -1;
    }

    // unit testing
    public static void main(String[] args) {
        Card jc = new Card(Suit.CLUBS, 11);
        Card qc = new Card(Suit.CLUBS, 12);
        Card qs = new Card(Suit.SPADES, 12);
        System.out.println(jc.getSuit());
        System.out.println(jc.getRank());
        System.out.println(jc.getPointValue());
        System.out.println(jc.compareTo(qc));
        System.out.println(qc.compareTo(jc));
        System.out.println(qs.compareTo(qc));
        System.out.println(qc.compareTo(qs));
    }
}
