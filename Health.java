package ch.epfl.cs107.icmaze.actor;


import ch.epfl.cs107.play.engine.actor.Graphics;
import ch.epfl.cs107.play.math.Positionable;
import ch.epfl.cs107.play.math.Transform;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.math.shape.Polygon;
import ch.epfl.cs107.play.signal.logic.Logic;
import ch.epfl.cs107.play.window.Canvas;

import java.awt.Color;
import java.util.List;

/**
 * Health is an object which stores the health points for an entity.
 * It implements Graphics, so that the health bar can be drawn. The health bar
 * is either green or red if the entity is friendly or not.
 * <p>
 * Health also implements Logic, which allows to know if the entity lost all of its health points or not.
 */
public class Health implements Graphics, Logic {

    /**
     * A positionable to the parent entity to draw the health bar on top of
     */
    private final Positionable parent;
    /**
     * The transform from the entity, because each entity has a slightly different size
     */
    private final Transform transform;
    /**
     * An integer indicating the health the entity had when created, which is by design the maximum
     */
    private final int maxHealth;
    /**
     * A boolean which indicated if the entity is friendly or not.
     * This allows to know if the health bar should be green or red
     */
    private final boolean isFriendly;
    /**
     * The actual number of health points the entity currently has
     */
    private int healthPoints;

    /**
     * The default Health constructor.
     *
     * @param parent       (Positionable): the parent entity to draw the health bar on top of
     * @param transform    (Transform): the transform from the entity
     * @param healthPoints (int): the number of health points the entity starts with. This will be a maximum.
     * @param isFriendly   (boolean): true if the health bar must be green, false otherwise.
     */
    public Health(Positionable parent, Transform transform, int healthPoints, boolean isFriendly) {
        this.parent = parent;
        this.transform = transform;
        this.maxHealth = healthPoints;
        this.healthPoints = healthPoints;
        this.isFriendly = isFriendly;
    }

    /**
     * @return (boolean): true if the entity still has health points, false otherwise.
     */
    @Override
    public boolean isOn() {
        return healthPoints != 0;
    }

    /**
     * @return (boolean): false if the entity still has health points, true otherwise.
     */
    @Override
    public boolean isOff() {
        return !isOn();
    }

    /**
     * @return (float): 1 is the entity still has health points, 0 otherwise.
     */
    @Override
    public float getIntensity() {
        return isOn() ? 1 : 0;
    }

    /**
     * Renders a health bar on the specified canvas.
     *
     * @param canvas target, not null
     */
    @Override
    public void draw(Canvas canvas) {
        // draws the grey outline
        canvas.drawShape(new Polygon(List.of(
                new Vector(0.1f, 0),
                new Vector(0.9f, 0),
                new Vector(0.9f, 0.15f),
                new Vector(0.1f, 0.15f)
        )), parent.getTransform().transformed(transform), Color.GRAY, Color.BLACK, 0.1f, 1, 0);
        // draws the green / red bar
        canvas.drawShape(new Polygon(List.of(
                new Vector(0.1f, 0),
                new Vector(0.8f * healthPoints / maxHealth + 0.1f, 0),
                new Vector(0.8f*  healthPoints / maxHealth + 0.1f, 0.15f),
                new Vector(0.1f, 0.15f)
        )), parent.getTransform().transformed(transform), isFriendly ? Color.GREEN : Color.RED, null, 0, 0.5f, 0);

    }

    /**
     * @param amount (int): the amount of health to restore. Capped by maxHealth.
     */
    public void increase(int amount) {
        healthPoints = Math.min(maxHealth, healthPoints + amount);
    }

    /**
     * @param amount (int): decrease the health points with this amount. The health points are capped at 0.
     */
    public void decrease(int amount) {
        healthPoints = Math.max(0, healthPoints - amount);
    }

    public void resetHealth() {
        healthPoints = maxHealth;
    }
}
