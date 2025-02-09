package mcp.mobius.waila.overlay;

import static mcp.mobius.waila.api.SpecialChars.ITALIC;

import java.util.List;

import moddedmite.waila.config.WailaConfig;
import net.minecraft.Minecraft;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import net.minecraft.World;

import org.lwjgl.input.Keyboard;

import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.api.impl.MetaDataProvider;
import mcp.mobius.waila.api.impl.TipList;
import mcp.mobius.waila.cbcore.Layout;
import mcp.mobius.waila.utils.Constants;

public class WailaTickHandler {

    public Tooltip tooltip = null;
    public MetaDataProvider handler = new MetaDataProvider();
    private final Minecraft mc = Minecraft.getMinecraft();

    private static WailaTickHandler _instance;

    private WailaTickHandler() {}

    public static WailaTickHandler instance() {
        if (_instance == null) _instance = new WailaTickHandler();
        return _instance;
    }

    public void tickClient() {

//        if (!Keyboard.isKeyDown(KeyEvent.key_show.getKeyCode())
//                && !ConfigHandler.instance().getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_MODE, false)
//                && ConfigHandler.instance()
//                        .getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_SHOW, false)) {
//            ConfigHandler.instance().setConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_SHOW, false);
//        }

        World world = mc.theWorld;
        EntityPlayer player = mc.thePlayer;
        if (world != null && player != null) {
            RaycastCollision target = RayTracing.instance().getTarget();
            RayTracing.instance().fire();

            List<String> currenttip;
            List<String> currenttipHead;
            List<String> currenttipBody;
            List<String> currenttipTail;
            if (target != null && target.isBlock()) {
                DataAccessorCommon accessor = DataAccessorCommon.instance;
                accessor.set(world, player, target);
                ItemStack targetStack = RayTracing.instance().getTargetStack(); // Here we get either the proper stack
                                                                                // or the override

                if (targetStack != null) {
                    currenttip = new TipList<String, String>();
                    currenttipHead = new TipList<String, String>();
                    currenttipBody = new TipList<String, String>();
                    currenttipTail = new TipList<String, String>();

                    currenttipHead = handler.handleBlockTextData(
                            targetStack,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipHead,
                            Layout.HEADER);
                    currenttipBody = handler.handleBlockTextData(
                            targetStack,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipBody,
                            Layout.BODY);
                    currenttipTail = handler.handleBlockTextData(
                            targetStack,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipTail,
                            Layout.FOOTER);

                    if (WailaConfig.shiftblock.getBooleanValue()
                            && !currenttipBody.isEmpty()
                            && !accessor.getPlayer().isSneaking()) {
                        currenttipBody.clear();
                        currenttipBody.add(ITALIC + "Press shift for more data");
                    }

                    currenttip.addAll(currenttipHead);
                    currenttip.addAll(currenttipBody);
                    currenttip.addAll(currenttipTail);

                    this.tooltip = new Tooltip(currenttip, WailaConfig.icon.getBooleanValue(), targetStack);
                }
            } else if (target != null && target.isEntity()) {
                DataAccessorCommon accessor = DataAccessorCommon.instance;
                accessor.set(world, player, target);

                Entity targetEnt = RayTracing.instance().getTargetEntity(); // This need to be replaced by the override
                                                                            // check.

                if (targetEnt != null) {
                    currenttip = new TipList<String, String>();
                    currenttipHead = new TipList<String, String>();
                    currenttipBody = new TipList<String, String>();
                    currenttipTail = new TipList<String, String>();

                    currenttipHead = handler.handleEntityTextData(
                            targetEnt,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipHead,
                            Layout.HEADER);
                    currenttipBody = handler.handleEntityTextData(
                            targetEnt,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipBody,
                            Layout.BODY);
                    currenttipTail = handler.handleEntityTextData(
                            targetEnt,
                            world,
                            player,
                            target,
                            accessor,
                            currenttipTail,
                            Layout.FOOTER);

                    if (WailaConfig.shiftents.getBooleanValue()
                            && !currenttipBody.isEmpty()
                            && !accessor.getPlayer().isSneaking()) {
                        currenttipBody.clear();
                        currenttipBody.add(ITALIC + "Press shift for more data");
                    }

                    currenttip.addAll(currenttipHead);
                    currenttip.addAll(currenttipBody);
                    currenttip.addAll(currenttipTail);

                    this.tooltip = new Tooltip(currenttip, false);
                }
            }
        }

    }
}
