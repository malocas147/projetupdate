package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.RandomGenerator;
import ch.epfl.cs107.icmaze.actor.collectable.Key;
import ch.epfl.cs107.icmaze.actor.util.Cooldown;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.engine.actor.OrientedAnimation;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.window.Canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Boss extends Ennemy {
    private static final int MAX_HP = 5;
    private static final float HIT_COOLDOWN = 0.3f;
    private static final int DAMAGE_PER_HIT = 1;
    private static final int ANIMATION_DURATION = 60;
    private static final float BARRAGE_COOLDOWN = 3.0f;

    private final OrientedAnimation animation;
    private final Cooldown barrageCooldown = new Cooldown(BARRAGE_COOLDOWN);
    private final ICMazeInteractionVisitor handler = new BossInteractionHandler();

    private boolean isActive = false;
    private boolean alreadyAttacked = false; // ✅ mémorise si déjà attaqué au moins une fois
    private final List<DiscreteCoordinates> teleportPositions;

    public Boss(Area area, DiscreteCoordinates position) {
        super(area, Orientation.DOWN, position, MAX_HP, HIT_COOLDOWN);

        Vector anchor = new Vector(-0.5f, 0);
        Orientation[] orders = {Orientation.DOWN, Orientation.RIGHT, Orientation.UP, Orientation.LEFT};
        animation = new OrientedAnimation(
                "icmaze/boss",
                ANIMATION_DURATION / 4,
                this,
                anchor,
                orders,
                3, 2, 2, 32, 32,
                true
        );

        int size = 8;
        teleportPositions = new ArrayList<>();
        teleportPositions.add(new DiscreteCoordinates(size / 2, size - 1));
        teleportPositions.add(new DiscreteCoordinates(size / 2, 1));
        teleportPositions.add(new DiscreteCoordinates(1, size / 2));
        teleportPositions.add(new DiscreteCoordinates(size - 1, size / 2));
    }

    @Override
    public int getMaxHealth() {
        return MAX_HP;
    }

    @Override
    protected ICMazeInteractionVisitor getInteractionHandler() {
        return handler;
    }

    /**
     * Appelé quand le joueur touche le boss avec la pioche.
     * 1er coup : téléport + activation (pas de dégâts)
     * coups suivants : téléport + dégâts
     */
    public void hitByPickaxe(int damage) {
        if (isDead()) return;

        // téléporte à chaque coup validé
        teleportRandomly();

        // 1ère attaque : active, pas de dégâts
        if (!alreadyAttacked) {
            alreadyAttacked = true;
            if (!isActive) {
                isActive = true;
                barrageCooldown.reset();
            }
            return;
        }

        // attaques suivantes : dégâts
        super.damage(damage);

        if (isDead()) {
            dropKey();
        }
    }

    // On garde damage() normal (utile si autre source de dégâts)
    @Override
    public void damage(int amount) {
        super.damage(amount);
        if (isDead()) {
            dropKey();
        }
    }

    private void teleportRandomly() {
        DiscreteCoordinates current = getCurrentMainCellCoordinates();
        List<DiscreteCoordinates> availablePositions = new ArrayList<>();

        for (DiscreteCoordinates pos : teleportPositions) {
            if (!pos.equals(current)) {
                availablePositions.add(pos);
            }
        }

        if (!availablePositions.isEmpty()) {
            DiscreteCoordinates newPos = availablePositions.get(
                    RandomGenerator.rng.nextInt(availablePositions.size())
            );

            int dx = newPos.x - current.x;
            int dy = newPos.y - current.y;

            if (Math.abs(dx) > Math.abs(dy)) {
                orientate(dx > 0 ? Orientation.RIGHT : Orientation.LEFT);
            } else {
                orientate(dy > 0 ? Orientation.UP : Orientation.DOWN);
            }

            setCurrentPosition(newPos.toVector());
            resetMotion();
        }
    }

    private void dropKey() {
        int keyId = -1;
        getOwnerArea().registerActor(new Key(
                getOwnerArea(),
                getCurrentMainCellCoordinates(),
                Orientation.DOWN,
                keyId
        ));
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (isDead()) return;

        animation.update(deltaTime);

        if (!isActive) return;

        if (barrageCooldown.ready(deltaTime)) {
            fireBarrage();
            barrageCooldown.reset();
        }
    }

    private void fireBarrage() {
        int size = 8;
        DiscreteCoordinates bossPos = getCurrentMainCellCoordinates();
        Orientation currentOrientation = getOrientation();

        List<DiscreteCoordinates> spawnPositions = new ArrayList<>();

        if (currentOrientation == Orientation.UP || currentOrientation == Orientation.DOWN) {
            for (int x = 1; x < size - 1; x++) {
                DiscreteCoordinates pos = new DiscreteCoordinates(x, bossPos.y);
                if (!pos.equals(bossPos)) spawnPositions.add(pos);
            }
        } else {
            for (int y = 1; y < size - 1; y++) {
                DiscreteCoordinates pos = new DiscreteCoordinates(bossPos.x, y);
                if (!pos.equals(bossPos)) spawnPositions.add(pos);
            }
        }

        if (!spawnPositions.isEmpty()) {
            int skipIndex = RandomGenerator.rng.nextInt(spawnPositions.size());

            for (int i = 0; i < spawnPositions.size(); i++) {
                if (i == skipIndex) continue;

                DiscreteCoordinates spawnPos = spawnPositions.get(i);
                WaterProjectile projectile = new WaterProjectile(getOwnerArea(), currentOrientation, spawnPos);
                getOwnerArea().registerActor(projectile);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (isDead()) {
            super.draw(canvas);
            return;
        }

        int frameCount = (int) (System.currentTimeMillis() / 100);
        if (!isImmune() || frameCount % 2 == 0) {
            animation.draw(canvas);
        }

        if (health != null && !isImmune()) {
            health.draw(canvas);
        }
    }

    @Override
    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        if (v instanceof ICMazeInteractionVisitor visitor) {
            visitor.interactWith(this, isCellInteraction);
        }
    }

    @Override
    public List<DiscreteCoordinates> getFieldOfViewCells() {
        return Collections.singletonList(getCurrentMainCellCoordinates().jump(getOrientation().toVector()));
    }

    @Override
    public boolean wantsViewInteraction() {
        return !isDead(); // ✅ pour pouvoir le toucher même avant activation
    }

    @Override
    public boolean wantsCellInteraction() {
        return false;
    }

    private class BossInteractionHandler implements ICMazeInteractionVisitor {
        @Override
        public void interactWith(ICMazePlayer player, boolean isCellInteraction) {
            if (!isCellInteraction && isActive && !isDead()) {
                DiscreteCoordinates front = getCurrentMainCellCoordinates()
                        .jump(getOrientation().toVector());
                DiscreteCoordinates playerPos = player.getCurrentMainCellCoordinates();

                if (playerPos.equals(front)) {
                    player.damage(DAMAGE_PER_HIT);
                }
            }
        }
    }
}
