package moddedmite.waila.gui.util;

/**
 * 平滑追赶值：每 tick 按剩余差值的固定比例逼近目标，用于列表的平滑滚动与标题动画。
 * <p>
 * 移植自 Jade 的 {@code snownee.jade.util.SmoothChasingValue}，纯数学实现，不依赖任何 Minecraft API。
 */
public class SmoothChasingValue {

    /** 差值小于该阈值时直接吸附到目标，避免无限逼近。 */
    private static final float EPS = 1 / 4096f;

    private float speed = 0.4f;
    private float target = 0;

    /** 当前值。渲染方直接读这个字段。 */
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

    /** 是否仍在明显移动中（用于判断是否需要继续重绘/同步）。 */
    public boolean isMoving() {
        return Math.abs(this.getCurrentDiff()) > 1 / 128f;
    }

    /** 只设当前值，不动目标。 */
    public SmoothChasingValue set(float value) {
        this.value = value;
        return this;
    }

    /** 当前值与目标一起设为 value，用于初始化或强制跳转。 */
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

    /**
     * 推进动画。
     *
     * @param partialTicks 距上次调用经过的 tick 数（可为小数）
     */
    public void tick(float partialTicks) {
        float diff = this.getCurrentDiff();
        if (Math.abs(diff) < EPS) {
            this.set(this.target);
            return;
        }
        this.set(this.value + diff * this.speed * partialTicks);
    }
}
