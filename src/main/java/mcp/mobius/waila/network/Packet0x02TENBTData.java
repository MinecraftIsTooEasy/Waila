package mcp.mobius.waila.network;

import java.io.*;

import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.*;

public class Packet0x02TENBTData {
    public byte header;
    public NBTTagCompound tag;

    public Packet0x02TENBTData(Packet250CustomPayload packet) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            this.header = inputStream.readByte();
            this.tag = readNBTTagCompound(inputStream);
        } catch (IOException e) {
        }
    }

    public static Packet250CustomPayload create(NBTTagCompound tag) {
        Packet250CustomPayload packet = new Packet250CustomPayload();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(17);
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            outputStream.writeByte(2);
            writeNBTTagCompound(tag, outputStream);
        } catch (IOException e) {
        }
        packet.channel = "Waila";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        return packet;
    }

    public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutputStream par1DataOutputStream) throws IOException {
        if (par0NBTTagCompound == null) {
            par1DataOutputStream.writeShort(-1);
            return;
        }
        byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
        par1DataOutputStream.writeShort((short) abyte.length);
        par1DataOutputStream.write(abyte);
    }

    public static NBTTagCompound readNBTTagCompound(DataInputStream par0DataInputStream) throws IOException {
        int readShort = par0DataInputStream.readShort();
        if (readShort < 0) {
            return null;
        }
        byte[] abyte = new byte[readShort];
        par0DataInputStream.readFully(abyte);
        return CompressedStreamTools.decompress(abyte);
    }
}
