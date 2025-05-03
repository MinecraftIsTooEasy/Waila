package mcp.mobius.waila.client;

import mcp.mobius.waila.addons.vanillamc.HUDHandlerFurnace;
import mcp.mobius.waila.overlay.tooltiprenderers.TTRenderArmor;
import net.minecraft.*;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.cbcore.LangUtil;
import mcp.mobius.waila.gui.truetyper.FontLoader;
import mcp.mobius.waila.gui.truetyper.TrueTypeFont;
import mcp.mobius.waila.handlers.HUDHandlerBlocks;
import mcp.mobius.waila.handlers.HUDHandlerEntities;
import mcp.mobius.waila.handlers.VanillaTooltipHandler;
import mcp.mobius.waila.overlay.tooltiprenderers.TTRenderHealth;
import mcp.mobius.waila.overlay.tooltiprenderers.TTRenderProgressBar;
import mcp.mobius.waila.overlay.tooltiprenderers.TTRenderStack;
import mcp.mobius.waila.server.ProxyServer;
import net.xiaoyu233.fml.FishModLoader;

public class ProxyClient extends ProxyServer {

    TrueTypeFont minecraftiaFont;

    public ProxyClient() {}

    @Override
    public void registerHandlers() {
//        if (FishModLoader.hasMod("emi")) {
//            try {
//                Class.forName("moddedmite.waila.handlers.emi.EMIHandler").getDeclaredMethod("register").invoke(null);
//            } catch (Exception e) {
//                Waila.log.error("Failed to hook into NEI properly. Reverting to Vanilla tooltip handler");
////                MinecraftForge.EVENT_BUS.register(new VanillaTooltipHandler());
//            }
//        } else {
////            MinecraftForge.EVENT_BUS.register(new VanillaTooltipHandler());
//        }

        ModuleRegistrar.instance().registerHeadProvider(new HUDHandlerBlocks(), Block.class);
        ModuleRegistrar.instance().registerTailProvider(new HUDHandlerBlocks(), Block.class);

        ModuleRegistrar.instance().registerHeadProvider(new HUDHandlerEntities(), Entity.class);
        ModuleRegistrar.instance().registerBodyProvider(new HUDHandlerEntities(), Entity.class);
        ModuleRegistrar.instance().registerTailProvider(new HUDHandlerEntities(), Entity.class);

//        ModuleRegistrar.instance().registerBodyProvider(new HUDHandlerFurnace(), BlockFurnace.class);
//        ModuleRegistrar.instance().registerNBTProvider(new HUDHandlerFurnace(), NBTBase.class);

        ModuleRegistrar.instance().registerTooltipRenderer("waila.health", new TTRenderHealth());
        ModuleRegistrar.instance().registerTooltipRenderer("waila.stack", new TTRenderStack());
        ModuleRegistrar.instance().registerTooltipRenderer("waila.progress", new TTRenderProgressBar());
        ModuleRegistrar.instance().registerTooltipRenderer("waila.armor", new TTRenderArmor());

    }

    @Override
    public Object getFont() {
        if (minecraftiaFont == null)
            minecraftiaFont = FontLoader.createFont(new ResourceLocation("waila", "fonts/Minecraftia.ttf"), 14, true);
        return this.minecraftiaFont;
    }

}
