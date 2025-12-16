package ch.epfl.cs107.icmaze;

import ch.epfl.cs107.icmaze.area.ICMazeArea;
import ch.epfl.cs107.icmaze.area.maps.BossArea;
import ch.epfl.cs107.icmaze.area.maps.LargeArea;
import ch.epfl.cs107.icmaze.area.maps.MediumArea;
import ch.epfl.cs107.icmaze.area.maps.SmallArea;
import ch.epfl.cs107.icmaze.area.maps.Spawn;
import ch.epfl.cs107.icmaze.actor.Portal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Générateur de niveaux procéduraux pour ICMaze.
 * Génère une succession linéaire d'aires avec difficulté croissante.
 */
public final class LevelGenerator {

    private LevelGenerator() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Représente une position dans le référentiel fictif du niveau.
     */
    private static class Position {
        final int x;
        final int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }

    /**
     * Directions possibles pour placer une nouvelle aire.
     * On ne peut jamais aller vers l'Ouest (arrière).
     */
    private enum Direction {
        NORTH(0, 1, ICMazeArea.AreaPortals.N, ICMazeArea.AreaPortals.S),
        EAST(1, 0, ICMazeArea.AreaPortals.E, ICMazeArea.AreaPortals.W),
        SOUTH(0, -1, ICMazeArea.AreaPortals.S, ICMazeArea.AreaPortals.N);

        final int dx;
        final int dy;
        final ICMazeArea.AreaPortals exitPortal;    // Sortie de l'aire actuelle
        final ICMazeArea.AreaPortals entryPortal;   // Entrée de la nouvelle aire

        Direction(int dx, int dy, ICMazeArea.AreaPortals exit, ICMazeArea.AreaPortals entry) {
            this.dx = dx;
            this.dy = dy;
            this.exitPortal = exit;
            this.entryPortal = entry;
        }
    }

    /**
     * Génère un niveau linéaire de longueur donnée.
     *
     * @param length Nombre d'aires labyrinthiques (hors Spawn et Boss)
     * @return Tableau d'aires dans l'ordre (Spawn, aires générées, Boss)
     */
    public static ICMazeArea[] generateLine(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }

        List<ICMazeArea> areas = new ArrayList<>();
        Set<Position> occupiedPositions = new HashSet<>();

        // 1. Créer l'aire de départ à [0, 0]
        Position currentPos = new Position(0, 0);
        occupiedPositions.add(currentPos);

        Spawn spawnArea = new Spawn();
        areas.add(spawnArea);

        ICMazeArea previousArea = spawnArea;
        Direction previousDirection = null;

        // 2. Générer les aires labyrinthiques
        for (int i = 0; i < length; i++) {
            // Calculer la progression
            double progress = (double) (i + 1) / length;

            // Calculer la difficulté
            int difficulty = switch ((int) (progress * 4)) {
                case 0 -> Difficulty.EASY;
                case 1 -> Difficulty.MEDIUM;
                case 2 -> Difficulty.HARD;
                default -> Difficulty.HARDEST;
            };

            // Choisir une direction libre
            Direction chosenDirection;
            Position nextPos;

            // Pour la première aire après Spawn, toujours aller vers l'Est
            if (i == 0) {
                chosenDirection = Direction.EAST;
                nextPos = new Position(currentPos.x + 1, currentPos.y);
            } else {
                // Pour les autres, choisir aléatoirement parmi les directions libres
                chosenDirection = null;
                nextPos = null;
                List<Direction> availableDirections = new ArrayList<>(List.of(Direction.values()));

                while (!availableDirections.isEmpty() && chosenDirection == null) {
                    int randomIndex = RandomGenerator.rng.nextInt(availableDirections.size());
                    Direction testDir = availableDirections.get(randomIndex);

                    Position testPos = new Position(
                            currentPos.x + testDir.dx,
                            currentPos.y + testDir.dy
                    );

                    if (!occupiedPositions.contains(testPos)) {
                        chosenDirection = testDir;
                        nextPos = testPos;
                        break;
                    } else {
                        availableDirections.remove(randomIndex);
                    }
                }

                // Si aucune direction libre trouvée, forcer vers l'Est
                if (chosenDirection == null) {
                    chosenDirection = Direction.EAST;
                    nextPos = new Position(currentPos.x + 1, currentPos.y);
                }
            }

            // Créer la nouvelle aire selon la progression
            ICMazeArea newArea = createAreaByProgress(progress, difficulty);

            // Définir quelle clé cette nouvelle aire doit contenir
            // Cette clé servira à ouvrir le portail de newArea vers l'aire suivante
            newArea.setExitKeyId(i + 1);

            areas.add(newArea);
            occupiedPositions.add(nextPos);

            // Déterminer la clé nécessaire pour sortir de previousArea vers newArea
            int keyToExit;
            if (i == 0) {
                // Pour sortir de Spawn, utiliser la clé qui est dans Spawn
                keyToExit = Integer.MAX_VALUE;
            } else {
                // Pour sortir de previousArea, utiliser la clé qui est dans previousArea
                // Cette clé a été définie à l'itération précédente : exitKeyId = i
                keyToExit = i;
            }

            // Connecter l'aire précédente à la nouvelle
            connectAreas(previousArea, newArea, chosenDirection, keyToExit);

            // Préparer pour la prochaine itération
            previousArea = newArea;
            previousDirection = chosenDirection;
            currentPos = nextPos;
        }

        // 3. Ajouter la BossArea à la fin
        BossArea bossArea = new BossArea();
        areas.add(bossArea);

        // Connecter la dernière aire au Boss
        // La dernière aire contient déjà la clé ID=length (définie dans la boucle)
        Direction finalDirection = Direction.EAST;
        connectAreas(previousArea, bossArea, finalDirection, length);

        // Retourner le tableau d'aires
        return areas.toArray(new ICMazeArea[0]);
    }

    /**
     * Crée une aire selon la progression du niveau.
     */
    private static ICMazeArea createAreaByProgress(double progress, int difficulty) {
        double r = RandomGenerator.rng.nextDouble();

        if (r < progress * progress) {
            return new LargeArea();
        }
        if (r < progress) {
            return new MediumArea();
        }
        return new SmallArea();
    }

    /**
     * Connecte deux aires dans une direction donnée.
     *
     * @param from Aire de départ
     * @param to Aire d'arrivée
     * @param direction Direction de connexion
     * @param keyIdToExit ID de la clé nécessaire pour sortir de 'from' vers 'to'
     */
    private static void connectAreas(ICMazeArea from, ICMazeArea to, Direction direction, int keyIdToExit) {
        // from -> to (sortie verrouillée si une clé est nécessaire)
        Portal.PortalState exitState = (keyIdToExit == 0 || keyIdToExit == Portal.NO_KEY_ID)
                ? Portal.PortalState.OPEN
                : Portal.PortalState.LOCKED;

        from.configurePortal(
                direction.exitPortal,
                to.getTitle(),
                to.getArrivalCoordinates(direction.entryPortal),
                exitState,
                keyIdToExit
        );

        // to -> from (retour toujours libre)
        to.configurePortal(
                direction.entryPortal,
                from.getTitle(),
                from.getArrivalCoordinates(direction.exitPortal),
                Portal.PortalState.OPEN,
                Portal.NO_KEY_ID
        );
    }
}