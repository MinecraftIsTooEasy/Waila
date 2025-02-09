package mcp.mobius.waila;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.gui.screen.ModsScreen;
import mcp.mobius.waila.client.ProxyClient;
import mcp.mobius.waila.network.Packet0x00ServerPing;
import moddedmite.waila.api.PacketDispatcher;
import moddedmite.waila.config.WailaConfig;
import moddedmite.waila.event.WailaEventFish;
import moddedmite.waila.network.WailaPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.*;
import net.xiaoyu233.fml.FishModLoader;
import net.xiaoyu233.fml.ModResourceManager;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.commands.CommandDumpHandlers;
import mcp.mobius.waila.network.NetworkHandler;
import mcp.mobius.waila.network.WailaPacketHandler;
import mcp.mobius.waila.overlay.OverlayConfig;
import mcp.mobius.waila.overlay.WailaTickHandler;
import mcp.mobius.waila.server.ProxyServer;
import mcp.mobius.waila.utils.ModIdentification;

public class Waila implements ModInitializer {
    public static Waila instance;
    public static Logger log = LogManager.getLogger("Waila");
    public boolean serverPresent = false;
    private WailaPacketHandler wailaPacketHandler;
    public static ProxyClient proxy;

    public void load() {
        instance = new Waila();
        proxy = new ProxyClient();
        proxy.registerHandlers();
        proxy.registerMods();
        proxy.registerIMCs();
        OverlayConfig.updateColors();
    }

    @Deprecated
    public boolean serverCustomPacketReceived(NetServerHandler handler, Packet250CustomPayload packet) {
        if (this.wailaPacketHandler == null) {
            this.wailaPacketHandler = new WailaPacketHandler();
        }
        this.wailaPacketHandler.handleCustomPacket(handler, packet);
        return false;
    }

    public void serverPlayerConnectionInitialized(NetServerHandler serverHandler, ServerPlayer playerMP) {
        PacketDispatcher.sendPacketToPlayer(Packet0x00ServerPing.create(), playerMP);
    }

    @Environment(EnvType.CLIENT)
    public boolean interceptCustomClientPacket(Minecraft mc, Packet250CustomPayload packet) {
        if (this.wailaPacketHandler == null) {
            this.wailaPacketHandler = new WailaPacketHandler();
        }
        this.wailaPacketHandler.handleCustomPacket(packet);
        return false;
    }

    public void onInitialize() {
        ModResourceManager.addResourcePackDomain("waila");
        MITEEvents.MITE_EVENT_BUS.register(new WailaEventFish());
        WailaConfig.getInstance().load();
        ConfigManager.getInstance().registerConfig(WailaConfig.getInstance());
        WailaPackets.registerClientReaders();
    }
}
