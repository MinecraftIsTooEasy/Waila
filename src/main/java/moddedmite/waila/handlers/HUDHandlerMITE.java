package moddedmite.waila.handlers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.*;

import java.util.List;

//2.0.4更新 添加HUDHandlerAnvil类
import mcp.mobius.waila.handlers.HUDHandlerAnvil;

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

    public static void register() {
        IWailaDataProvider provider = new HUDHandlerMITE();

        ModuleRegistrar.instance().registerStackProvider(provider, onions.getClass());

        //2.0.4更新 添加HUDHandlerAnvil类注册
        HUDHandlerAnvil.register();
    }
}
