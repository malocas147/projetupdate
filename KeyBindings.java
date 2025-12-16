package ch.epfl.cs107.icmaze;

import static ch.epfl.cs107.play.window.Keyboard.E;
import static ch.epfl.cs107.play.window.Keyboard.ENTER;
import static ch.epfl.cs107.play.window.Keyboard.P;
import static ch.epfl.cs107.play.window.Keyboard.R;
import static ch.epfl.cs107.play.window.Keyboard.B;
import static ch.epfl.cs107.play.window.Keyboard.SPACE;
import static ch.epfl.cs107.play.window.Keyboard.UP;
import static ch.epfl.cs107.play.window.Keyboard.DOWN;
import static ch.epfl.cs107.play.window.Keyboard.LEFT;
import static ch.epfl.cs107.play.window.Keyboard.RIGHT;
import static ch.epfl.cs107.play.window.Keyboard.F;

/**
 * Interface KeyboardConfig
 * Définition des touches de déplacement des deux joueurs ainsi que d'autres
 * actions globales dans le jeu.
 */
public final class KeyBindings {

    /**
     * Touches utilisées pour le joueur rouge.
     *
     * up, left, down, right, pickaxe, interact, attack
     */
    public static final PlayerKeyBindings PLAYER_KEY_BINDINGS =
            new PlayerKeyBindings(UP, LEFT, DOWN, RIGHT, SPACE, E, F);

    /**
     * Touche pour passer au dialogue suivant.
     */
    public static final int NEXT_DIALOG = ENTER;

    /**
     * Touche pour réinitialiser le jeu.
     */
    public static final int RESET_GAME = R;

    /**
     * Touche pour mettre en pause.
     */
    public static final int PAUSE_GAME = P;

    /**
     * Touche pour se téléporter à la salle du boss.
     */
    public static final int BOSS_ROOM = B;

    private KeyBindings() {}

    /**
     * Touches utilisées pour un joueur.
     *
     * @param up         Déplacement haut
     * @param left       Déplacement gauche
     * @param down       Déplacement bas
     * @param right      Déplacement droite
     * @param pickaxe    Utilisation d’un objet pioche (ramassage / usage)
     * @param interact   Interaction à distance
     * @param attack     Attaque directe (pioche contre mur ou ennemi)
     */
    public record PlayerKeyBindings(int up, int left, int down, int right,
                                    int pickaxe, int interact, int attack) {
    }
}
