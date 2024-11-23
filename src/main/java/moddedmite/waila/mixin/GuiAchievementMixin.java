package moddedmite.waila.mixin;

import mcp.mobius.waila.Waila;
import net.minecraft.GuiAchievement;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiAchievement.class})
public abstract class GuiAchievementMixin {

    @Inject(method = {"updateAchievementWindow"}, at = {@At("RETURN")})
    public void updateAchievementWindow(CallbackInfo info) {

//        if (Minecraft.getMinecraft().theWorld != null) {
//            this.wailaAddon.checkKeybind();
//        }
    }
}

