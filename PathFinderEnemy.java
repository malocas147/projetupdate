package ch.epfl.cs107.icmaze.actor;

import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;

import java.util.ArrayList;
import java.util.List;

/**
 * Catégorie d'ennemis capables de naviguer dans des labyrinthes.
 * Ils ont un champ de vision carré (rayon de perception).
 * Ils sont demandeurs UNIQUEMENT d'interactions à distance.
 */
public abstract class PathFinderEnemy extends Ennemy {

    /** Rayon de perception (carré autour de l'ennemi) */
    protected final int perceptionRadius;

    /**
     * Constructeur d'un PathFinderEnemy.
     *
     * @param area L'aire d'appartenance
     * @param orientation L'orientation initiale
     * @param position La position initiale
     * @param maxHealth Points de vie maximaux
     * @param hitCooldownSeconds Durée d'immunité après un coup
     * @param perceptionRadius Rayon de perception (taille du carré)
     */
    protected PathFinderEnemy(Area area,
                              Orientation orientation,
                              DiscreteCoordinates position,
                              int maxHealth,
                              float hitCooldownSeconds,
                              int perceptionRadius) {
        super(area, orientation, position, maxHealth, hitCooldownSeconds);
        this.perceptionRadius = perceptionRadius;
    }

    /**
     * Méthode abstraite : détermine la prochaine orientation selon la stratégie
     * de déplacement spécifique à chaque sous-classe.
     *
     * @return L'orientation vers laquelle se diriger, ou null si aucune direction disponible
     */
    protected abstract Orientation getNextOrientation();

    @Override
    public final boolean wantsCellInteraction() {
        // PathFinderEnemy ne demande PAS d'interactions de contact
        return false;
    }

    @Override
    public boolean wantsViewInteraction() {
        // PathFinderEnemy demande UNIQUEMENT des interactions à distance
        return true;
    }

    @Override
    public List<DiscreteCoordinates> getFieldOfViewCells() {
        // Champ de vision = carré centré sur l'ennemi
        DiscreteCoordinates center = getCurrentMainCellCoordinates();
        List<DiscreteCoordinates> cells = new ArrayList<>();

        for (int dx = -perceptionRadius; dx <= perceptionRadius; dx++) {
            for (int dy = -perceptionRadius; dy <= perceptionRadius; dy++) {
                cells.add(new DiscreteCoordinates(center.x + dx, center.y + dy));
            }
        }

        return cells;
    }
}