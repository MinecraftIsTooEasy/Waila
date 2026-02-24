package moddedmite.waila.mixin.accessor;

import net.minecraft.EntityLivestock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLivestock.class)
public interface EntityLivestockAccessor {

    @Accessor("food")
    float getFood();

    @Accessor("water")
    float getWater();

    @Accessor("freedom")
    float getFreedom();
}