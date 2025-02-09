package moddedmite.waila.mixin;

import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.WailaTickHandler;
import net.minecraft.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class GuiInGameMixin {
    @Inject(method = "renderGameOverlay(FZII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;inDevMode()Z"))
    private void renderWailaOverlay(float par1, boolean par2, int par3, int par4, CallbackInfo ci) {
        DataAccessorCommon.instance = new DataAccessorCommon();
        WailaTickHandler.instance().tickClient();
        OverlayRenderer overlayRenderer = new OverlayRenderer();
        overlayRenderer.renderOverlay();
    }
}
