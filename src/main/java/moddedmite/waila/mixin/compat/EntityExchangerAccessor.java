package moddedmite.waila.mixin.compat;

import cn.wensc.mitemod.extreme.entity.EntityExchanger;
import moddedmite.waila.compat.IExchangerEvasions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityExchanger.class)
public interface EntityExchangerAccessor extends IExchangerEvasions {

    @Accessor("num_evasions")
    int getNumEvasions();
}