package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.actor.collectable.Heart;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.actor.AreaEntity;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.engine.actor.Animation;
import ch.epfl.cs107.play.engine.actor.Sprite;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Transform;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.window.Canvas;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Rock extends AreaEntity {

    private static final int DEFAULT_HP = 2;
    private static final double HEART_DROP_PROBABILITY = 0.5;
    private static final Random RNG = new Random();
    private static final int VANISH_FRAME_DURATION = 3;

    private final Sprite sprite;
    private int hp;
    private final int maxHp;

    // Barre de vie
    private final Health health;

    private Animation vanishAnimation = null;
    private boolean isDying = false;

    public Rock(Area area, Orientation orientation, DiscreteCoordinates position) {
        super(area, orientation, position);
        this.sprite = new Sprite("rock.2", 1f, 1f, this);
        this.hp = DEFAULT_HP;
        this.maxHp = DEFAULT_HP;

        // Initialiser la barre de vie (rouge car pas amical)
        this.health = new Health(this, Transform.I.translated(0.f, 1.5f), maxHp, false);
    }

    @Override
    public List<DiscreteCoordinates> getCurrentCells() {
        return Collections.singletonList(getCurrentMainCellCoordinates());
    }

    @Override
    public boolean takeCellSpace() {
        return !isDying; // Ne bloque plus l'espace une fois en train de disparaître
    }

    @Override
    public boolean isViewInteractable() {
        return true;
    }

    @Override
    public boolean isCellInteractable() {
        return true;
    }

    /**
     * Inflige des dégâts au rocher et déclenche son animation de disparition.
     */
    public void damage(int amount) {
        if (isDying) return;

        hp -= amount;
        health.decrease(amount);

        if (hp <= 0) {
            isDying = true;

            // Extraction des 7 frames
            Sprite[] vanishFrames = Sprite.extractSprites(
                    "icmaze/vanish",
                    7,
                    2, 2,
                    this,
                    32, 32
            );

            for (Sprite s : vanishFrames) {
                s.setAnchor(new Vector(-0.5f, 0f));
            }

            vanishAnimation = new Animation(
                    VANISH_FRAME_DURATION,
                    vanishFrames,
                    false
            );
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (isDying && vanishAnimation != null) {
            vanishAnimation.update(deltaTime);

            if (vanishAnimation.isCompleted()) {
                Area owner = getOwnerArea();
                owner.unregisterActor(this);

                // Drop de Heart
                if (RNG.nextDouble() < HEART_DROP_PROBABILITY) {
                    owner.registerActor(new Heart(owner, getCurrentMainCellCoordinates()));
                }
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (isDying && vanishAnimation != null) {
            vanishAnimation.draw(canvas);
        } else {
            sprite.draw(canvas);
            // Dessiner la barre de vie seulement si le rocher est endommagé
            if (hp < maxHp) {
                health.draw(canvas);
            }
        }
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        if (v instanceof ICMazeInteractionVisitor) {
            ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
        }
    }
}