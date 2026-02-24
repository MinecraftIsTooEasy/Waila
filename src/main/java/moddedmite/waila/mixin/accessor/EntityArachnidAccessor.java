package moddedmite.waila.mixin.accessor;

import net.minecraft.EntityArachnid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityArachnid.class)
public interface EntityArachnidAccessor {
    @Accessor("num_webs")
    int getNumWebs();
}
