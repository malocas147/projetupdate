package ch.epfl.cs107.icmaze.area.maps;

import ch.epfl.cs107.icmaze.MazeGenerator;
import ch.epfl.cs107.icmaze.actor.LogMonster;
import ch.epfl.cs107.icmaze.actor.Rock;
import ch.epfl.cs107.icmaze.actor.collectable.Key;
import ch.epfl.cs107.icmaze.area.ICMazeArea;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;

import java.util.Random;

public class MediumArea extends ICMazeArea {

    private static final int DIFFICULTY = 3;
    private static final int MONSTER_COUNT = 2;

    public MediumArea() {
        super("SmallArea", 16);
    }

    @Override
    protected void createArea() {
        int size = getSize();
        int[][] maze = MazeGenerator.createMaze(size, size, DIFFICULTY);

        DiscreteCoordinates entry = getArrivalCoordinates(ICMazeArea.AreaPortals.W);
        DiscreteCoordinates exit  = getArrivalCoordinates(ICMazeArea.AreaPortals.E);

        // Placer les rochers
        for (int y = 1; y < size - 1; ++y) {
            for (int x = 1; x < size - 1; ++x) {
                if (maze[y][x] == 1) {
                    DiscreteCoordinates p = new DiscreteCoordinates(x, y);
                    if (p.equals(entry) || p.equals(exit)) continue;
                    if (isAdjacentTo(p, entry) || isAdjacentTo(p, exit)) continue;

                    try {
                        registerActor(new Rock(this, Orientation.DOWN, p));
                    } catch (Exception e) {
                        // Si placement échoue, continuer
                    }
                }
            }
        }

        // Spawner les monstres
        spawnMonsters(maze, entry, exit, MONSTER_COUNT);

        // Placer la clé de sortie
        placeExitKey(maze, entry, exit);
    }

    /**
     * Place la clé de sortie dans le niveau
     */
    private void placeExitKey(int[][] maze, DiscreteCoordinates entry, DiscreteCoordinates exit) {
        // Récupérer l'ID de la clé via la méthode publique
        int keyId = getExitKeyId();

        if (keyId == 0) {
            System.out.println("MediumArea: No exit key required");
            return;
        }

        Random rng = new Random();
        int size = getSize();

        int tries = 0;
        while (tries < 2000) {
            tries++;
            int x = rng.nextInt(size - 2) + 1;
            int y = rng.nextInt(size - 2) + 1;

            // La clé doit être sur un chemin (0)
            if (maze[y][x] != 0) continue;

            DiscreteCoordinates keyPos = new DiscreteCoordinates(x, y);

            // Ne pas placer trop près de l'entrée
            if (keyPos.equals(entry) || isAdjacentTo(keyPos, entry)) continue;

            try {
                registerActor(new Key(this, keyPos, Orientation.DOWN, keyId));
                System.out.println("MediumArea: Placed exit key (ID=" + keyId + ") at " + keyPos);
                return;
            } catch (Exception e) {
                // Si placement échoue, continuer à essayer
            }
        }

        System.err.println("MediumArea: Failed to place exit key after " + tries + " tries!");
    }

    /**
     * Vérifie si deux positions sont adjacentes (distance Manhattan = 1)
     */
    private boolean isAdjacentTo(DiscreteCoordinates pos, DiscreteCoordinates target) {
        return Math.abs(pos.x - target.x) + Math.abs(pos.y - target.y) == 1;
    }

    /**
     * Spawne les monstres dans le niveau
     */
    private void spawnMonsters(int[][] maze, DiscreteCoordinates entry, DiscreteCoordinates exit, int count) {
        Random rng = new Random();
        int size = getSize();

        int spawned = 0;
        int tries = 0;

        while (spawned < count && tries < 4000) {
            tries++;
            int x = rng.nextInt(size - 2) + 1;
            int y = rng.nextInt(size - 2) + 1;

            // Placer uniquement sur les chemins
            if (maze[y][x] != 0) continue;

            DiscreteCoordinates p = new DiscreteCoordinates(x, y);

            // Ne pas bloquer l'entrée ou la sortie
            if (p.equals(entry) || p.equals(exit)) continue;
            if (isAdjacentTo(p, entry) || isAdjacentTo(p, exit)) continue;

            try {
                registerActor(new LogMonster(this, Orientation.DOWN, p, LogMonster.State.RANDOM));
                spawned++;
            } catch (Exception e) {
                // Si placement échoue, continuer à essayer
            }
        }

        System.out.println("MediumArea: Spawned " + spawned + "/" + count + " monsters (tries: " + tries + ")");
    }
}