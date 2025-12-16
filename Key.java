package ch.epfl.cs107.icmaze.actor.collectable;

import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.engine.actor.Sprite;
import ch.epfl.cs107.play.window.Canvas;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;

public class Key extends ICMazeEquipment {

    private final int id;
    private final Sprite sprite;

    public Key(Area area, DiscreteCoordinates position, Orientation orientation, int id) {
        super(area, orientation, position, "key", 0.75f, 0.75f);
        this.id = id;
        this.sprite = new Sprite("key", 0.75f, 0.75f, this);
    }

    public int getId() {
        return id;
    }

    @Override
    public void draw(Canvas canvas) {
        sprite.draw(canvas);
    }

    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
    }
}
