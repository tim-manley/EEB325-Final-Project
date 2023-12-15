import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Simulator {
    private ArrayList<Player> players;
    private final int numRounds;
    private final int numPlayers;
    private final int[][] frequenciesOverTime;
    private int currentRound;

    public Simulator(int numParticipants, int[] numOfSpecies, int n) {
        int NUM_GENS = 100;
        currentRound = 0;
        frequenciesOverTime = new int[3][NUM_GENS];
        numPlayers = numOfSpecies[0] + numOfSpecies[1] + numOfSpecies[2];
        numRounds = n;
        int totalIndividuals = 0;
        for (int i : numOfSpecies)
            totalIndividuals += i;
        assert (totalIndividuals == numParticipants);

        players = new ArrayList<Player>();

        for (int i = 0; i < numOfSpecies[0]; i++)
            players.add(new Player(Species.THREAT));
        for (int i = 0; i < numOfSpecies[1]; i++)
            players.add(new Player(Species.COOPERATOR));
        for (int i = 0; i < numOfSpecies[2]; i++)
            players.add(new Player(Species.CHEATER));

        for (int i = 0; i < NUM_GENS; i++) {
            recordFrequencies();
            playGeneration();
            stepGeneration();
        }
    }

    private void playGeneration() {
        /*
         * ArrayList<Player> threats = new ArrayList<>();
         * for (int i = 0; i < 25; i++)
         * threats.add(new Player(Species.THREAT));
         */
        // ArrayList<Player> wholePop = new ArrayList<>();
        // wholePop.addAll(threats);
        for (Player p : players)
            p.clearSum();
        // wholePop.addAll(players);

        int numGames = players.size() / 4;
        // System.out.printf("Num games: %d\n", numGames);
        for (int k = 0; k < numRounds; k++) {
            Collections.shuffle(players);
            for (int i = 0; i < numGames; i++) {
                Player[] thisGame = new Player[4];
                for (int j = 0; j < 4; j++) {
                    thisGame[j] = players.get(i * 4 + j);
                }
                playGame(thisGame);
            }
        }
        currentRound++;
    }

    // highest-ranked quartile represents most losing players, who die
    // lowest-ranked quartile represents most winning players, who reproduce
    // middle 50% live but do not reproduce
    private void stepGeneration() {
        Collections.sort(players);
        // overwrites these players with new players
        for (int i = 0; i < numPlayers / 4; i++)
            players.set(i + (3 * numPlayers / 4), new Player(players.get(i).getSpecies()));
    }

    // records the current population frequency data
    private void recordFrequencies() {
        for (int i = 0; i < numPlayers; i++) {
            Species s = players.get(i).getSpecies();
            if (s == Species.THREAT)
                frequenciesOverTime[0][currentRound] = frequenciesOverTime[0][currentRound] + 1;
            else if (s == Species.COOPERATOR)
                frequenciesOverTime[1][currentRound] = frequenciesOverTime[1][currentRound] + 1;
            else if (s == Species.CHEATER)
                frequenciesOverTime[2][currentRound] = frequenciesOverTime[2][currentRound] + 1;
        }
        // frequenciesOverTime[0][currentRound] = 25;
    }

    private void playGame(Player[] players) {
        int[] scores = new int[4];
        while (scores[0] < 100 && scores[1] < 100 && scores[2] < 100 && scores[3] < 100) {
            Round r = new Round(players);
            r.playRound();
            int[] results = r.getPointsTaken();
            for (int i = 0; i < 4; i++)
                scores[i] += results[i];
        }
        for (int i = 0; i < 4; i++) {
            // System.out.printf("Player %d strategy: %s\nPlayer score: %d\n", i,
            // players[i].getSpecies(), scores[i]);
        }
        tabulate(scores, players);
    }

    // takes results from one game, then adds the [0-3] ranked results to each
    // personal player's history
    // (lower scores are better)
    private void tabulate(int[] scores, Player[] players) {
        double[] tallies = new double[4];
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (scores[i] > scores[j])
                    tallies[i]++;
                else if (scores[j] > scores[i])
                    tallies[j]++;
                // shared penalty for tying
                else {
                    tallies[i] += 0.5;
                    tallies[j] += 0.5;
                }
            }
        }
        for (int i = 0; i < 4; i++)
            players[i].recordPlacement(tallies[i]);
    }

    public int[][] getFrequenciesOverTime() {
        return frequenciesOverTime;
    }

    public static void main(String[] args) {
        int[] numOfSpecies = { Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]) };
        Simulator s = new Simulator(100, numOfSpecies, Integer.parseInt(args[0]));
        System.out.println(Arrays.deepToString(s.frequenciesOverTime));
    }

}
