package ch.epfl.cs107.icmaze.area.maps;

import ch.epfl.cs107.icmaze.area.ICMazeArea;
import ch.epfl.cs107.icmaze.actor.collectable.Heart;
import ch.epfl.cs107.icmaze.actor.collectable.Key;
import ch.epfl.cs107.icmaze.actor.collectable.Pickaxe;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;

public class Spawn extends ICMazeArea {

    public Spawn() {
        super("SmallArea", 8);
    }

    @Override
    protected void createArea() {
        registerActor(new Pickaxe(this, new DiscreteCoordinates(5, 4), Orientation.DOWN));
        registerActor(new Heart(this, new DiscreteCoordinates(4, 5)));
        registerActor(new Key(this, new DiscreteCoordinates(6, 5), Orientation.DOWN, Integer.MAX_VALUE));
        registerActor(new Key(this, new DiscreteCoordinates(1, 2), Orientation.DOWN, Integer.MAX_VALUE - 1));
    }
}