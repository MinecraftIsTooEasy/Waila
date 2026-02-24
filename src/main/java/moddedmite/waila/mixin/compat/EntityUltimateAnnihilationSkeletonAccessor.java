package moddedmite.waila.mixin.compat;

import net.moddedmite.mitemod.bex.entity.EntityUltimateAnnihilationSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityUltimateAnnihilationSkeleton.class)
public interface EntityUltimateAnnihilationSkeletonAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}