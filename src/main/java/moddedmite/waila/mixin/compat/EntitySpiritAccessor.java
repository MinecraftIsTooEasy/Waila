package moddedmite.waila.mixin.compat;

import net.oilcake.mitelros.entity.mob.EntitySpirit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySpirit.class)
public interface EntitySpiritAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}