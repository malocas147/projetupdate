package ch.epfl.cs107.icmaze.area;

import ch.epfl.cs107.icmaze.actor.Portal;
import ch.epfl.cs107.play.areagame.AreaGraph;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.engine.actor.Background;
import ch.epfl.cs107.play.io.FileSystem;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.math.Orientation;
import ch.epfl.cs107.play.window.Window;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

public abstract class ICMazeArea extends Area {

    public enum AreaPortals {
        N(Orientation.UP),
        W(Orientation.LEFT),
        S(Orientation.DOWN),
        E(Orientation.RIGHT);

        private final Orientation orientation;
        AreaPortals(Orientation orientation) { this.orientation = orientation; }
        public Orientation getOrientation() { return orientation; }
    }

    private final String gridName;
    private final int size;
    private ICMazeBehavior behavior;
    private final Map<AreaPortals, Portal> portals = new EnumMap<>(AreaPortals.class);

    private AreaGraph graph;

    // ID de la clé de sortie pour cette aire (0 = pas de clé nécessaire)
    private int exitKeyId = 0;

    protected ICMazeArea(String gridName, int size) {
        this.gridName = gridName;
        this.size = size;
        initPortals();
    }

    public int getSize() {
        return size;
    }

    public ICMazeBehavior getBehavior() {
        return behavior;
    }

    public int getDifficulty() {
        return ch.epfl.cs107.icmaze.Difficulty.MEDIUM;
    }

    protected final void setGraph(AreaGraph graph) {
        this.graph = graph;
    }

    public final Queue<Orientation> shortestPath(DiscreteCoordinates from, DiscreteCoordinates to) {
        if (graph == null) return null;
        return graph.shortestPath(from, to);
    }

    // Méthodes publiques pour gérer l'ID de clé de sortie
    public final void setExitKeyId(int keyId) {
        this.exitKeyId = keyId;
    }

    public final int getExitKeyId() {
        return exitKeyId;
    }

    private static final double DYNAMIC_SCALE_MULTIPLIER = 1.375;
    private static final double MAXIMUM_SCALE = 30.0;

    @Override
    public final float getCameraScaleFactor() {
        return (float) Math.min(size * DYNAMIC_SCALE_MULTIPLIER, MAXIMUM_SCALE);
    }

    @Override
    public final String getTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean begin(Window window, FileSystem fileSystem) {
        if (!super.begin(window, fileSystem)) return false;

        behavior = new ICMazeBehavior(window, gridName);
        setBehavior(behavior);

        registerActor(new Background(this, gridName));

        for (Portal portal : portals.values()) registerActor(portal);

        createArea();
        return true;
    }

    protected abstract void createArea();

    private void initPortals() {
        for (AreaPortals ap : AreaPortals.values()) {
            DiscreteCoordinates mainPos = portalPosition(ap);
            Orientation ori = ap.getOrientation().opposite();
            Portal portal = new Portal(this, ori, mainPos, null, null, Portal.NO_KEY_ID);
            portals.put(ap, portal);
        }
    }

    private DiscreteCoordinates portalPosition(AreaPortals portal) {
        int half = size / 2;
        return switch (portal) {
            case N -> new DiscreteCoordinates(half, size + 1);
            case S -> new DiscreteCoordinates(half, 0);
            case W -> new DiscreteCoordinates(0, half);
            case E -> new DiscreteCoordinates(size + 1, half);
        };
    }

    public DiscreteCoordinates getArrivalCoordinates(AreaPortals entrySide) {
        int half = size / 2;
        return switch (entrySide) {
            case N -> new DiscreteCoordinates(half + 1, size);
            case S -> new DiscreteCoordinates(half + 1, 1);
            case W -> new DiscreteCoordinates(1, half + 1);
            case E -> new DiscreteCoordinates(size, half + 1);
        };
    }

    public void configurePortal(AreaPortals side,
                                String destinationArea,
                                DiscreteCoordinates arrivalCoordinates,
                                Portal.PortalState state,
                                int keyId) {
        Portal portal = portals.get(side);
        if (portal != null) {
            portal.configureDestination(destinationArea, arrivalCoordinates);
            portal.setState(state);
            portal.setKeyId(keyId);
        }
    }

    public void setPortalState(AreaPortals side, Portal.PortalState state) {
        Portal portal = portals.get(side);
        if (portal != null) portal.setState(state);
    }
}