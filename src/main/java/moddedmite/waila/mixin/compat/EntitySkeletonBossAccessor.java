package moddedmite.waila.mixin.compat;

import net.moddedmite.mitemod.bex.entity.EntitySkeletonBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySkeletonBoss.class)
public interface EntitySkeletonBossAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}