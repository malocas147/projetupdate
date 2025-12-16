package ch.epfl.cs107.icmaze.actor;

/**
 * Ensemble des états possibles d'un joueur dans ICMaze.
 * Chaque état indique si le joueur peut se déplacer,
 * s'il est en action, ou s'il doit temporairement rester immobile.
 */
public enum PlayerState {

    /**
     * Le joueur est immobile mais disponible pour effectuer des actions ou démarrer un mouvement.
     */
    IDLE(true),

    /**
     * Le joueur est en mouvement dans une direction.
     */
    MOVING(true),

    /**
     * Le joueur réalise une interaction (levier, clé, portail, dialogue…).
     * Il ne peut pas se déplacer tant que la touche d’interaction reste enfoncée.
     */
    INTERACTING(false),

    /**
     * Le joueur attaque avec la pioche.
     * Animation obligatoire → le joueur ne peut pas bouger durant cet état.
     */
    ATTACKING_WITH_PICKAXE(false),

    /**
     * Le joueur traverse un portail.
     * Immobilisé durant la transition.
     */
    CROSSING_PORTAL(false);

    /** true si le joueur peut se déplacer dans cet état */
    private final boolean movable;

    PlayerState(boolean movable) {
        this.movable = movable;
    }

    /**
     * @return true si ce state autorise les déplacements.
     */
    public boolean isMovable() {
        return movable;
    }

    /**
     * @return true si ce state autorise les interactions.
     * Les états d’attaque et de portail n’autorisent aucune interaction.
     */
    public boolean canInteract() {
        return this == IDLE || this == MOVING;
    }

    /**
     * @return true si le joueur effectue une action qui doit bloquer le mouvement.
     */
    public boolean blocksMovement() {
        return !movable;
    }
}
