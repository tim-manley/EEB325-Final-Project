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
        if (placementIndex >= placementHistory.length) throw new Exception("Exceeded placement history length");
        placementHistory[placementIndex] = s;
        placementIndex++;
    }

    public double getAveragePlacement() {
        double total = 0.0;
        for (int i: placementHistory) total += i;
        return total / placementIndex;
    }

    public Species getSpecies() {
        return species;
    }

    public Strategy getPlayerStrategy() {
        switch (species) {
            case CHEATER:
                return Strategy.AVOID_POINTS;
            case THREAT:
                return Strategy.SHOOT; // FOR NOW, WANT TO ADD ABILITY TO ABANDON
            case COOPERATOR:
                return Strategy.AVOID_POINTS; // FOR NOW, NEED TO ADD ABILITY TO COOPERATE
            default:
                throw new IllegalStateException("Unexpected value: " + species);
        }
    }
}
