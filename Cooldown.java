package ch.epfl.cs107.icmaze.actor.util;

/**
 * Cooldown timer utility expected by ICMazeActor.
 */
public final class Cooldown {

    private final float delay;
    private float elapsed;

    public Cooldown(float delaySeconds) {
        this.delay = Math.max(0f, delaySeconds);
        this.elapsed = delay; // ready immediately
    }

    /** Updates the timer and returns true if ready (matches code like: cd.ready(dt)) */
    public boolean ready(float dt) {
        update(dt);
        return isReady();
    }

    /** Update timer */
    public void update(float dt) {
        elapsed += Math.max(0f, dt);
    }

    /** True when cooldown finished */
    public boolean isReady() {
        return elapsed >= delay;
    }

    /** Restart cooldown */
    public void reset() {
        elapsed = 0f;
    }

    /** Force ready now */
    public void setReady() {
        elapsed = delay;
    }
}
