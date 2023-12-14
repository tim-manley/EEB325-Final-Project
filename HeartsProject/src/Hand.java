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

    public Card playCard(Round r, Trick thisTrick) {
        // pick and return a card from the lead suit, informed by a strategy [TBD]
        Strategy s = player.getPlayerStrategy(); // need to write, just returns AVOID_POINTS for now
        return pickCard(r, s, thisTrick);
    }

    // strategy is computed per species
    // such that for a given hand and a given strategy,
    // there is a predetermined (or probabilistic, if desired) card that is played
    private Strategy pickStrategy(Round r, Trick thisTrick) {
        double perceivedRiskOfShooting = perceivedRiskOfShooting(r, thisTrick);
        double perceivedCooperationCost = perceivedCooperationCost(r, thisTrick);
        return Strategy.AVOID_POINTS; // filler
    }

    // returns a number corresponding to a player's perception of risk, based on remaining points in hand
    private double perceivedRiskOfShooting(Round r, Trick thisTrick) {
        int[] pointsTaken = r.getPointsTaken();
        int totalPointsTaken = r.getTotalPointsTaken();
        int leader = r.getLeader();
        int potentialThreatIndex = -1;

        // hearts haven't been broken
        if (totalPointsTaken == 0) return 0.0;

        // hearts have been broken
        int numPlayersWithPoints = 0;
        for (int i = 0; i < 4; i++) {
            if (pointsTaken[i] > 0) {
                numPlayersWithPoints++;
                potentialThreatIndex = i;
            }
        }
        // pointsTaken[myIndex] > 0 --> If I've taken points, nobody else can shoot
        if (numPlayersWithPoints > 1 || pointsTaken[myIndex] > 0) return 0.0;

        // at this point, we've identified that exactly one player has taken hearts in the round.
        // let's see if someone else is already guaranteed to take hearts this round
        if (thisTrick.getTakerIndex() != potentialThreatIndex && thisTrick.hasPlayerGone(potentialThreatIndex)
            && thisTrick.getPointsInTrick() > 0) {
            return 0.0; // someone else will take points
        }

        return totalPointsTaken / 26.0;
    }

//    // NEEDS REFACTORING, IF WE WANT TO INCLUDE
//    private double perceivedCooperationCost(Round r, Trick thisTrick) {
//
//        int[] pointsTaken = r.getPointsTaken();
//        int totalPointsTaken = r.getTotalPointsTaken();
//        boolean heartsBroken = r.areHeartsBroken();
//        boolean[][] playedCards = r.getPlayedCards();
//
//        // *************************************************************
//        //    FACTOR 0: How many points are left to be taken?
//        //
//        //    Explanation: Normalized score as a percentage
//        //    Note: This is essentially an inverted FACTOR 0 from the
//        //          perceived risk of shooting hand.
//        //
//        // *************************************************************
//
//        double pointsTakenScore = ((26.0 - totalPointsTaken) / 26.0);
//
//        // *************************************************************
//        //    FACTOR 1: Do I have many high cards? (J, Q, K, A)
//        //
//        //    Explanation: If I have high cards and I choose to cooperate,
//        //                 maybe I get stuck in control and take the rest
//        //                 of the cards as well.
//        //    Note: This is only a practical worry if there are many points
//        //          left to be taken, but for now I'm considering each
//        //          aspect of the game state in total isolation.
//        //
//        // *************************************************************
//
//        int myHighCards = 0;
//        for (Card myCard : myCards) {
//            if (myCard.getRank() > 10) myHighCards++;
//        }
//        double myHighCardsScore = (myHighCards / 12.0);
//
//
//        // *************************************************************
//        //    FACTOR 2: Do I have the Queen of Spades?
//        //
//        //    Explanation: If I have high cards and I choose to cooperate,
//        //                 maybe I get stuck in control and take the Queen!
//        //
//        //    Note: This is only a practical worry if there are many points
//        //          left to be taken, but for now I'm considering each
//        //          aspect of the game state in total isolation.
//        //
//        // *************************************************************
//
//        double myQueenOfSpadesRisk = 0.0;
//        if (myHand[Suit.SPADES.getIndex()][12]) myQueenOfSpadesRisk = 1.0;
//
//        // **************************************************************
//        //    Returns total cooperation risk score, which is the average
//        //    of all other scaled scores
//        // **************************************************************
//
//        // notably should include the cost of cards that would be taken in this trick (not implemented yet)
//        return (pointsTakenScore + myHighCardsScore + myQueenOfSpadesRisk) / 3.0;
//    }

    // picks a card from the player's hand to play
    // given the strategy they have determined and the other cards in the trick
    private Card pickCard(Round r, Strategy s, Trick thisTrick) throws Exception {
        // start with a binary choice: cooperate, or defect
        // could expand into thresholds, there are different extents to cooperate
        // on a scale from not cooperating at all (minimizing personal points taken)
        // to fully cooperating (taking a point card at all cost, saving all high cards to do so, etc)
        // Perhaps the pickStrategy outputs a cooperation score from 0 to 1 with thresholds for different
        // extents of corresponding cooperative behavior, if we choose to implement a broader spectrum of behavior

        // What does a cooperating player do?
        // - takes trick with some amount of hearts in it, if the threat will take instead
        //   (threshold dependent, eventually -- not worth it to take the queen if you are able to take a single heart later)
        // - saves highest heart, possibly more hearts

        boolean heartsBroken = r.areHeartsBroken();

        // leads new trick
        if (thisTrick.getLeaderIndex() == myIndex) {
            if (r.getTricksLeft() == 13) {
                if (!myHand[0][2]) throw new Exception("Leader of first trick should have 2Clubs");
                else return new Card(Suit.CLUBS, 2);
            }
            if (s == Strategy.AVOID_POINTS) {
                Card c = myLowestCard(heartsBroken);
                if (c != null) return c;
                // c is only null if hearts haven't been broken and this player has to do so
                c = myLowestInSuit(3);
                if (c == null) throw new Exception("Player has no valid moves?");
                return c;
            }
//            if (s == Strategy.)
        }

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

    // returns lowest card in a suit designated by suitIndex
    private Card myLowestInSuit(int suitIndex) {
        for (int i = 1; i < 14; i++)
            if (myHand[suitIndex][i]) return new Card(suitIndex, i);
        return null;
    }

    // returns lowest card in hand that is playable
    private Card myLowestCard(boolean isHeartsBroken) {
        int maxSuit = 3;
        if (isHeartsBroken) maxSuit = 4;
        for (int i = 1; i < 14; i++) {
            for (int j = 0; j < maxSuit; j++)
                if (myHand[i][j]) return new Card(i, j);
        }
        return null;
    }

}
