package ch.epfl.cs107.icmaze.area.maps;

import ch.epfl.cs107.icmaze.area.ICMazeArea;

public class BossArea extends ICMazeArea {

    public BossArea() {
        super("SmallArea", 8);
    }

    @Override
    protected void createArea() {
        // Zone Boss --> rien à générer maintenant
    }
}
