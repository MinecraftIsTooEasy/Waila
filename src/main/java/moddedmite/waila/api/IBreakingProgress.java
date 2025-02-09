package moddedmite.waila.api;

public interface IBreakingProgress {
    default float getCurrentBreakingProgress() {
        return 0.0f;
    }
}
