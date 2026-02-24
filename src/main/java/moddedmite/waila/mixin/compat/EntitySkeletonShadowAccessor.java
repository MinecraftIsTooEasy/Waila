package moddedmite.waila.mixin.compat;

import net.moddedmite.mitemod.bex.entity.EntitySkeletonShadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySkeletonShadow.class)
public interface EntitySkeletonShadowAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}