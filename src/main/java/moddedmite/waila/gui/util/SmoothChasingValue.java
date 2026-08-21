package moddedmite.waila.gui.util;

public class SmoothChasingValue {

    private static final float EPS = 1 / 4096f;

    private float speed = 0.4f;
    private float target = 0;

    public float value;

    protected float getCurrentDiff() {
        return this.target - this.value;
    }

    public float getTarget() {
        return this.target;
    }

    public float getSpeed() {
        return this.speed;
    }

    public boolean isMoving() {
        return Math.abs(this.getCurrentDiff()) > 1 / 128f;
    }

    public SmoothChasingValue set(float value) {
        this.value = value;
        return this;
    }

    public SmoothChasingValue start(float value) {
        this.value = value;
        return this.target(value);
    }

    public SmoothChasingValue target(float target) {
        this.target = target;
        return this;
    }

    public SmoothChasingValue withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public void tick(float partialTicks) {
        float diff = this.getCurrentDiff();
        if (Math.abs(diff) < EPS) {
            this.set(this.target);
            return;
        }
        this.set(this.value + diff * this.speed * partialTicks);
    }
}
