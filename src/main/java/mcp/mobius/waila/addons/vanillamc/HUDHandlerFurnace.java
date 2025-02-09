package mcp.mobius.waila.addons.vanillamc;

import java.util.List;

import moddedmite.waila.network.Packet0x05FurnaceNBTData;
import moddedmite.rustedironcore.network.Network;
import net.minecraft.*;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.server.MinecraftServer;

public class HUDHandlerFurnace implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        if (((TileEntityFurnace) accessor.getTileEntity()).isBurning()) {
            int cookTime = accessor.getNBTData().getShort("CookTime");
            NBTTagList tag = accessor.getNBTData().getTagList("Items");
            ServerPlayer playerEntity = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername("Xy_Lose");
//            ServerPlayer playerEntity = Minecraft.getClientPlayer().getAsEntityPlayerMP();
            String renderStr = "";
            ItemStack input = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.tagAt(0));
            ItemStack fuel = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.tagAt(1));
            ItemStack output = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.tagAt(2));
            Packet0x05FurnaceNBTData packet0x05FurnaceNBTData = new Packet0x05FurnaceNBTData(input, fuel, output, cookTime);
            Network.sendToClient(playerEntity, packet0x05FurnaceNBTData);

            {
                ItemStack input_1 = packet0x05FurnaceNBTData.getInput();
                renderStr += SpecialChars.getRenderString(
                        "waila.stack",
                        "1",
                        input_1.getDisplayName(),
                        String.valueOf(input_1.stackSize),
                        String.valueOf(input_1.getItemSubtype()),
                        String.valueOf(input_1.itemID));
//                PacketDispatcher.sendPacketToAllPlayers(Packet0x02TENBTData.create((NBTTagCompound) tag.tagAt(0)));
            }
            {
                ItemStack fuel_1 = packet0x05FurnaceNBTData.getFuel();
                renderStr += SpecialChars.getRenderString(
                        "waila.stack",
                        "1",
                        fuel_1.getDisplayName(),
                        String.valueOf(fuel_1.stackSize),
                        String.valueOf(fuel_1.getItemSubtype()),
                        String.valueOf(fuel_1.itemID));
//                PacketDispatcher.sendPacketToAllPlayers(Packet0x02TENBTData.create((NBTTagCompound) tag.tagAt(1)));
            }

            renderStr += SpecialChars.getRenderString("waila.progress", String.valueOf(packet0x05FurnaceNBTData.getCookProgress()), String.valueOf(200));

            {
                ItemStack output_1 = packet0x05FurnaceNBTData.getOutput();
                renderStr += SpecialChars.getRenderString(
                        "waila.stack",
                        "1",
                        output_1.getDisplayName(),
                        String.valueOf(output_1.stackSize),
                        String.valueOf(output_1.getItemSubtype()),
                        String.valueOf(output_1.itemID));
//                PacketDispatcher.sendPacketToAllPlayers(Packet0x02TENBTData.create((NBTTagCompound) tag.tagAt(2)));
            }

            currenttip.add(renderStr);
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
        if (te != null) te.writeToNBT(tag);
        return tag;
    }

    public static void register() {}
}
