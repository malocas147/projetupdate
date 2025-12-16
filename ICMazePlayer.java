package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.KeyBindings;
import ch.epfl.cs107.icmaze.KeyBindings.PlayerKeyBindings;
import ch.epfl.cs107.icmaze.actor.collectable.Heart;
import ch.epfl.cs107.icmaze.actor.collectable.Key;
import ch.epfl.cs107.icmaze.actor.collectable.Pickaxe;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;

import ch.epfl.cs107.play.areagame.actor.Interactable;
import ch.epfl.cs107.play.areagame.actor.Interactor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;

import ch.epfl.cs107.play.engine.actor.OrientedAnimation;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Vector;

import ch.epfl.cs107.play.window.Audio;
import ch.epfl.cs107.play.window.Button;
import ch.epfl.cs107.play.window.Canvas;
import ch.epfl.cs107.play.window.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ICMazePlayer extends ICMazeActor implements Interactor {

    private static final int BASE_ANIMATION_DURATION = 4;
    private static final int PICKAXE_ANIMATION_DURATION = 5;

    private final Keyboard keyboard;
    private final ICMazePlayerInteractionHandler handler = new ICMazePlayerInteractionHandler();

    private PlayerState state = PlayerState.IDLE;

    private boolean hasPickaxe = false;
    private boolean hasAttacked = false; // Pour éviter les attaques multiples

    // animations
    private final OrientedAnimation baseAnimation;
    private final OrientedAnimation pickaxeAnimation;

    // inventaire de clés
    private final Set<Integer> collectedKeys = new HashSet<>();

    // portail en cours de traversée
    private Portal crossingPortal = null;

    public ICMazePlayer(Area area, DiscreteCoordinates position, Keyboard keyboard) {
        super(area, Orientation.DOWN, position, 5, true, 0.3f);
        this.keyboard = keyboard;

        // Animation de base
        baseAnimation = new OrientedAnimation(
                "icmaze/player",
                BASE_ANIMATION_DURATION,
                this,
                new Vector(0f,0f),
                new Orientation[]{Orientation.DOWN, Orientation.RIGHT, Orientation.UP, Orientation.LEFT},
                4, 1, 2, 16, 32, true
        );

        // Animation pioche
        pickaxeAnimation = new OrientedAnimation(
                "icmaze/player.pickaxe",
                PICKAXE_ANIMATION_DURATION,
                this,
                new Vector(-0.5f, 0f),
                new Orientation[]{Orientation.DOWN, Orientation.UP, Orientation.RIGHT, Orientation.LEFT},
                4, 2, 2, 32, 32
        );
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        handleInteractionState();
        handleAttackState();

        if (state.isMovable()) {
            handleMovement();
        }

        // Animation de base
        if (state == PlayerState.MOVING)
            baseAnimation.update(deltaTime);
        else
            baseAnimation.reset();

        // Animation pioche
        if (state == PlayerState.ATTACKING_WITH_PICKAXE) {
            pickaxeAnimation.update(deltaTime);

            if (pickaxeAnimation.isCompleted()) {
                pickaxeAnimation.reset();
                state = PlayerState.IDLE;
                hasAttacked = false; // Réinitialiser pour la prochaine attaque
            }
        }
    }

    /** Gère l'entrée dans l'état d'attaque */
    private void handleAttackState() {
        PlayerKeyBindings keys = KeyBindings.PLAYER_KEY_BINDINGS;
        Button attack = keyboard.get(keys.attack());

        if (state == PlayerState.IDLE && hasPickaxe && attack.isPressed()) {
            state = PlayerState.ATTACKING_WITH_PICKAXE;
            pickaxeAnimation.reset();
            hasAttacked = false; // Nouvelle attaque commence
        }
    }

    public boolean hasKey(int id) {
        return collectedKeys.contains(id);
    }

    private void handleInteractionState() {
        PlayerKeyBindings keys = KeyBindings.PLAYER_KEY_BINDINGS;
        Button interact = keyboard.get(keys.interact());

        switch (state) {
            case IDLE -> {
                if (interact.isPressed()) state = PlayerState.INTERACTING;
            }
            case INTERACTING -> {
                if (!interact.isDown()) state = PlayerState.IDLE;
            }
        }
    }

    private void handleMovement() {
        if (isDisplacementOccurs()) {
            state = PlayerState.MOVING;
            return;
        }

        PlayerKeyBindings keys = KeyBindings.PLAYER_KEY_BINDINGS;

        Button up = keyboard.get(keys.up());
        Button down = keyboard.get(keys.down());
        Button left = keyboard.get(keys.left());
        Button right = keyboard.get(keys.right());

        if (up.isDown()) {
            orientate(Orientation.UP);
            move(8);
            state = PlayerState.MOVING;
        } else if (down.isDown()) {
            orientate(Orientation.DOWN);
            move(8);
            state = PlayerState.MOVING;
        } else if (left.isDown()) {
            orientate(Orientation.LEFT);
            move(8);
            state = PlayerState.MOVING;
        } else if (right.isDown()) {
            orientate(Orientation.RIGHT);
            move(8);
            state = PlayerState.MOVING;
        } else {
            state = PlayerState.IDLE;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (state == PlayerState.ATTACKING_WITH_PICKAXE)
            pickaxeAnimation.draw(canvas);
        else
            baseAnimation.draw(canvas);

        health.draw(canvas);
    }

    @Override
    public List<DiscreteCoordinates> getCurrentCells() {
        return List.of(getCurrentMainCellCoordinates());
    }

    @Override
    public List<DiscreteCoordinates> getFieldOfViewCells() {
        return List.of(
                getCurrentMainCellCoordinates().jump(getOrientation().toVector())
        );
    }

    @Override
    public boolean takeCellSpace() {
        return true;
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
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        if (v instanceof ICMazeInteractionVisitor visitor) {
            visitor.interactWith(this, isCellInteraction);
        }
    }

    @Override
    public boolean wantsCellInteraction() {
        return true;
    }

    @Override
    public boolean wantsViewInteraction() {
        return true;
    }

    public void interactWith(Interactable other) {
        other.acceptInteraction(handler, false);
    }

    @Override
    public void interactWith(Interactable other, boolean isCellInteraction) {
        other.acceptInteraction(handler, isCellInteraction);
    }

    @Override
    public void bip(Audio audio) {
        super.bip(audio);
    }

    /** Handler d'interactions */
    private class ICMazePlayerInteractionHandler implements ICMazeInteractionVisitor {
        @Override
        public void interactWith(Heart heart, boolean isCellInteraction) {
            if (isCellInteraction) {
                heart.collect();
                heal(1);
            }
        }

        @Override
        public void interactWith(Pickaxe pickaxe, boolean isCellInteraction) {
            if (isCellInteraction) {
                pickaxe.collect();
                hasPickaxe = true;
            }
        }

        @Override
        public void interactWith(Rock rock, boolean isCellInteraction) {
            // Attaque en VIEW interaction (devant le joueur) uniquement
            if (!isCellInteraction &&
                    state == PlayerState.ATTACKING_WITH_PICKAXE &&
                    !hasAttacked) {
                rock.damage(1);
                hasAttacked = true; // Marquer qu'on a déjà attaqué dans cette animation
            }
        }

        @Override
        public void interactWith(Key key, boolean isCellInteraction) {
            if (isCellInteraction) {
                key.collect();
                collectedKeys.add(key.getId());
            }
        }

        @Override
        public void interactWith(Portal portal, boolean isCellInteraction) {
            // Remote unlocking (interact button)
            if (!isCellInteraction && state == PlayerState.INTERACTING) {
                if (portal.getState() == Portal.PortalState.LOCKED) {
                    int needed = portal.getKeyId();

                    if (needed == Portal.NO_KEY_ID
                            || ICMazePlayer.this.hasKey(needed)) {
                        portal.setState(Portal.PortalState.OPEN);
                    }
                }
            }

            // Crossing
            if (isCellInteraction && portal.getState() == Portal.PortalState.OPEN) {
                crossingPortal = portal;
            }
        }
    }

    public Portal consumeCrossingPortal() {
        Portal p = crossingPortal;
        crossingPortal = null;
        return p;
    }
}