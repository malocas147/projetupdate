package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.play.areagame.actor.AreaEntity;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.engine.actor.Sprite;
import ch.epfl.cs107.play.window.Canvas;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;

import java.util.List;

public class Portal extends AreaEntity {

    public enum PortalState {
        OPEN,
        LOCKED,
        INVISIBLE
    }

    public static final int NO_KEY_ID = Integer.MIN_VALUE;

    private PortalState state;
    private String destinationArea;
    private DiscreteCoordinates arrivalCoordinates;
    private int keyId;

    private final Sprite invisibleSprite;
    private final Sprite lockedSprite;

    public Portal(Area area,
                  Orientation orientation,
                  DiscreteCoordinates position,
                  String destinationArea,
                  DiscreteCoordinates arrivalCoordinates,
                  int keyId) {
        super(area, orientation, position);
        this.destinationArea = destinationArea;
        this.arrivalCoordinates = arrivalCoordinates;
        this.keyId = keyId;
        this.state = PortalState.INVISIBLE; // par défaut invisible

        int o = orientation.ordinal();

        invisibleSprite = new Sprite(
                "icmaze/invisibleDoor_" + o,
                (o + 1) % 2 + 1,
                o % 2 + 1,
                this
        );

        lockedSprite = new Sprite(
                "icmaze/chained_wood_" + o,
                (o + 1) % 2 + 1,
                o % 2 + 1,
                this
        );
    }

    // --- Accesseurs / configuration ---

    public String getDestinationArea() {
        return destinationArea;
    }

    public DiscreteCoordinates getArrivalCoordinates() {
        return arrivalCoordinates;
    }

    public int getKeyId() {
        return keyId;
    }

    public PortalState getState() {
        return state;
    }

    public void setState(PortalState state) {
        this.state = state;
    }

    public void configureDestination(String destinationArea, DiscreteCoordinates arrivalCoordinates) {
        this.destinationArea = destinationArea;
        this.arrivalCoordinates = arrivalCoordinates;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    // --- Interactable ---

    @Override
    public List<DiscreteCoordinates> getCurrentCells() {
        // occupe sa cellule principale + celle "à côté", selon l’orientation
        DiscreteCoordinates coord = getCurrentMainCellCoordinates();
        return List.of(
                coord,
                coord.jump(new Vector(
                        (getOrientation().ordinal() + 1) % 2,
                        getOrientation().ordinal() % 2))
        );
    }

    @Override
    public boolean takeCellSpace() {
        // Traversable seulement s’il est ouvert
        return state != PortalState.OPEN;
    }

    @Override
    public boolean isCellInteractable() {
        return true;
    }

    @Override
    public boolean isViewInteractable() {
        // accepte toujours les interactions à distance
        return true;
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
    }

    @Override
    public void draw(Canvas canvas) {
        switch (state) {
            case INVISIBLE -> invisibleSprite.draw(canvas);
            case LOCKED -> lockedSprite.draw(canvas);
            case OPEN -> {
                // rien à dessiner, passage libre
            }
        }
    }
}
