package ch.epfl.cs107.icmaze.actor.collectable;

import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;

public class Pickaxe extends ICMazeEquipment {

    private boolean collected = false;

    public Pickaxe(Area area, DiscreteCoordinates position, Orientation orientation) {
        super(
                area,
                orientation,
                position,
                "icmaze/pickaxe",   // sprite name
                0.75f,              // width (cohérent avec les objets du jeu)
                0.75f               // height
        );
    }

    /** Lorsque le joueur ramasse la pioche */
    public void collect() {
        if (!collected) {
            collected = true;
            getOwnerArea().unregisterActor(this);
        }
    }

    @Override
    public boolean takeCellSpace() {
        return false;   // ne bloque pas le passage
    }

    @Override
    public boolean isCellInteractable() {
        return true;    // le joueur doit marcher dessus pour la ramasser
    }

    @Override
    public boolean isViewInteractable() {
        return false;   // pas d'interaction à distance
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
    }
}

