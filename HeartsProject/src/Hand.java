import sun.jvm.hotspot.gc.z.ZHeap;
import sun.jvm.hotspot.ui.tree.SimpleTreeGroupNode;

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
        myHand = new boolean[4][15]; // one extra entry per row, for 1-based indexing :)
        myCards = new ArrayList<>(); // MAKE SURE TO MODIFY ALONG WITH MYHAND WHEN REMOVING
    }

    public Card playCard(Round r, Trick thisTrick, int relativePlayerIndex) {
        // pick and return a card from the lead suit, informed by a strategy [TBD]
        double shootingRisk = perceivedRiskOfShooting(r, thisTrick);
        Strategy s = player.getPlayerStrategy(r, myIndex, shootingRisk); // need to write, just returns AVOID_POINTS for
                                                                         // now
        return pickCard(r, s, thisTrick, relativePlayerIndex);
    }

    // strategy is computed per species
    // such that for a given hand and a given strategy,
    // there is a predetermined (or probabilistic, if desired) card that is played
    private Strategy pickStrategy(Round r, Trick thisTrick) {
        double perceivedRiskOfShooting = perceivedRiskOfShooting(r, thisTrick);
        // double perceivedCooperationCost = perceivedCooperationCost(r, thisTrick);
        return Strategy.AVOID_POINTS; // filler
    }

    // returns a number corresponding to a player's perception of risk, based on
    // remaining points in hand
    private double perceivedRiskOfShooting(Round r, Trick thisTrick) {
        int[] pointsTaken = r.getPointsTaken();
        int totalPointsTaken = r.getTotalPointsTaken();
        int leader = r.getLeader();
        int potentialThreatIndex = -1;

        // hearts haven't been broken
        if (totalPointsTaken == 0)
            return 0.0;

        // hearts have been broken
        int numPlayersWithPoints = 0;
        for (int i = 0; i < 4; i++) {
            if (pointsTaken[i] > 0) {
                numPlayersWithPoints++;
                potentialThreatIndex = i;
            }
        }
        // pointsTaken[myIndex] > 0 --> If I've taken points, nobody else can shoot
        if (numPlayersWithPoints > 1 || pointsTaken[myIndex] > 0)
            return 0.0;

        // at this point, we've identified that exactly one player has taken hearts in
        // the round.
        // let's see if someone else is already guaranteed to take hearts this round
        if (thisTrick.getTakerIndex() != potentialThreatIndex && thisTrick.hasPlayerGone(potentialThreatIndex)
                && thisTrick.getPointsInTrick() > 0) {
            return 0.0; // someone else will take points
        }

        return totalPointsTaken / 26.0;
    }

    // // NEEDS REFACTORING, IF WE WANT TO INCLUDE
    // private double perceivedCooperationCost(Round r, Trick thisTrick) {
    //
    // int[] pointsTaken = r.getPointsTaken();
    // int totalPointsTaken = r.getTotalPointsTaken();
    // boolean heartsBroken = r.areHeartsBroken();
    // boolean[][] playedCards = r.getPlayedCards();
    //
    // // *************************************************************
    // // FACTOR 0: How many points are left to be taken?
    // //
    // // Explanation: Normalized score as a percentage
    // // Note: This is essentially an inverted FACTOR 0 from the
    // // perceived risk of shooting hand.
    // //
    // // *************************************************************
    //
    // double pointsTakenScore = ((26.0 - totalPointsTaken) / 26.0);
    //
    // // *************************************************************
    // // FACTOR 1: Do I have many high cards? (J, Q, K, A)
    // //
    // // Explanation: If I have high cards and I choose to cooperate,
    // // maybe I get stuck in control and take the rest
    // // of the cards as well.
    // // Note: This is only a practical worry if there are many points
    // // left to be taken, but for now I'm considering each
    // // aspect of the game state in total isolation.
    // //
    // // *************************************************************
    //
    // int myHighCards = 0;
    // for (Card myCard : myCards) {
    // if (myCard.getRank() > 10) myHighCards++;
    // }
    // double myHighCardsScore = (myHighCards / 12.0);
    //
    //
    // // *************************************************************
    // // FACTOR 2: Do I have the Queen of Spades?
    // //
    // // Explanation: If I have high cards and I choose to cooperate,
    // // maybe I get stuck in control and take the Queen!
    // //
    // // Note: This is only a practical worry if there are many points
    // // left to be taken, but for now I'm considering each
    // // aspect of the game state in total isolation.
    // //
    // // *************************************************************
    //
    // double myQueenOfSpadesRisk = 0.0;
    // if (myHand[Suit.SPADES.getIndex()][12]) myQueenOfSpadesRisk = 1.0;
    //
    // // **************************************************************
    // // Returns total cooperation risk score, which is the average
    // // of all other scaled scores
    // // **************************************************************
    //
    // // notably should include the cost of cards that would be taken in this trick
    // (not implemented yet)
    // return (pointsTakenScore + myHighCardsScore + myQueenOfSpadesRisk) / 3.0;
    // }

    // picks a card from the player's hand to play
    // given the strategy they have determined and the other cards in the trick
    private Card pickCard(Round r, Strategy s, Trick thisTrick, int relativePlayerIndex) {
        // start with a binary choice: cooperate, or defect
        // could expand into thresholds, there are different extents to cooperate
        // on a scale from not cooperating at all (minimizing personal points taken)
        // to fully cooperating (taking a point card at all cost, saving all high cards
        // to do so, etc)
        // Perhaps the pickStrategy outputs a cooperation score from 0 to 1 with
        // thresholds for different
        // extents of corresponding cooperative behavior, if we choose to implement a
        // broader spectrum of behavior

        // What does a cooperating player do?
        // - takes trick with some amount of hearts in it, if the threat will take
        // instead
        // (threshold dependent, eventually -- not worth it to take the queen if you are
        // able to take a single heart later)
        // - saves highest heart, possibly more hearts

        boolean heartsBroken = r.areHeartsBroken();

        // leads new trick
        if (relativePlayerIndex == 0) {
            if (r.getTricksLeft() == 13) {
                if (!myHand[0][2])
                    // throw new Exception("Leader of first trick should have 2Clubs");
                    return null;
                else
                    return new Card(Suit.CLUBS, 2);
            } else if (s == Strategy.AVOID_POINTS) {
                Card c = myLowestCard(heartsBroken);
                if (c != null)
                    return c;
                // CORNER CASE:
                // c is only null if hearts haven't been broken and this player is forced to do
                // so
                int lowestHeartRank = lowestRankInSuit(Suit.HEARTS);
                if (lowestHeartRank == -1)
                    // throw new Exception("Player has no valid moves?");
                    return null;
                return new Card(Suit.HEARTS, lowestHeartRank);
                // END CORNER CASE
            } else if (s == Strategy.COOPERATE) {
                if (heartsBroken) {
                    int highestHeartRank = highestRankInSuit(Suit.HEARTS);
                    if (highestHeartRank != -1) {
                        // plays a high heart to #stoptheshoot if certainly can
                        if (!areUnplayedCardsAbove(Suit.HEARTS, highestHeartRank, r)) {
                            return new Card(Suit.HEARTS, highestHeartRank);
                        }
                    }
                    // plays a low heart to bait out high hearts/allow someone else to take
                    int lowestHeartRank = lowestRankInSuit(Suit.HEARTS);
                    if (lowestHeartRank != -1)
                        return new Card(Suit.HEARTS, lowestHeartRank);
                }
                // if we've gotten to here, either hearts aren't broken or we have no hearts
                // plays lowest card because there usually isn't a clear direction as the leader
                // here
                Card c = myLowestCard(heartsBroken);
                if (c != null)
                    return c;
                // CORNER CASE:
                // c is only null if hearts haven't been broken and this player is forced to do
                // so
                int lowestHeartRank = lowestRankInSuit(Suit.HEARTS);
                if (lowestHeartRank == -1)
                    // throw new Exception("Player has no valid moves?");
                    return null;
                return new Card(Suit.HEARTS, lowestHeartRank);
                // END CORNER CASE
            } else if (s == Strategy.SHOOT) {
                Card c = guaranteedTake(r);
                if (c != null)
                    return c;
                // no guaranteed card to take, so play some high card
                c = myHighestCard(heartsBroken);
                if (c != null)
                    return c;
                // CORNER CASE:
                // c is only null if hearts haven't been broken and this player is forced to do
                // so
                int lowestHeartRank = lowestRankInSuit(Suit.HEARTS);
                if (lowestHeartRank == -1)
                    // throw new Exception("Player has no valid moves?");
                    return null;
                return new Card(Suit.HEARTS, lowestHeartRank);
                // END CORNER CASE
            } else {
                // throw new Exception("No valid strategy chosen");
                return null;
            }
        }

        // We are not leading the suit
        Suit leadingSuit = thisTrick.getPlayedCards()[0].getSuit();
        int highestRank = thisTrick.getTakingCard().getRank();

        if (s == Strategy.AVOID_POINTS) {
            int highestInSuit = highestRankInSuit(leadingSuit);
            int lowestInSuit = lowestRankInSuit(leadingSuit);
            int highestNonTaking = highestRankInSuitUnderRank(leadingSuit, highestRank);
            // are we out of the suit that was led?
            if (highestInSuit == -1) {
                return powerCard(r);
            }
            // second or third position
            if (relativePlayerIndex == 1 || relativePlayerIndex == 2) {
                if (highestNonTaking > -1)
                    return new Card(leadingSuit, highestNonTaking);
                return new Card(leadingSuit, lowestInSuit);
            }
            // fourth position
            else {
                if (thisTrick.getPointsInTrick() == 0)
                    return new Card(leadingSuit, highestInSuit);
                if (highestNonTaking > -1)
                    return new Card(leadingSuit, highestNonTaking);
                return new Card(leadingSuit, highestInSuit);
            }
        } else if (s == Strategy.SHOOT) {
            int highestInSuit = highestRankInSuit(leadingSuit);
            int lowestInSuit = lowestRankInSuit(leadingSuit);
            int highestNonTaking = highestRankInSuitUnderRank(leadingSuit, highestRank);
            // are we out of the suit that was led?
            if (highestInSuit == -1) {
                return myLowestCard(false); // plays lowest non-heart
            }
            // for 2nd, 3rd & 4th, we always play low if no hearts, high if hearts
            if (thisTrick.getPointsInTrick() == 0) {
                return new Card(leadingSuit, lowestInSuit);
            } else {
                Card winningCard = thisTrick.getTakingCard();
                int lowestTaking = lowestTaking(leadingSuit, winningCard);
                // If we're 4th then win with the lowest card possible
                if (relativePlayerIndex == 3 && lowestTaking != -1) {
                    return new Card(leadingSuit, lowestTaking);
                }
                // Otherwise just play the highest card we can to maximize chance
                return new Card(leadingSuit, highestInSuit);
            }

        } else if (s == Strategy.COOPERATE) {
            int highestInSuit = highestRankInSuit(leadingSuit);
            // Out of suit
            if (highestInSuit == -1) {
                // If a non-threat player before us is guaranteed to win based on trick and
                // cards left
                // then play a power card
                if (perceivedRiskOfShooting(r, thisTrick) == 0) {
                    return powerCard(r);
                } else { // Else play lowest non-heart/QS to save high cards
                    return myLowestCard(false);
                }
            }
            int lowestInSuit = lowestRankInSuit(leadingSuit);
            // No hearts/QS played
            if (thisTrick.getPointsInTrick() == 0) {
                return new Card(leadingSuit, lowestInSuit);
            }
            // If a heart/qs has been played, then try to win or if can't get rid of low
            // cards
            else {
                // Check if our highest card in suit is higher than current winning card
                Card winningCard = thisTrick.getTakingCard();
                Card ourHighestCard = new Card(leadingSuit, highestRankInSuit(leadingSuit));
                if (ourHighestCard.compareTo(winningCard) > 0) {
                    return ourHighestCard;
                } else {
                    return new Card(leadingSuit, lowestInSuit);
                }
            }
        } else {
            // throw new Exception("No valid strategy chosen");
            return null;
        }

    }

    // public Card highest_non_taking(leadingSuit, myCards) {
    // for card in suit[-1::]:
    // if card is higher than highest played so far:
    // continue;
    // else:
    // return card;
    //
    //
    // }

    private int lowestTaking(Suit s, Card takingCard) {
        int suitIndex = s.getIndex();
        for (int i = 2; i < 15; i++) {
            if (myHand[suitIndex][i]) {
                Card thisCard = new Card(suitIndex, i);
                if (thisCard.compareTo(takingCard) > 1) {
                    return i;
                }
            }
        }
        return -1; // no taking cards
    }

    // returns the rank of the highest card in my hand, in this suit
    private int highestRankInSuit(Suit s) {
        int suitIndex = s.getIndex();
        for (int i = 14; i > 1; i--) {
            if (myHand[suitIndex][i])
                return i;
        }
        return -1;
    }

    // returns the rank of the lowest card in my hand, in this suit
    private int lowestRankInSuit(Suit s) {
        int suitIndex = s.getIndex();
        for (int i = 2; i < 15; i++) {
            if (myHand[suitIndex][i])
                return i;
        }
        return -1;
    }

    // returns the highest rank of a card in the suit, under a given card
    private int highestRankInSuitUnderRank(Suit s, int rank) {
        int suitIndex = s.getIndex();
        for (int i = rank - 1; i > 1; i--) {
            if (myHand[suitIndex][i])
                return i;
        }
        return -1;
    }

    // returns lowest card in hand that is playable
    private Card myLowestCard(boolean includeHearts) {
        int maxSuit = 3;
        if (includeHearts)
            maxSuit = 4;
        for (int i = 2; i < 15; i++) {
            for (int j = 0; j < maxSuit; j++)
                if (myHand[i][j])
                    return new Card(i, j);
        }
        return null;
    }

    // returns highest card in hand that is playable
    private Card myHighestCard(boolean isHeartsBroken) {
        int maxSuit = 3;
        if (isHeartsBroken)
            maxSuit = 4;
        for (int i = 14; i > 1; i--) {
            for (int j = maxSuit; j >= 0; j--)
                if (myHand[i][j])
                    return new Card(i, j);
        }
        return null;
    }

    // tells whether there are unplayed cards above the current one in this round
    private boolean areUnplayedCardsAbove(Suit s, int rank, Round r) {
        int suitIndex = s.getIndex();
        boolean[][] playedCards = r.getPlayedCards();
        for (int i = rank + 1; i < 15; i++) {
            if (!playedCards[suitIndex][i])
                return false;
        }
        return true;
    }

    // QS -> AS -> KS -> AH -> KH -> QH -> JH -> highest rank card
    private Card powerCard(Round r) {
        boolean[][] playedCards = r.getPlayedCards();
        // is only concerned about dumping high spades if queen is still out
        if (!playedCards[2][12]) {
            if (myHand[2][12])
                return new Card(Suit.SPADES, 12);
            if (myHand[2][13])
                return new Card(Suit.SPADES, 13);
            if (myHand[2][14])
                return new Card(Suit.SPADES, 14);
        }
        if (myHand[3][14])
            return new Card(Suit.HEARTS, 14);
        if (myHand[3][13])
            return new Card(Suit.HEARTS, 13);
        if (myHand[3][12])
            return new Card(Suit.HEARTS, 12);
        if (myHand[3][11])
            return new Card(Suit.HEARTS, 11);
        return myHighestCard(true);
    }

    // returns a card that can guarantee taking the trick, otherwise null
    private Card guaranteedTake(Round r) {
        int maxCardsAbove = 13;
        for (int i = 14; i > 1; i--) {
            for (int s = 0; s < 4; s++) {
                if (myHand[s][i]) {
                    if (!areUnplayedCardsAbove(correspondingSuit(s), i, r)) {
                        return new Card(s, i);
                    }
                }
            }
        }
        return null;
    }

    private Suit correspondingSuit(int index) {
        switch (index) {
            case 0:
                return Suit.CLUBS;
            case 1:
                return Suit.DIAMONDS;
            case 2:
                return Suit.SPADES;
            default:
                return Suit.HEARTS;
        }
    }

    // deals the player a card
    public void giveCard(Card c) {
        myHand[c.getSuit().getIndex()][c.getRank()] = true;
        myCards.add(c);
    }

    public boolean isFirstLeader() {
        return isFirstLeader;
    }

}
