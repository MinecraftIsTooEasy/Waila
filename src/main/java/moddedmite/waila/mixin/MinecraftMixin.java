package moddedmite.waila.mixin;

import mcp.mobius.waila.Waila;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique
    private Waila wailaAddon;

    @Inject(method = "startGame", at = @At("TAIL"))
    private void onWorldUnload(CallbackInfo ci) {
        if (this.wailaAddon == null) {
            this.wailaAddon = new Waila();
            this.wailaAddon.load();
        }
    }
}
