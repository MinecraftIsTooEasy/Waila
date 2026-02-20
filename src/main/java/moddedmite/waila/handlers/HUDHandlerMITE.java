package moddedmite.waila.handlers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.cbcore.LangUtil;
import net.minecraft.*;

import java.util.List;

import net.minecraft.server.MinecraftServer;

public class HUDHandlerMITE implements IWailaDataProvider {

    static Block onions = Block.onions;

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();

        if (block == onions) {
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
        if (!(accessor.getTileEntity() instanceof TileEntityAnvil tea)) return currenttip;

        if (itemStack.getItem() instanceof ItemAnvilBlock ia) {
            int maxDurability = ia.getMaxDamage(itemStack);
            int durability = getAnvilDurability(accessor.getTileEntity().getWorldObj().getDimensionId(), tea.xCoord, tea.yCoord, tea.zCoord);
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
        return tag;
    }
    
    private int getAnvilDurability(int dimension, int x, int y, int z) {
        List<TileEntity> tes = MinecraftServer.getServer().worldServerForDimension(dimension).loadedTileEntityList;
        
        for (TileEntity te : tes) {
            if (!(te instanceof TileEntityAnvil tea)) continue;
            if (tea.xCoord == x && tea.yCoord == y && tea.zCoord == z) {
                return tea.damage;
            }
        }
        return 0;
    }

    public static void register() {
        IWailaDataProvider provider = new HUDHandlerMITE();

        ModuleRegistrar.instance().registerStackProvider(provider, onions.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, BlockAnvil.class);
    }
}
