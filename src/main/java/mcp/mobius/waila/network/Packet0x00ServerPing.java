package mcp.mobius.waila.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.utils.Constants;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.Packet;
import net.minecraft.Packet250CustomPayload;

public class Packet0x00ServerPing {
    public byte header;
    public HashMap<String, Boolean> forcedKeys = new HashMap();

    public Packet0x00ServerPing(Packet250CustomPayload packet) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try {
            this.header = inputStream.readByte();
            this.forcedKeys.clear();

            while(true) {
                String key = Packet.readString(inputStream, 255);
                if (key.equals("END OF LIST")) {
                    break;
                }

                boolean value = inputStream.readBoolean();
                this.forcedKeys.put(key, value);
            }
        } catch (IOException var5) {
        }

    }

    public static Packet250CustomPayload create() {
        Packet250CustomPayload packet = new Packet250CustomPayload();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1);
        DataOutputStream outputStream = new DataOutputStream(bos);

        try {
            outputStream.writeByte(0);
//            ConfigCategory serverForcing = ConfigHandler.instance().config.getCategory(Constants.CATEGORY_SERVER);
//            Iterator i$ = serverForcing.keySet().iterator();
//
//            while(i$.hasNext()) {
//                String key = (String)i$.next();
//                if (WailaConfig.CATEGORY_SERVER.getBooleanValue()) {
//                    Packet.writeString(key, outputStream);
                    outputStream.writeBoolean(false);
//                }
//            }

            Packet.writeString("END OF LIST", outputStream);
        } catch (IOException var6) {
        }

        packet.channel = Waila.NAME;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        return packet;
    }
}
