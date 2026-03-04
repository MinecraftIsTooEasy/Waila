package mcp.mobius.waila.network;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaExceptionHandler;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import moddedmite.waila.api.PacketDispatcher;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WailaPacketHandler {
    public void handleCustomPacket(Packet250CustomPayload packet) {

        if (packet.channel.equals("Waila"))
        {
            try
            {
                byte header = getHeader(packet);

                if (header == 0)
                {
                    Waila.log.info("Received server authentication msg. Remote sync will be activated");
                    Waila.instance.serverPresent = true;
                }
                else if (header == 2)
                {
                    Packet0x02TENBTData castedPacket = new Packet0x02TENBTData(packet);
                    DataAccessorCommon.instance.remoteNbt = castedPacket.tag;
                }
                else if (header == 4)
                {
                    Packet0x04EntNBTData castedPacket = new Packet0x04EntNBTData(packet);
                    DataAccessorCommon.instance.remoteNbt = castedPacket.tag;
                }
            }
            catch (Exception e) {}
        }
    }

    public void handleCustomPacket(NetServerHandler handler, Packet250CustomPayload packet) {

        if (packet.channel.equals("Waila"))
        {
            try {
                byte header = getHeader(packet);

                if (header == 1)
                {
                    Packet0x01TERequest castedPacket = new Packet0x01TERequest(packet);
                    MinecraftServer server = MinecraftServer.getServer();
                    World teWorld = null;

                    for (World w : server.worldServers)
                    {
                        if (w != null && w.provider.dimensionId == castedPacket.worldID)
                        {
                            teWorld = w;
                            break;
                        }
                    }

                    if (teWorld == null) return;

                    TileEntity entity = teWorld.getBlockTileEntity(castedPacket.posX, castedPacket.posY, castedPacket.posZ);

                    if (entity == null) return;

                    if (entity instanceof TileEntitySkull) return;

                    Block teBlock = teWorld.getBlock(castedPacket.posX, castedPacket.posY, castedPacket.posZ);

                    if (ModuleRegistrar.instance().hasNBTProviders(entity) || ModuleRegistrar.instance().hasNBTProviders(teBlock))
                    {
                        try
                        {
                            NBTTagCompound tag = new NBTTagCompound();
                            tag.setInteger("WailaX", castedPacket.posX);
                            tag.setInteger("WailaY", castedPacket.posY);
                            tag.setInteger("WailaZ", castedPacket.posZ);

                            for (Map.Entry<Integer, List<IWailaDataProvider>> entry : ModuleRegistrar.instance().getNBTProviders(teBlock).entrySet())
                            {
                                for (IWailaDataProvider provider : entry.getValue())
                                {
                                    tag = provider.getNBTData(handler.playerEntity, entity, tag, teWorld, castedPacket.posX, castedPacket.posY, castedPacket.posZ);
                                }
                            }

                            for (Map.Entry<Integer, List<IWailaDataProvider>> entry : ModuleRegistrar.instance().getNBTProviders(entity).entrySet())
                            {
                                for (IWailaDataProvider provider : entry.getValue())
                                {
                                    tag = provider.getNBTData(handler.playerEntity, entity, tag, teWorld, castedPacket.posX, castedPacket.posY, castedPacket.posZ);
                                }
                            }

                            PacketDispatcher.sendPacketToPlayer(Packet0x02TENBTData.create(tag), handler.playerEntity);
                        }
                        catch (Throwable e)
                        {
                            WailaExceptionHandler.handleErr(e, entity.getClass().toString(), null);
                        }
                    }
                }
                else if (header == 3)
                {
                    Packet0x03EntRequest castedPacket = new Packet0x03EntRequest(packet);
                    MinecraftServer server = MinecraftServer.getServer();
                    World world = null;

                    for (World w : server.worldServers)
                    {
                        if (w != null && w.provider.dimensionId == castedPacket.worldID)
                        {
                            world = w;
                            break;
                        }
                    }

                    if (world == null) return;

                    Entity entity = world.getEntityByID(castedPacket.id);

                    if (entity == null) return;

                    if (ModuleRegistrar.instance().hasNBTEntityProviders(entity))
                    {
                        NBTTagCompound tag = new NBTTagCompound();

                        for (Map.Entry<Integer, List<IWailaEntityProvider>> entry :
                                ModuleRegistrar.instance().getNBTEntityProviders(entity).entrySet())
                        {
                            for (IWailaEntityProvider provider : entry.getValue())
                            {
                                tag = provider.getNBTData(handler.playerEntity, entity, tag, world);
                            }
                        }
                        PacketDispatcher.sendPacketToPlayer(Packet0x04EntNBTData.create(tag), handler.playerEntity);
                    }
                }
            }
            catch (Exception e2) {}
        }
    }

    public byte getHeader(Packet250CustomPayload packet) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            return inputStream.readByte();
        } catch (IOException e) {
            return (byte) -1;
        }
    }
}