public class Player {
    private final Species species;
    private final int[] placementHistory; // used for recording information across games
    private int placementIndex;

    // creates a new player, with randomly assigned weights
    public Player(Species s, int numGames) {
        placementIndex = 0;
        species = s;
        placementHistory = new int[numGames];
    }

    public void recordPlacement(int s) throws Exception {
        if (placementIndex >= placementHistory.length)
            throw new Exception("Exceeded placement history length");
        placementHistory[placementIndex] = s;
        placementIndex++;
    }

    public double getAveragePlacement() {
        double total = 0.0;
        for (int i : placementHistory)
            total += i;
        return total / placementIndex;
    }

    public Species getSpecies() {
        return species;
    }

    public Strategy getPlayerStrategy(Round r, int myIndex, double shootingRisk) {
        switch (species) {
            case CHEATER:
                return Strategy.AVOID_POINTS;
            case THREAT:
                // If I have all the points so far
                if (r.getTotalPointsTaken() == r.getPointsTaken()[myIndex]) {
                    return Strategy.SHOOT;
                } else {
                    return Strategy.AVOID_POINTS;
                }
            case COOPERATOR:
                double threshold = 0.2;
                if (shootingRisk < threshold) {
                    return Strategy.AVOID_POINTS;
                } else {
                    return Strategy.COOPERATE;
                }
                // return Strategy.AVOID_POINTS; // FOR NOW, NEED TO ADD ABILITY TO COOPERATE
            default:
                throw new IllegalStateException("Unexpected value: " + species);
        }
    }
}
