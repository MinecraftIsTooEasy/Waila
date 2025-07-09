package moddedmite.waila.mixin;

import moddedmite.waila.api.IBreakingProgress;
import net.minecraft.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin implements IBreakingProgress {
    @Shadow
    public float curBlockDamageMP;

    @Override
    public float getCurrentBreakingProgress() {
        return this.curBlockDamageMP;
    }
}
