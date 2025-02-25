package moddedmite.xylose.hwite.info;

@FunctionalInterface
public interface InfoSupplier {
    String get(InfoContext context);
}
