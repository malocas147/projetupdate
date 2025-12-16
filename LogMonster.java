package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.Difficulty;
import ch.epfl.cs107.icmaze.RandomGenerator;
import ch.epfl.cs107.icmaze.actor.util.Cooldown;
import ch.epfl.cs107.icmaze.area.ICMazeArea;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.engine.actor.OrientedAnimation;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.window.Canvas;

import java.util.Queue;

/**
 * Monstre tronc (LogMonster) - ennemi capable de naviguer dans les labyrinthes.
 */
public class LogMonster extends PathFinderEnemy {

    public enum State {
        SLEEPING,
        RANDOM,
        TARGET
    }

    private static final int MAX_HP = 3;
    private static final float HIT_COOLDOWN_SECONDS = 0.30f;
    private static final int PERCEPTION_RADIUS = 5;
    private static final float REORIENT_COOLDOWN = 0.75f;
    private static final float STATE_COOLDOWN = 3.0f;
    private static final int DAMAGE_TO_PLAYER = 1;
    private static final int ANIMATION_DURATION = 30;

    private final OrientedAnimation sleepingAnimation;
    private final OrientedAnimation randomAnimation;
    private final OrientedAnimation targetAnimation;

    private final Cooldown reorientCooldown = new Cooldown(REORIENT_COOLDOWN);
    private final Cooldown transitionCooldown = new Cooldown(STATE_COOLDOWN);

    private State state;
    private DiscreteCoordinates targetPos = null;

    private final ICMazeInteractionVisitor handler = new LogMonsterInteractionHandler();

    public LogMonster(Area area, Orientation orientation, DiscreteCoordinates position, State initialState) {
        super(area, orientation, position, MAX_HP, HIT_COOLDOWN_SECONDS, PERCEPTION_RADIUS);
        this.state = initialState;

        Orientation[] sleepOrder = new Orientation[]{
                Orientation.DOWN, Orientation.LEFT, Orientation.UP, Orientation.RIGHT
        };
        sleepingAnimation = new OrientedAnimation(
                "icmaze/logMonster.sleeping",
                ANIMATION_DURATION / 3,
                this,
                new Vector(-0.5f, 0.25f),
                sleepOrder,
                1, 2, 2, 32, 32,
                true
        );

        Orientation[] randomOrder = new Orientation[]{
                Orientation.DOWN, Orientation.UP, Orientation.RIGHT, Orientation.LEFT
        };
        randomAnimation = new OrientedAnimation(
                "icmaze/logMonster_random",
                ANIMATION_DURATION / 3,
                this,
                new Vector(-0.5f, 0.25f),
                randomOrder,
                4, 2, 2, 32, 32,
                true
        );

        Orientation[] targetOrder = new Orientation[]{
                Orientation.DOWN, Orientation.UP, Orientation.RIGHT, Orientation.LEFT
        };
        targetAnimation = new OrientedAnimation(
                "icmaze/logMonster",
                ANIMATION_DURATION / 3,
                this,
                new Vector(-0.5f, 0.25f),
                targetOrder,
                4, 2, 2, 32, 32,
                true
        );
    }

    @Override
    public int getMaxHealth() {
        return MAX_HP;
    }

    @Override
    protected ICMazeInteractionVisitor getInteractionHandler() {
        return handler;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (isDead()) return;

        final double pTransition = computePTransition();

        switch (state) {
            case SLEEPING -> updateSleepingState(deltaTime, pTransition);
            case RANDOM -> updateRandomState(deltaTime, pTransition);
            case TARGET -> updateTargetState(deltaTime, pTransition);
        }

        updateAnimations(deltaTime);
    }

    /**
     * Comportement dans l'état SLEEPING
     */
    private void updateSleepingState(float deltaTime, double pTransition) {
        // 1. Réorientation : tourne à gauche
        if (reorientCooldown.ready(deltaTime)) {
            orientate(getOrientation().hisLeft());
            reorientCooldown.reset(); // ✅ RESET obligatoire
        }

        // 2. Transition d'état : vers RANDOM
        if (transitionCooldown.ready(deltaTime)) {
            if (RandomGenerator.rng.nextDouble() < pTransition) {
                state = State.RANDOM;
                transitionCooldown.reset(); // ✅ RESET obligatoire
            }
        }
    }

