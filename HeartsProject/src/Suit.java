public enum Suit {
    CLUBS(0),
    DIAMONDS(1),
    SPADES(2),
    HEARTS(3);

    private final int index;

    private Suit(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
