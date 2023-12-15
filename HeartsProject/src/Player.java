public class Player implements Comparable<Player> {
    private final Species species;
    private double totalPlacementSum; // used for recording information across games

    // creates a new player, with randomly assigned weights
    public Player(Species s) {
        species = s;
        totalPlacementSum = 0;
    }

    public void clearSum() {
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

    public Strategy getPlayerStrategy(Round r, int myIndex, double shootingRisk) {
        switch (species) {
            case CHEATER:
                return Strategy.AVOID_POINTS;
            case THREAT:
            // If all the points are still mine, I can still shoot
                if (r.getTotalPointsTaken() == r.getPointsTaken()[myIndex])
                    return Strategy.SHOOT;
                return Strategy.AVOID_POINTS; // Abandon shooting if someone takes hearts
            case COOPERATOR:
                double threshold = 0.2;
                if (shootingRisk < threshold)
                    return Strategy.AVOID_POINTS; // No need to cooperate if no perceived risk
                return Strategy.COOPERATE;
            default:
                System.out.println("ERROR!");
                return null;
        }
    }

    public int compareTo(Player other) {
        return (int) Math.signum(totalPlacementSum - other.totalPlacementSum);
    }

}