    /**
     * Comportement dans l'état RANDOM
     */
    private void updateRandomState(float deltaTime, double pTransition) {
        // 1. Réorientation et déplacement aléatoire
        if (reorientCooldown.ready(deltaTime)) {
            Orientation randomOrientation = Orientation.fromInt(RandomGenerator.rng.nextInt(4));
            orientate(randomOrientation);
            move(DEFAULT_MOVE_FRAMES);
            reorientCooldown.reset();
        }

        // 2. Transition d'état : vers TARGET si on a une cible
        if (transitionCooldown.ready(deltaTime)) {
            if (targetPos != null && RandomGenerator.rng.nextDouble() < pTransition) {
                state = State.TARGET;
                transitionCooldown.reset();
            }
        }
    }

    /**
     * Comportement dans l'état TARGET
     */
    private void updateTargetState(float deltaTime, double pTransition) {
        // Si on a perdu la cible, retourner en RANDOM
        if (targetPos == null) {
            state = State.RANDOM;
            return;
        }

        // 1. Calcul de l'orientation vers la cible et déplacement
        if (reorientCooldown.ready(deltaTime)) {
            Orientation nextOrientation = getNextOrientation();
            if (nextOrientation != null) {
                orientate(nextOrientation);
                move(DEFAULT_MOVE_FRAMES);
            }
            reorientCooldown.reset(); // ✅ RESET obligatoire
        }

        // 2. Transition d'état : vers SLEEPING
        if (transitionCooldown.ready(deltaTime)) {
            if (RandomGenerator.rng.nextDouble() < (1.0 - pTransition)) {
                state = State.SLEEPING;
                targetPos = null;  // Oublier la cible
                transitionCooldown.reset(); // ✅ RESET obligatoire
            }
        }
    }

    /**
     * Calcule la probabilité de transition selon la difficulté de l'aire
     */
    private double computePTransition() {
        int difficulty = Difficulty.MEDIUM;

        if (getOwnerArea() instanceof ICMazeArea area) {
            difficulty = area.getDifficulty();
        }

        return (double) Difficulty.HARDEST / (double) difficulty;
    }

    @Override
    protected Orientation getNextOrientation() {
        if (targetPos == null) return null;

        if (!(getOwnerArea() instanceof ICMazeArea area)) {
            return null;
        }

        Queue<Orientation> path = area.shortestPath(
                getCurrentMainCellCoordinates(),
                targetPos
        );

        if (path == null || path.isEmpty()) {
            return null;
        }

        return path.poll();
    }

    /**
     * Met à jour les animations selon l'état actuel
     */
    private void updateAnimations(float deltaTime) {
        switch (state) {
            case SLEEPING -> {
                sleepingAnimation.update(deltaTime);
                randomAnimation.reset();
                targetAnimation.reset();
            }
            case RANDOM -> {
                randomAnimation.update(deltaTime);
                sleepingAnimation.reset();
                targetAnimation.reset();
            }
            case TARGET -> {
                targetAnimation.update(deltaTime);
                sleepingAnimation.reset();
                randomAnimation.reset();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (isDead()) {
            super.draw(canvas);
            return;
        }

        health.draw(canvas);

        switch (state) {
            case SLEEPING -> sleepingAnimation.draw(canvas);
            case RANDOM -> randomAnimation.draw(canvas);
            case TARGET -> targetAnimation.draw(canvas);
        }
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        if (v instanceof ICMazeInteractionVisitor visitor) {
            visitor.interactWith(this, isCellInteraction);
        }
    }

    private class LogMonsterInteractionHandler implements ICMazeInteractionVisitor {
        @Override
        public void interactWith(ICMazePlayer player, boolean isCellInteraction) {
            if (state == State.SLEEPING) return;

            DiscreteCoordinates front = getCurrentMainCellCoordinates()
                    .jump(getOrientation().toVector());

            DiscreteCoordinates playerPos = player.getCurrentMainCellCoordinates();

            if (playerPos.equals(front)) {
                player.damage(DAMAGE_TO_PLAYER);
            } else {
                targetPos = playerPos;
            }
        }
    }
}