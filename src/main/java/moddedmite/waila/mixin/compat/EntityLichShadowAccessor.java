package moddedmite.waila.mixin.compat;

import net.oilcake.mitelros.entity.mob.EntityLichShadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLichShadow.class)
public interface EntityLichShadowAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}