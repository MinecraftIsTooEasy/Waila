package mcp.mobius.waila;

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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.*;
import net.xiaoyu233.fml.FishModLoader;
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
    private KeyBinding keyBinding;
    public static Logger log = LogManager.getLogger("Waila");
    public boolean serverPresent = false;
    private WailaPacketHandler wailaPacketHandler;
    public static ProxyClient proxy;
    private boolean enableKeybind;

    public String getModName(ItemStack itemStack) {
        if (itemStack != null) {
            return itemStack.getItem().getItemDisplayName(itemStack);
        }
        return "";
    }

    public void load() {
        instance = new Waila();
        proxy = new ProxyClient();
        proxy.registerHandlers();
        initKeybind();
    }

    public void initKeybind() {
        this.enableKeybind = true;
        this.keyBinding = new KeyBinding(StatCollector.translateToLocal("key.waila.hidden"), 35);
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        KeyBinding[] keyBindings = settings.keyBindings;
        KeyBinding[] keyBindings2 = (KeyBinding[]) Arrays.copyOf(keyBindings, keyBindings.length + 1);
        keyBindings2[keyBindings2.length - 1] = this.keyBinding;
        settings.keyBindings = keyBindings2;
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
        log.info("Waila Version 1.0.7 beta Initializing...");
        MITEEvents.MITE_EVENT_BUS.register(new WailaEventFish());
        WailaConfig.init();
        WailaConfig.getInstance().load();
        ModsScreen.getInstance().addConfig(WailaConfig.getInstance());
    }
}
//@Instance("Waila")
//public static Waila instance;
//
//@SidedProxy(clientSide = "mcp.mobius.waila.client.ProxyClient", serverSide = "mcp.mobius.waila.server.ProxyServer")
//public static ProxyServer proxy;
//public static Logger log = LogManager.getLogger("Waila");
//public boolean serverPresent = false;
//private final ArtifactVersion minimumClientJoinVersion = new DefaultArtifactVersion("1.7.3");
//
///* INIT SEQUENCE */
//@EventHandler
//public void preInit(FMLPreInitializationEvent event) {
//    ConfigHandler.instance().loadDefaultConfig(event);
//    OverlayConfig.updateColors();
//    MinecraftForge.EVENT_BUS.register(new DecoratorRenderer());
//    WailaPacketHandler.INSTANCE.ordinal();
//}
//
//@EventHandler
//public void initialize(FMLInitializationEvent event) {
//    try {
//        Field eBus = FMLModContainer.class.getDeclaredField("eventBus");
//        eBus.setAccessible(true);
//        EventBus FMLbus = (EventBus) eBus.get(FMLCommonHandler.instance().findContainerFor(this));
//        FMLbus.register(this);
//    } catch (Throwable ignored) {}
//
//    if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
//        MinecraftForge.EVENT_BUS.register(new DecoratorRenderer());
//        FMLCommonHandler.instance().bus().register(new KeyEvent());
//        FMLCommonHandler.instance().bus().register(WailaTickHandler.instance());
//
//    }
//    FMLCommonHandler.instance().bus().register(new NetworkHandler());
//}
//
//@EventHandler
//public void postInit(FMLPostInitializationEvent event) {
//    proxy.registerHandlers();
//    ModIdentification.init();
//}
//
//@Subscribe
//public void loadComplete(FMLLoadCompleteEvent event) {
//    proxy.registerMods();
//    proxy.registerIMCs();
//}
//
//@EventHandler
//public void processIMC(FMLInterModComms.IMCEvent event) {
//    for (IMCMessage imcMessage : event.getMessages()) {
//        if (!imcMessage.isStringMessage()) continue;
//
//        if (imcMessage.key.equalsIgnoreCase("addconfig")) {
//            String[] params = imcMessage.getStringValue().split("\\$\\$");
//            if (params.length != 3) {
//                Waila.log.warn(
//                        String.format(
//                                "Error while parsing config option from [ %s ] for %s",
//                                imcMessage.getSender(),
//                                imcMessage.getStringValue()));
//                continue;
//            }
//            Waila.log.info(
//                    String.format(
//                            "Receiving config request from [ %s ] for %s",
//                            imcMessage.getSender(),
//                            imcMessage.getStringValue()));
//            ConfigHandler.instance().addConfig(params[0], params[1], params[2]);
//        }
//
//        if (imcMessage.key.equalsIgnoreCase("register")) {
//            Waila.log.info(
//                    String.format(
//                            "Receiving registration request from [ %s ] for method %s",
//                            imcMessage.getSender(),
//                            imcMessage.getStringValue()));
//            ModuleRegistrar.instance().addIMCRequest(imcMessage.getStringValue(), imcMessage.getSender());
//        }
//    }
//}
//
//@EventHandler
//public void serverStarting(FMLServerStartingEvent event) {
//    event.registerServerCommand(new CommandDumpHandlers());
//}
//
///**
// * Block any clients older than 1.7.3 to ensure the vanilla.show_invisible_players property is respected
// */
//@SuppressWarnings("unused")
//@NetworkCheckHandler
//public boolean checkModList(Map<String, String> versions, Side side) {
//    if (side == Side.CLIENT && versions.containsKey("Waila")) {
//        return minimumClientJoinVersion.compareTo(new DefaultArtifactVersion(versions.get("Waila"))) <= 0;
//    }
//    return true;
//
//}
//}
