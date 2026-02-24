package moddedmite.waila.handlers.emi;

import mcp.mobius.waila.api.impl.DataAccessorCommon;
import net.minecraft.ItemStack;

import java.util.Objects;

public class EMIHandler {
    static DataAccessorCommon accessor = DataAccessorCommon.instance;

    public static dev.emi.emi.api.stack.EmiStack updateEmiStack() {
        return dev.emi.emi.api.stack.EmiStack.of(new ItemStack(accessor.block));
    }

    public static void displayRecipes() {
        if (accessor != null && accessor.getBlock() != null) {
            dev.emi.emi.api.EmiApi.displayRecipes(Objects.requireNonNull(EMIHandler.updateEmiStack()));
        }
    }

    public static void displayUses() {
        if (accessor != null && accessor.getBlock() != null) {
            dev.emi.emi.api.EmiApi.displayUses(Objects.requireNonNull(EMIHandler.updateEmiStack()));
        }
    }
}
