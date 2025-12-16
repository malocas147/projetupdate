package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.icmaze.actor.util.Cooldown;
import ch.epfl.cs107.play.areagame.actor.MovableAreaEntity;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.math.Transform;

/**
 * Classe de base pour tous les acteurs du jeu ICMaze
 * (joueur, ennemis, etc.).
 */
public abstract class ICMazeActor extends MovableAreaEntity {

    /** Barre de vie dessinable au-dessus de l’acteur */
    protected final Health health;

    /** Gestion du délai entre deux coups reçus */
    protected final Cooldown hitCooldown;
    private boolean onHitCooldown = false;

    public ICMazeActor(Area area,
                       Orientation orientation,
                       DiscreteCoordinates position,
                       int maxHealth,
                       boolean isFriendly,
                       float hitCooldownSeconds) {
        super(area, orientation, position);

        // Barre de vie légèrement au-dessus de l’acteur
        this.health = new Health(this, Transform.I.translated(0.f, 1.75f), maxHealth, isFriendly);

        // Cooldown entre deux dégâts
        this.hitCooldown = new Cooldown(hitCooldownSeconds);
    }

    /** L’acteur est-il encore en vie ? */
    public boolean isAlive() {
        return health.isOn();
    }

    /** Inflige des dégâts en respectant le cooldown. */
    public void damage(int amount) {
        if (!onHitCooldown && isAlive()) {
            health.decrease(amount);
            onHitCooldown = true;
        }
    }

    /** Soigne l’acteur. */
    public void heal(int amount) {
        health.increase(amount);
    }

    /** Quitter l’aire actuelle. */
    public void leaveArea() {
        getOwnerArea().unregisterActor(this);
    }

    /** Entrer dans une nouvelle aire à une position donnée. */
    public void enterArea(Area area, DiscreteCoordinates position) {
        area.registerActor(this);
        area.setViewCandidate(this);
        setOwnerArea(area);
        setCurrentPosition(position.toVector());
        resetMotion();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Avance le cooldown des dégâts
        if (onHitCooldown && hitCooldown.ready(deltaTime)) {
            onHitCooldown = false;
        }
    }
}
