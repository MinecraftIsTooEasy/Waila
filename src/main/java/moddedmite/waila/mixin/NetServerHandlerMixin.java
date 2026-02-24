package moddedmite.waila.mixin;

import mcp.mobius.waila.network.WailaPacketHandler;
import net.minecraft.NetServerHandler;
import net.minecraft.Packet250CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetServerHandler.class)
public class NetServerHandlerMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void onHandleCustomPayload(Packet250CustomPayload packet, CallbackInfo ci) {
        new WailaPacketHandler().handleCustomPacket((NetServerHandler) (Object) this, packet);
    }
}