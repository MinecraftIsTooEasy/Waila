package mcp.mobius.waila.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import mcp.mobius.waila.Waila;
import net.minecraft.*;

public class Packet0x01TERequest {
    public byte header;
    public int worldID;
    public int posX;
    public int posY;
    public int posZ;
    public HashSet<String> keys = new HashSet();

    public Packet0x01TERequest(Packet250CustomPayload packet) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        this.keys.clear();

        try {
            this.header = inputStream.readByte();
            this.worldID = inputStream.readInt();
            this.posX = inputStream.readInt();
            this.posY = inputStream.readInt();
            this.posZ = inputStream.readInt();
            int nkeys = inputStream.readInt();

            for(int i = 0; i < nkeys; ++i) {
                this.keys.add(Packet.readString(inputStream, 250));
            }
        } catch (IOException var5) {
        }

    }

    public static Packet250CustomPayload create(World world, RaycastCollision mop, HashSet<String> keys) {
        Packet250CustomPayload packet = new Packet250CustomPayload();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(17);
        DataOutputStream outputStream = new DataOutputStream(bos);
        keys.add("x");
        keys.add("y");
        keys.add("z");

        try {
            outputStream.writeByte(1);
            outputStream.writeInt(world.provider.dimensionId);
            outputStream.writeInt(mop.block_hit_x);
            outputStream.writeInt(mop.block_hit_y);
            outputStream.writeInt(mop.block_hit_z);
            outputStream.writeInt(keys.size());
            Iterator i$ = keys.iterator();

            while(i$.hasNext()) {
                String key = (String)i$.next();
                Packet.writeString(key, outputStream);
            }
        } catch (IOException var8) {
        }

        packet.channel = Waila.NAME;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        return packet;
    }
}
