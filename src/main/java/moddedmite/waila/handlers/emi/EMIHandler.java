package moddedmite.waila.handlers.emi;

import net.minecraft.Block;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;

import java.util.Objects;

public class EMIHandler {
    static Minecraft mc = Minecraft.getMinecraft();

    public static dev.emi.emi.api.stack.EmiStack updateEmiStack() {
        return dev.emi.emi.api.stack.EmiStack.of(new ItemStack(Block.blocksList[mc.theWorld.getBlockId(mc.objectMouseOver.block_hit_x, mc.objectMouseOver.block_hit_y, mc.objectMouseOver.block_hit_z)]));
    }

    public static void displayRecipes() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.isBlock()) {
            dev.emi.emi.api.EmiApi.displayRecipes(Objects.requireNonNull(EMIHandler.updateEmiStack()));
        }
    }

    public static void displayUses() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.isBlock()) {
            dev.emi.emi.api.EmiApi.displayUses(Objects.requireNonNull(EMIHandler.updateEmiStack()));
        }
    }
}
