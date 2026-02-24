package moddedmite.waila.mixin.client;

import mcp.mobius.waila.Waila;
import net.minecraft.Minecraft;
import net.minecraft.NetClientHandler;
import net.minecraft.Packet250CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetClientHandler.class)
public class NetClientHandlerMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void onHandleCustomPayload(Packet250CustomPayload packet, CallbackInfo ci) {
        if (Waila.instance != null) {
            Waila.instance.interceptCustomClientPacket(Minecraft.getMinecraft(), packet);
        }
    }
}