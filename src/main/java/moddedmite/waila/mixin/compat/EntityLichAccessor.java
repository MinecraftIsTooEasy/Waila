package moddedmite.waila.mixin.compat;

import net.oilcake.mitelros.entity.boss.EntityLich;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLich.class)
public interface EntityLichAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}