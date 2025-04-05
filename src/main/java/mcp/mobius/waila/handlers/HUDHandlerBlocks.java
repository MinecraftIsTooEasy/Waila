package mcp.mobius.waila.handlers;

import java.util.List;

import mcp.mobius.waila.api.SpecialChars;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.*;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.utils.Constants;
import mcp.mobius.waila.utils.ModIdentification;

import static mcp.mobius.waila.api.SpecialChars.*;

public class HUDHandlerBlocks implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

        String name = null;
        try {
            String s = DisplayUtil.itemDisplayNameShort(itemStack);
            if (s != null && !s.endsWith("Unnamed")) name = s;

            if (name != null) currenttip.add(name);
        } catch (Exception ignored) {}

        if (itemStack.getItem() == Item.redstone) {
            int md = accessor.getMetadata();
            String s = "" + md;
            if (s.length() < 2) s = " " + s;
            currenttip.set(currenttip.size() - 1, name + " " + s);
        }

        if (currenttip.isEmpty()) currenttip.add("< Unnamed >");
        else {
            if (WailaConfig.metadata.getBooleanValue()) {
                currenttip.add(
                        String.format(
                                ITALIC + "[%d:%d] | %s",
                                accessor.getBlockID(),
                                accessor.getMetadata(),
                                accessor.getBlockQualifiedName()));
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        if (!WailaConfig.showMods.getBooleanValue()) return currenttip;

        currenttip.add(RENDER + "{Plip}" + RENDER + "{Plop,thisisatest,222,333}");

        String modName = ModIdentification.nameFromStack(itemStack);
        if (modName != null && !modName.isEmpty()) {
            currenttip.add(BLUE + ITALIC + modName);
        }

        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
        return tag;
    }
}
