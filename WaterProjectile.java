package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.actor.Interactable;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.engine.actor.Animation;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.window.Canvas;

public class WaterProjectile extends Projectile {
    private static final int DAMAGE = 1;
    private static final int ANIMATION_DURATION = 12;

    private final Animation animation;
    private final ICMazeInteractionVisitor handler = new WaterProjectileInteractionHandler();

    public WaterProjectile(Area area, Orientation orientation, DiscreteCoordinates position) {
        super(area, orientation, position);
        this.animation = new Animation("icmaze/waterProjectile", 4, 1f, 1f, this, 32, 32,
                ANIMATION_DURATION / 4, true);
    }

    @Override
    public int getDamage() {
        return DAMAGE;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        animation.update(deltaTime);
    }

    @Override
    public void draw(Canvas canvas) {
        animation.draw(canvas);
    }

    @Override
    public void interactWith(Interactable other, boolean isCellInteraction) {
        other.acceptInteraction(handler, isCellInteraction);
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        if (v instanceof ICMazeInteractionVisitor visitor) {
            visitor.interactWith(this, isCellInteraction);
        }
    }

    private class WaterProjectileInteractionHandler implements ICMazeInteractionVisitor {
        @Override
        public void interactWith(ICMazePlayer player, boolean isCellInteraction) {
            if (isCellInteraction) {
                player.damage(DAMAGE);
                stopProjectile();
            }
        }

        @Override
        public void interactWith(Rock rock, boolean isCellInteraction) {
            if (isCellInteraction) {
                stopProjectile();
            }
        }
    }
}