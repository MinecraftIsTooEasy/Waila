package mcp.mobius.waila.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import mcp.mobius.waila.Waila;
import net.minecraft.Entity;
import net.minecraft.Packet;
import net.minecraft.Packet250CustomPayload;
import net.minecraft.World;

public class Packet0x03EntRequest {
    public byte header;
    public int worldID;
    public int id;
    public HashSet<String> keys = new HashSet();

    public Packet0x03EntRequest(Packet250CustomPayload packet) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        this.keys.clear();

        try {
            this.header = inputStream.readByte();
            this.worldID = inputStream.readInt();
            this.id = inputStream.readInt();
            int nkeys = inputStream.readInt();

            for(int i = 0; i < nkeys; ++i) {
                this.keys.add(Packet.readString(inputStream, 250));
            }
        } catch (IOException var5) {
        }

    }

    public static Packet250CustomPayload create(World world, Entity ent, HashSet<String> keys) {
        Packet250CustomPayload packet = new Packet250CustomPayload();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(17);
        DataOutputStream outputStream = new DataOutputStream(bos);

        try {
            outputStream.writeByte(3);
            outputStream.writeInt(world.provider.dimensionId);
            outputStream.writeInt(ent.entityId);
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
