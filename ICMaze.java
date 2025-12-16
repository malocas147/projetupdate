package ch.epfl.cs107.icmaze;

import ch.epfl.cs107.icmaze.actor.ICMazePlayer;
import ch.epfl.cs107.icmaze.actor.Portal;
import ch.epfl.cs107.icmaze.area.ICMazeArea;

import ch.epfl.cs107.play.areagame.AreaGame;
import ch.epfl.cs107.play.io.FileSystem;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.window.Window;

public class ICMaze extends AreaGame {

    // Longueur du niveau (nombre d'aires labyrinthiques hors Spawn et Boss)
    private static final int LEVEL_LENGTH = 5;

    private ICMazeArea[] allAreas;
    private ICMazePlayer player;

    @Override
    public String getTitle() {
        return "ICMaze";
    }

    @Override
    public boolean begin(Window window, FileSystem fileSystem) {
        if (!super.begin(window, fileSystem)) {
            return false;
        }

        // Générer le niveau procéduralement
        allAreas = createAreas();

        // Ajouter toutes les aires au jeu
        for (ICMazeArea area : allAreas) {
            addArea(area);
        }

        // L'aire initiale est la première (Spawn)
        ICMazeArea spawnArea = allAreas[0];
        ICMazeArea current = (ICMazeArea) setCurrentArea(spawnArea.getTitle(), true);

        // Position initiale du joueur
        DiscreteCoordinates spawnPos = new DiscreteCoordinates(4, 4);

        player = new ICMazePlayer(current, spawnPos, window.getKeyboard());
        player.enterArea(current, spawnPos);

        return true;
    }

    /**
     * Crée les aires du niveau en utilisant le générateur procédural.
     */
    private ICMazeArea[] createAreas() {
        // Utiliser le LevelGenerator pour générer le niveau
        return LevelGenerator.generateLine(LEVEL_LENGTH);
    }

    @Override
    public void update(float deltaTime) {
        // RESET
        if (getWindow().getKeyboard().get(KeyBindings.RESET_GAME).isPressed()) {
            begin(getWindow(), getFileSystem());
            return;
        }

        super.update(deltaTime);

        // Mort du joueur -> reset
        if (player != null && !player.isAlive()) {
            begin(getWindow(), getFileSystem());
            return;
        }

        // Gestion des portails
        if (player != null) {
            Portal portal = player.consumeCrossingPortal();
            if (portal != null && portal.getDestinationArea() != null) {
                switchArea(portal);
            }
        }
    }

    /**
     * Change l'aire actuelle et transfère correctement le joueur.
     */
    private void switchArea(Portal portal) {
        // Quitter l'aire actuelle
        player.leaveArea();

        // Passer à l'aire de destination
        ICMazeArea nextArea = (ICMazeArea) setCurrentArea(portal.getDestinationArea(), false);

        // Position d'arrivée dans la nouvelle aire
        DiscreteCoordinates arrival = portal.getArrivalCoordinates();

        // Entrer dans la nouvelle aire (utilise la méthode de ICMazeActor)
        player.enterArea(nextArea, arrival);

        System.out.println("✅ Player switched to " + nextArea.getTitle() + " at position " + arrival);
    }
}