package ch.epfl.cs107.icmaze;


public final class Difficulty {
    private Difficulty() {
    }

    // Number represents "tightness" of the maximum room subdivision. A lower number effectively makes the room harder
    public static final int EASIEST = 10;
    public static final int EASY = 8;
    public static final int MEDIUM = 6;
    public static final int HARD = 4;
    public static final int HARDEST = 2;
}
