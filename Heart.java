package ch.epfl.cs107.icmaze.actor.collectable;

import ch.epfl.cs107.icmaze.handler.ICMazeInteractionVisitor;
import ch.epfl.cs107.play.areagame.area.Area;
import ch.epfl.cs107.play.engine.actor.Animation;
import ch.epfl.cs107.play.areagame.handler.AreaInteractionVisitor;
import ch.epfl.cs107.play.math.DiscreteCoordinates;
import ch.epfl.cs107.play.window.Canvas;

public class Heart extends ICMazeCollectable {

    // durée totale de l’animation de base (comme dans l’énoncé)
    private static final int ANIMATION_DURATION = 24;

    private final Animation animation;

    public Heart(Area area, DiscreteCoordinates position) {
        super(area, position);

        // new Animation("icmaze/heart", 4, 1, 1, this , 16, 16,
        //               ANIMATION_DURATION/4, true)
        animation = new Animation(
                "icmaze/heart",
                4,                   // nbFrames
                1f, 1f,              // width, height (taille en monde)
                this,
                16, 16,              // taille d’un frame en pixels
                ANIMATION_DURATION / 4,
                true                 // boucle
        );
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        animation.update(deltaTime);
    }

    @Override
    public void draw(Canvas canvas) {
        animation.draw(canvas);
    }

    public void acceptInteraction(AreaInteractionVisitor v, boolean isCellInteraction) {
        ((ICMazeInteractionVisitor) v).interactWith(this, isCellInteraction);
    }
}
