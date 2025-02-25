package moddedmite.xylose.hwite.info;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import net.minecraft.EntityPlayer;
import net.minecraft.RaycastCollision;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public record InfoEntry(BooleanSupplier isActive, InfoSupplier infoSupplier) {
    public static InfoEntry of(ConfigBoolean configBoolean, Function<RaycastCollision, String> infoSupplier) {
        return new InfoEntry(configBoolean::getBooleanValue, context -> infoSupplier.apply(context.rc()));
    }

    public static InfoEntry of(ConfigBoolean configBoolean, BiFunction<RaycastCollision, EntityPlayer, String> infoSupplier) {
        return new InfoEntry(configBoolean::getBooleanValue, context -> infoSupplier.apply(context.rc(), context.player()));
    }

    public static InfoEntry of(Function<RaycastCollision, String> infoSupplier) {
        return new InfoEntry(() -> true, context -> infoSupplier.apply(context.rc()));
    }

    public static InfoEntry of(BiFunction<RaycastCollision, EntityPlayer, String> infoSupplier) {
        return new InfoEntry(() -> true, context -> infoSupplier.apply(context.rc(), context.player()));
    }

    public static InfoEntry of(Supplier<String> infoSuppler) {
        return new InfoEntry(() -> true, context -> infoSuppler.get());
    }

    public void tryAddToList(InfoContext context) {
        String s = this.infoSupplier.get(context);
        if ("".equals(s)) return;
        context.tooltip().add(s);
    }

}
