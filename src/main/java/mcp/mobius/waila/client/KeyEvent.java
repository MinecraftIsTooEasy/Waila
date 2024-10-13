package mcp.mobius.waila.client;

import net.minecraft.Minecraft;
import net.minecraft.KeyBinding;

import org.lwjgl.input.Keyboard;

import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.gui.screens.config.ScreenConfig;
import mcp.mobius.waila.utils.Constants;

public class KeyEvent {

    public static KeyBinding key_cfg;
    public static KeyBinding key_show;
    public static KeyBinding key_liquid;
    public static KeyBinding key_recipe;
    public static KeyBinding key_usage;

    public KeyEvent() {
        ClientRegistry
                .registerKeyBinding(key_cfg = new KeyBinding(Constants.BIND_WAILA_CFG, Keyboard.KEY_NUMPAD0, "Waila"));
        ClientRegistry.registerKeyBinding(
                key_show = new KeyBinding(Constants.BIND_WAILA_SHOW, Keyboard.KEY_NUMPAD1, "Waila"));
        ClientRegistry.registerKeyBinding(
                key_liquid = new KeyBinding(Constants.BIND_WAILA_LIQUID, Keyboard.KEY_NUMPAD2, "Waila"));
        ClientRegistry.registerKeyBinding(
                key_recipe = new KeyBinding(Constants.BIND_WAILA_RECIPE, Keyboard.KEY_NUMPAD3, "Waila"));
        ClientRegistry.registerKeyBinding(
                key_usage = new KeyBinding(Constants.BIND_WAILA_USAGE, Keyboard.KEY_NUMPAD4, "Waila"));
    }

    public void onKeyEvent(KeyInputEvent event) {
        boolean showKey = key_show.isPressed();
        if (key_cfg.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ScreenConfig(null));
            }
        } else if (showKey && ConfigHandler.instance()
                .getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_MODE, false)) {
                    boolean status = ConfigHandler.instance()
                            .getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_SHOW, true);
                    ConfigHandler.instance()
                            .setConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_SHOW, !status);
                } else
            if (showKey && !ConfigHandler.instance()
                    .getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_MODE, false)) {
                        ConfigHandler.instance()
                                .setConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_SHOW, true);
                    } else
                if (key_liquid.isPressed()) {
                    boolean status = ConfigHandler.instance()
                            .getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_LIQUID, true);
                    ConfigHandler.instance()
                            .setConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_LIQUID, !status);
                } else if (key_recipe.isPressed()) {
                    if (Loader.isModLoaded("NotEnoughItems")) {
                        try {
                            Class.forName("mcp.mobius.waila.handlers.nei.NEIHandler")
                                    .getDeclaredMethod("openRecipeGUI", boolean.class).invoke(null, true);
                        } catch (Exception ignored) {}
                    }
                } else if (key_usage.isPressed()) {
                    if (Loader.isModLoaded("NotEnoughItems")) {
                        try {
                            Class.forName("mcp.mobius.waila.handlers.nei.NEIHandler")
                                    .getDeclaredMethod("openRecipeGUI", boolean.class).invoke(null, false);
                        } catch (Exception ignored) {}
                    }
                }
    }

}
