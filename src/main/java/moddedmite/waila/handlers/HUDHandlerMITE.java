package moddedmite.waila.handlers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.cbcore.LangUtil;
import net.minecraft.*;

import java.util.List;

public class HUDHandlerMITE implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();

        if (block == Block.onions) {
            return new ItemStack(Item.onion);
        }
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!(accessor.getTileEntity() instanceof TileEntityAnvil)) return currenttip;

        if (itemStack.getItem() instanceof ItemAnvilBlock ia) {
            NBTTagCompound tag = accessor.getNBTData();
            if (tag == null || !tag.hasKey("WailaAnvilDamage")) return currenttip;

            int maxDurability = ia.getMaxDamage(itemStack);
            int durability = tag.getInteger("WailaAnvilDamage");
            if (durability == maxDurability || durability == 0) return currenttip;
            currenttip.add(LangUtil.translateG(
                            "hud.msg.anvil.durability",
                            maxDurability - durability, maxDurability));
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
        if (te instanceof TileEntityAnvil tea) {
            tag.setInteger("WailaAnvilDamage", tea.damage);
        }
        return tag;
    }

    public static void register() {
        IWailaDataProvider provider = new HUDHandlerMITE();

        ModuleRegistrar.instance().registerStackProvider(provider, Block.class);
        ModuleRegistrar.instance().registerBodyProvider(provider, BlockAnvil.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, BlockAnvil.class);
    }
}
