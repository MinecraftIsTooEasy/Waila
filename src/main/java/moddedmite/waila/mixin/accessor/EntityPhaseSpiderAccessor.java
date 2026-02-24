package moddedmite.waila.mixin.accessor;

import net.minecraft.EntityPhaseSpider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPhaseSpider.class)
public interface EntityPhaseSpiderAccessor {

    @Accessor("num_evasions")
    int getNumEvasions();
}