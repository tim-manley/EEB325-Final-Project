import java.util.*;

public class Hand {
    private Player player;
    private int myIndex;
    private boolean[][] myHand; // true if I have the card (suit, index), false otherwise
    private List<Card> myCards; // to iterate more effectively over in certain instances
    private boolean isFirstLeader;

    // constructor
    public Hand(Player p, int i) {
        myIndex = i;
        player = p;
        isFirstLeader = false;
        myHand = new boolean[4][14]; // one extra entry per row, for 1-based indexing :)
        myCards = new ArrayList<>(); // MAKE SURE TO MODIFY ALONG WITH MYHAND WHEN REMOVING
    }

    public Card playCard(Round r, Card[] thisTrick) {
        // pick and return a card from the lead suit, informed by a strategy [TBD]
        Strategy s = pickStrategy(r, thisTrick);
        return pickCard(r, s, thisTrick);
    }

    // (do we want to take this path? strategy could just be an enum)
    // strategy is computed using the round info
    // such that for a given hand and a given strategy,
    // there is a predetermined (or probabilistic, if desired) card that is played
    // The idea is that in abstracting the information from the round state to
    // a strategy, we can evolve over strategy parameters rather than game cases
    private Strategy pickStrategy(Round r, Card[] thisTrick) {
        double perceivedRiskOfShooting = perceivedRiskOfShooting(r, thisTrick);
        double perceivedCooperationCost = perceivedCooperationCost(r, thisTrick);
        return Strategy.DEFECT; // filler
    }

    // returns a number corresponding to a player's perception of risk
    // where each trait (gene) corresponds to the weight that they place on that game element
    private double perceivedRiskOfShooting(Round r, Card[] thisTrick) {
        // *************************************************************
        //    FACTOR EXPLANATION
        //
        //    For each factor, the metric in question has been scaled to
        //    some double value between (0, 1), where 0 represents that
        //    factor as being a non-risk and 1 represents that factor as
        //    high risk in this scenario. This number is scaled by
        //    a player's trait corresponding to that factor, which tells
        //    you how much they personally consider that factor to be a
        //    risk factor in someone shooting the moon.
        //
        //    The result will be a normalized sum.
        // *************************************************************
        double[] traits = player.getTraits();
        int[] pointsTaken = r.getPointsTaken();
        int totalPointsTaken = r.getTotalPointsTaken();
        boolean heartsBroken = r.areHeartsBroken();
        boolean[][] playedCards = r.getPlayedCards();

        // *************************************************************
        //    FACTOR -1: Is it possible for someone to shoot the moon?
        //
        //    Explanation: If two different players have taken cards,
        //    then nobody can shoot the moon. This does not vary by
        //    player's perception, unless we were to include a memory
        //    decay element (which we are not, at the moment).
        //
        //    Traits used: None
        // *************************************************************

        int numPlayersWithPoints = 0;
        for (int i = 0; i < 4; i++) if (pointsTaken[i] > 0) numPlayersWithPoints++;
        // pointsTaken[myIndex] > 0 --> If I've taken points, nobody else can shoot
        if (numPlayersWithPoints > 1 || pointsTaken[myIndex] > 0) return 0.0;

        // *************************************************************
        //    FACTOR 0: How many points of the 26 has the threat taken?
        //
        //    Explanation: Normalized score as a percentage
        //
        //    Traits used: traits[0]
        // *************************************************************

        double pointsTakenScore = (totalPointsTaken / 26.0) * traits[0];

        // **************************************************************
        //    FACTOR 1: How many hearts higher than my highest heart
        //              are left in the deck?
        //
        //    Explanation: Normalized by dividing by 13, maximum possible
        //
        //    Traits used: traits[1]
        // **************************************************************

        double higherHeartsThanMine = 0.0;
        int myHighestHeart = highestRankInSuit(Suit.HEARTS);
        for (int i = myHighestHeart + 1; i < 14; i++) {
            if (!playedCards[Suit.HEARTS.getIndex()][i]) higherHeartsThanMine++;
        }
        double higherHeartsScore = (higherHeartsThanMine / 13.0) * traits[1];

        // **************************************************************
        //    FACTOR 2: Is the Queen of Spades still yet to be played?
        //
        //    Explanation: 1 if yes, 0 if no (indicator function, scaled)
        //
        //    Traits used: traits[2]
        // **************************************************************

        double queenOfSpadesScore = 0.0;
        if (!playedCards[Suit.SPADES.getIndex()][12]) queenOfSpadesScore = traits[2]; // (times 1.0)

        // **************************************************************
        //    Returns total risk score, which is the average of
        //    all other scaled scores
        // **************************************************************

        return (pointsTakenScore + higherHeartsScore + queenOfSpadesScore) / 3.0;
    }

