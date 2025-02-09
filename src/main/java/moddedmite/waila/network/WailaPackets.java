package moddedmite.waila.network;

import moddedmite.rustedironcore.network.PacketReader;
import net.minecraft.ResourceLocation;

public class WailaPackets {
    public static final String CompactID = "waila";
    public static final ResourceLocation FurnaceData = new ResourceLocation(CompactID, "furnace_data");

    public static void registerClientReaders() {
        PacketReader.registerClientPacketReader(FurnaceData, Packet0x05FurnaceNBTData::new);
    }
}
