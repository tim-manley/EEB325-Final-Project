public class Trick {
    private final Card[] cards;
    private final int leaderIndex; // absolute index of leader
    private int pointsInTrick;
    private int takerIndex;
    private Card takingCard;

    public Trick(int l) {
        cards = new Card[4];
        leaderIndex = l;
        pointsInTrick = 0;
        takerIndex = l;
    }

    public void playCard(Card c, int relativePlayerIndex) {
        // adds card to table
        cards[relativePlayerIndex] = c;

        // determines the current taking card and associated player
        if (relativePlayerIndex == 0) takingCard = c;
        else if (c.compareTo(takingCard) > 0) {
            takingCard = c;
            takerIndex = (leaderIndex + relativePlayerIndex) % 4;
        }

        // adds points to tally of points in trick
        pointsInTrick += c.getPointValue();
    }

    public Card[] getPlayedCards() {
        return cards;
    }

    public int getLeaderIndex() {
        return leaderIndex;
    }

    public boolean hasPlayerGone(int absolutePlayerIndex){
        return cards[Math.floorMod(absolutePlayerIndex-leaderIndex, 4)] != null;
    }

    public int getPointsInTrick() {
        return pointsInTrick;
    }

    // returns absolute index of player [0-3] corresponding to their
    // fixed position in the table, not relative to the trick
    public int getTakerIndex() {
        return takerIndex;
    }

    public Card getTakingCard() {
        return takingCard;
    }

}