    private double perceivedCooperationCost(Round r, Card[] thisTrick) {

        double[] traits = player.getTraits();
        int[] pointsTaken = r.getPointsTaken();
        int totalPointsTaken = r.getTotalPointsTaken();
        boolean heartsBroken = r.areHeartsBroken();
        boolean[][] playedCards = r.getPlayedCards();

        // *************************************************************
        //    FACTOR 0: How many points are left to be taken?
        //
        //    Explanation: Normalized score as a percentage
        //    Note: This is essentially an inverted FACTOR 0 from the
        //          perceived risk of shooting hand.
        //
        //    Traits used: traits[6]
        // *************************************************************

        double pointsTakenScore = ((26.0 - totalPointsTaken) / 26.0) * traits[6];

        // *************************************************************
        //    FACTOR 1: Do I have many high cards? (J, Q, K, A)
        //
        //    Explanation: If I have high cards and I choose to cooperate,
        //                 maybe I get stuck in control and take the rest
        //                 of the cards as well.
        //    Note: This is only a practical worry if there are many points
        //          left to be taken, but for now I'm considering each
        //          aspect of the game state in total isolation.
        //
        //    Traits used: traits[7]
        // *************************************************************

        int myHighCards = 0;
        for (Card myCard : myCards) {
            if (myCard.getRank() > 10) myHighCards++;
        }
        double myHighCardsScore = (myHighCards / 12.0) * traits[7];


        // *************************************************************
        //    FACTOR 2: Do I have the Queen of Spades?
        //
        //    Explanation: If I have high cards and I choose to cooperate,
        //                 maybe I get stuck in control and take the Queen!
        //
        //    Note: This is only a practical worry if there are many points
        //          left to be taken, but for now I'm considering each
        //          aspect of the game state in total isolation.
        //
        //    Traits used: traits[8]
        // *************************************************************

        double myQueenOfSpadesRisk = 0.0;
        if (myHand[Suit.SPADES.getIndex()][12]) myQueenOfSpadesRisk = traits[8];

        // **************************************************************
        //    Returns total cooperation risk score, which is the average
        //    of all other scaled scores
        // **************************************************************

        // notably should include the cost of cards that would be taken in this trick (not implemented yet)
        return (pointsTakenScore + myHighCardsScore + myQueenOfSpadesRisk) / 3.0;
    }

    // picks a card from the player's hand to play
    // given the strategy they have determined and the other cards in the trick
    // (critically, at this point we are able to ignore the other information
    //  present in the round, since that is what informs the strategy)
    private Card pickCard(Round r, Strategy s, Card[] thisTrick) {
        // start with a binary choice: cooperate, or defect
        // could expand into thresholds, there are different extents to cooperate
        // on a scale from not cooperating at all (minimizing personal points taken)
        // to fully cooperating (taking a point card at all cost, saving all high cards to do so, etc)
        // Perhaps the pickStrategy outputs a cooperation score from 0 to 1 with thresholds for different
        // extents of corresponding cooperative behavior

        // What does a cooperating player do?
        // - takes trick with some amount of hearts in it, if the threat will take instead
        //   (threshold dependent, eventually -- not worth it to take the queen if you are able to take a single heart later)
        // - saves highest heart, possibly more hearts

        // BAD BAD BAD example code, not what we would really use
        // picks a random valid card to lead (doesn't pick 2 of clubs in first round, for instance)
        if (thisTrick[0] == null) {
            for (Card c: myCards) {
                if ((c.getSuit() == Suit.HEARTS) && !r.areHeartsBroken()) {
                    return c;
                }
            }
        }
        assert thisTrick[0] != null; // duh but ok
        Suit leadSuit = thisTrick[0].getSuit();
        for (Card c: myCards) {
            if (c.getSuit() == leadSuit) {
                return c;
            }
        }
        // returns a random card if doesn't have something in suit, for now.
        return myCards.get(0);
    }

//    public Card highest_non_taking(leadingSuit, myCards) {
//            for card in suit[-1::]:
//                if card is higher than highest played so far:
//                    continue;
//                else:
//            return card;
//
//
//        }

    // returns the rank of the highest card in my hand, in this suit
    private int highestRankInSuit(Suit s) {
        int suitIndex = s.getIndex();
        for (int i = 13; i > 0; i--) {
            if (myHand[suitIndex][i]) return i;
        }
        return -1;
    }

    public void giveCard(Card c) {
        myHand[c.getSuit().getIndex()][c.getRank()] = true;
        myCards.add(c);
    }

    public boolean isFirstLeader() {
        return isFirstLeader;
    }

}
