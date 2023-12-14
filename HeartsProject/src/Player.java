public class Player implements Comparable<Player> {
    private final Species species;
    private double totalPlacementSum; // used for recording information across games

    // creates a new player, with randomly assigned weights
    public Player(Species s) {
        species = s;
        totalPlacementSum = 0;
    }

    public void recordPlacement(double s) {
        totalPlacementSum += s;
    }

    public double getTotalPlacementSum() {
        return totalPlacementSum;
    }

    public Species getSpecies() {
        return species;
    }

    // THIS NEEDS TO BE IMPLEMENTED PROPERLY STILL!
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

    public int compareTo(Player other) {
        return (int) Math.signum(totalPlacementSum - other.totalPlacementSum);
    }

}
