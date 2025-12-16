package ch.epfl.cs107.icmaze.area;

import ch.epfl.cs107.play.areagame.area.AreaBehavior;
import ch.epfl.cs107.play.areagame.actor.Interactable;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.window.Window;
import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;

/**
 * Behavior that interprets a behavior image: pixels -> MazeCellType
 */
public class ICMazeBehavior extends AreaBehavior {

    public enum MazeCellType {
        NONE(false),
        GROUND(true),
        WALL(false),
        HOLE(true);

        private final boolean traversable;

        MazeCellType(boolean traversable) {
            this.traversable = traversable;
        }

        public boolean isTraversable() {
            return traversable;
        }

        public static MazeCellType toType(int rgb) {
            // map the color values used in the project (from your earlier spec)
            switch (rgb) {
                case -16777216: // ground
                    return GROUND;
                case -14112955: // wall
                    return WALL;
                case -65536: // hole
                    return HOLE;
                default:
                    return NONE;
            }
        }
    }

    public ICMazeBehavior(Window window, String gridName) {
        super(window, gridName); // AreaBehavior loads the behavior image named gridName
        buildCells();
    }

    private void buildCells() {
        int w = getWidth();
        int h = getHeight();

        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                // Note: AreaBehavior.getRGB expects (r, c) with r = row index from top
                int rgb = getRGB(h - 1 - y, x);
                MazeCellType type = MazeCellType.toType(rgb);
                setCell(x, y, new ICMazeCell(x, y, type));
            }
        }
    }

    public MazeCellType getCellType(DiscreteCoordinates coords) {
        ICMazeCell cell = (ICMazeCell) getCell(coords.x, coords.y);
        return (cell != null) ? cell.type : MazeCellType.NONE;
    }

    public class ICMazeCell extends Cell {

        private final MazeCellType type;

        public ICMazeCell(int x, int y, MazeCellType type) {
            super(x, y);
            this.type = type;
        }

        @Override
        protected boolean canEnter(Interactable entity) {
            // 1) terrain must be traversable
            if (!type.isTraversable()) return false;

            // 2) no other occupant that takes cell space
            for (Interactable occupant : entities) {
                if (occupant.takeCellSpace() && entity.takeCellSpace()) return false;
            }
            return true;
        }

        @Override
        protected boolean canLeave(Interactable entity) {
            return true;
        }

        @Override
        public boolean takeCellSpace() {
            return !type.isTraversable();
        }

        @Override
        public boolean isViewInteractable() {
            return false;
        }

        @Override
        public boolean isCellInteractable() {
            return false;
        }

        @Override
        public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
            ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
        }
    }
}