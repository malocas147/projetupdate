package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.play.areagame.actor.MovableAreaEntity;
import ch.epfl.cs107.play.areagame.actor.Interactable;
import ch.epfl.cs107.play.areagame.actor.Interactor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;

import java.util.Collections;
import java.util.List;

public abstract class Projectile extends MovableAreaEntity implements Interactor {
    private static final int MOVE_DURATION = 6;
    private static final int MAX_DISTANCE = 7;

    private int distanceRemaining;
    private boolean hasMoved = false;

    protected Projectile(Area area, Orientation orientation, DiscreteCoordinates position) {
        super(area, orientation, position);
        this.distanceRemaining = MAX_DISTANCE;
    }

    public abstract int getDamage();

    public void stopProjectile() {
        distanceRemaining = 0;
    }

    @Override
    public void update(float deltaTime) {
        if (distanceRemaining <= 0) {
            getOwnerArea().unregisterActor(this);
            return;
        }

        if (!hasMoved) {
            move(MOVE_DURATION);
            hasMoved = true;
        }

        if (!isDisplacementOccurs()) {
            distanceRemaining--;
            if (distanceRemaining > 0) {
                move(MOVE_DURATION);
            } else {
                getOwnerArea().unregisterActor(this);
            }
        }

        super.update(deltaTime);
    }

    @Override
    public List<DiscreteCoordinates> getCurrentCells() {
        return Collections.singletonList(getCurrentMainCellCoordinates());
    }

    @Override
    public boolean takeCellSpace() {
        return false;
    }

    @Override
    public boolean isCellInteractable() {
        return false;
    }

    @Override
    public boolean isViewInteractable() {
        return false;
    }

    @Override
    public List<DiscreteCoordinates> getFieldOfViewCells() {
        return Collections.emptyList();
    }

    @Override
    public boolean wantsCellInteraction() {
        return distanceRemaining > 0;
    }

    @Override
    public boolean wantsViewInteraction() {
        return false;
    }
}