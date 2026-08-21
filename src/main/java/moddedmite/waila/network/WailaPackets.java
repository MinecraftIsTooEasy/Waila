package moddedmite.waila.network;

//import moddedmite.rustedironcore.network.PacketReader;
import mcp.mobius.waila.Waila;
import net.minecraft.ResourceLocation;

public class WailaPackets {
    public static final String CompactID = Waila.ID;
    public static final ResourceLocation FurnaceData = new ResourceLocation(CompactID, "furnace_data");

    public static void registerClientReaders() {
//        PacketReader.registerClientPacketReader(FurnaceData, Packet0x05FurnaceNBTData::new);
    }
}
