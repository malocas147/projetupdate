package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.actor.Interactable;
import ch.epfl.cs107.play.areagame.actor.Interactor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.engine.actor.Animation;
import ch.epfl.cs107.play.engine.actor.Sprite;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.window.Canvas;

import java.util.Collections;
import java.util.List;

/**
 * Classe de base pour tous les ennemis dans ICMaze.
 */
public abstract class Ennemy extends ICMazeActor implements Interactor {

    protected static final int DEFAULT_MOVE_FRAMES = 10;

    private static final int VANISH_FRAMES = 7;
    private static final int VANISH_DURATION = 24;

    private Animation dyingAnimation;
    private boolean dying = false;

    protected Ennemy(Area area,
                     Orientation orientation,
                     DiscreteCoordinates position,
                     int maxHealth,
                     float hitCooldownSeconds) {
        super(area, orientation, position, maxHealth, false, hitCooldownSeconds);
    }

    public abstract int getMaxHealth();

    public final boolean isDead() {
        return !isAlive();
    }

    @Override
    public boolean takeCellSpace() {
        return !(isDead() || dying);
    }

    @Override
    public boolean isCellInteractable() {
        return true;
    }

    @Override
    public boolean isViewInteractable() {
        return true;
    }

    @Override
    public List<DiscreteCoordinates> getCurrentCells() {
        return Collections.singletonList(getCurrentMainCellCoordinates());
    }

    @Override
    public List<DiscreteCoordinates> getFieldOfViewCells() {
        return Collections.singletonList(
                getCurrentMainCellCoordinates().jump(getOrientation().toVector())
        );
    }

    @Override
    public boolean wantsCellInteraction() {
        return true;
    }

    @Override
    public boolean wantsViewInteraction() {
        return true;
    }

    @Override
    public void interactWith(Interactable other, boolean isCellInteraction) {
        other.acceptInteraction(getInteractionHandler(), isCellInteraction);
    }

    protected abstract ICMazeInteractionVisitor getInteractionHandler();

    @Override
    public void damage(int amount) {
        super.damage(amount);

        if (!dying && isDead()) {
            dying = true;

            Sprite[] frames = Sprite.extractSprites(
                    "icmaze/vanish",
                    VANISH_FRAMES,
                    2, 2,
                    this,
                    32, 32
            );
            for (Sprite s : frames) {
                s.setAnchor(new Vector(-0.5f, 0f));
            }

            dyingAnimation = new Animation(VANISH_DURATION / VANISH_FRAMES, frames, false);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (dying && dyingAnimation != null) {
            dyingAnimation.update(deltaTime);
            if (dyingAnimation.isCompleted()) {
                getOwnerArea().unregisterActor(this);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isDead() && !dying) {
            health.draw(canvas);
        }

        if (dying && dyingAnimation != null) {
            dyingAnimation.draw(canvas);
        }
    }
}