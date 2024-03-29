import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;;

// one round of hearts, composed of 13 tricks
public class Round {
    private int tricksLeft;
    private final Hand[] playerHands;
    private final boolean[][] playedCards;
    private final int[] pointsTaken;
    private int leader;
    private boolean heartsBroken;

    // constructor
    public Round(Player[] players) {
        playedCards = new boolean[4][15]; // one extra entry per row, for 1-based indexing :)
        heartsBroken = false;
        tricksLeft = 13;
        // instantiates four player hands [TBD]
        playerHands = new Hand[4];
        for (int i = 0; i < 4; i++)
            playerHands[i] = new Hand(players[i], i);
        pointsTaken = new int[4];
        leader = -1;

        // creates, shuffles, and deals a deck
        List<Card> deck = new ArrayList<>();
        for (Suit s : Suit.values()) {
            for (int r = 2; r <= 14; r++) {
                deck.add(new Card(s, r));
            }
        }
        Collections.shuffle(deck);
        for (int i = 0; i < 52; i++) {
            playerHands[i % 4].giveCard(deck.get(i));
        }
        // at this point, optionally a passing phase -- we are omitting for now, for
        // simplicity
    }

    public void playTrick() {
        // System.out.println("Playing a trick!");
        // determines which player leads the first trick of a hand
        if (leader == -1) {
            for (int i = 0; i < 4; i++) {
                if (playerHands[i].isFirstLeader())
                    leader = i; // isFirstLeader is set when 2 of Clubs is dealt
            }
        }
        Trick thisTrick = new Trick(leader);

        // each remaining player plays their turn
        for (int i = 0; i < 4; i++) {
            // maybe add more to evaluate on, but in passing the Round we should be able to
            // use any
            // getter method to retrieve other state info
            int absolutePlayerIndex = (leader + i) % 4;
            Card c = playerHands[absolutePlayerIndex].playCard(this, thisTrick, i);
            // System.out.printf("Player %d plays %d of %s\n", absolutePlayerIndex,
            // c.getRank(), c.getSuit());
            thisTrick.playCard(c, i);
            playedCards[c.getSuit().getIndex()][c.getRank()] = true;
        }

        // gives the trick-taker their points, and assigns them as next leader
        int total = thisTrick.getPointsInTrick();
        int taker = thisTrick.getTakerIndex();
        pointsTaken[taker] += total; // Does this += notation work with arrays? I'm rusty, I hope so
        if (total > 0)
            heartsBroken = true;
        leader = taker;
        tricksLeft--;
    }

    public int[] getPointsTaken() {
        return pointsTaken;
    }

    public int getTotalPointsTaken() {
        int total = 0;
        for (int i = 0; i < 4; i++)
            total += pointsTaken[i];
        return total;
    }

    public boolean areHeartsBroken() {
        return heartsBroken;
    }

    public boolean[][] getPlayedCards() {
        return playedCards;
    }

    public int getLeader() {
        return leader;
    }

    public int getTricksLeft() {
        return tricksLeft;
    }

    public void playRound() {
        // System.out.println("NEW ROUND!!!");
        // passes cards first
        Card[][] passCards = new Card[4][3];
        for (int i = 0; i < 4; i++) {
            passCards[i] = playerHands[i].passCards(this);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                playerHands[i].giveCard(passCards[(i + 1) % 4][j]);
            }
        }
        while (continueRound())
            playTrick();

        // Check if anyone shot the moon successfully
        for (int i = 0; i < 4; i++) {
            if (pointsTaken[i] == 26) {
                // System.out.println("MOOOOOOOOOOOOOOON");
                pointsTaken[i] -= 26;
                // System.out.println(pointsTaken[i]);
                for (int j = 0; j < 4; j++) {
                    if (j == i)
                        continue;
                    pointsTaken[j] += 26;
                }
            }
        }
    }

    public boolean continueRound() {
        return tricksLeft != 0;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Tricks left: ");
        s.append(tricksLeft);
        s.append("\n");
        s.append("Scores: ");
        for (int i = 0; i < 4; i++) {
            s.append(i);
            s.append(": ");
            s.append(pointsTaken[i]);
            s.append("\n");
        }
        return s.toString();
    }

    public static void main(String[] args) {
        Player[] players = new Player[4];
        players[0] = new Player(Species.CHEATER, 0);
        players[1] = new Player(Species.CHEATER, 0);
        players[2] = new Player(Species.THREAT, 0);
        players[3] = new Player(Species.COOPERATOR, 0.2);
        Round round = new Round(players);
        System.out.println(round);
        while (round.continueRound()) {
            round.playTrick();
            System.out.println(round);
        }
    }
}
