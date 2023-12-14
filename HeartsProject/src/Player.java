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
    public Strategy getPlayerStrategy(Round r, int myIndex, double shootingRisk) {
        switch (species) {
            case CHEATER:
                return Strategy.AVOID_POINTS;
            case THREAT:
                // If all the points are still mine, I can still shoot
                if (r.getTotalPointsTaken() == r.getPointsTaken()[myIndex]) {
                    return Strategy.SHOOT;
                } else {
                    return Strategy.AVOID_POINTS; // Abandon shooting if someone takes hearts
                }
            case COOPERATOR:
                double threshold = 0.2;
                if (shootingRisk < threshold) {
                    return Strategy.AVOID_POINTS; // No need to cooperate if no perceived risk
                } else {
                    return Strategy.COOPERATE;
                }
            default:
                throw new IllegalStateException("Unexpected value: " + species);
        }
    }

    public int compareTo(Player other) {
        return (int) Math.signum(totalPlacementSum - other.totalPlacementSum);
    }

}
