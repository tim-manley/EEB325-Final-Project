public class Player {
    static final int NUM_TRAITS = 10;
    static final double MUTATION_THRESHOLD = 0.05;
    private double[] traits;

    // creates a new player, with randomly assigned weights
    public Player() {
        traits = new double[NUM_TRAITS];
        for (int i = 0; i < NUM_TRAITS; i++) {
            traits[i] = Math.random();
        }
    }

    // creates a new player that is a child of a parent, with some mutation(s)
    public Player(Player parent) {
        traits = new double[NUM_TRAITS];
        double[] parentTraits = parent.getTraits();

        for (int i = 0; i < NUM_TRAITS; i++) {
            double traitValue = parentTraits[i];
            if (!doesMutate())  traits[i] = traitValue;
            else                traits[i] = mutatedValue(traitValue);
        }
    }

    public boolean doesMutate() {
        return Math.random() < MUTATION_THRESHOLD;
    }

    // returns traitValue shifted an amount between (-.05, .05)
    public double mutatedValue(double traitValue) {
        return traitValue + (Math.random() - 0.5) / 10.0;
        // maybe we don't want to bound this!
        // newValue = Math.max(newValue, 0.0); // bounds to 0 if negative
        // newValue = Math.min(newValue, 1.0); // bounds to 1 if over
        // return newValue;
    }

    public double[] getTraits() {
        return traits;
    }
}
