package moddedmite.waila.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigTab;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.SimpleConfigs;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.util.JsonUtils;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.overlay.OverlayConfig;
import moddedmite.waila.handlers.emi.EMIHandler;
import net.minecraft.GuiScreen;
import net.minecraft.Minecraft;
import net.xiaoyu233.fml.FishModLoader;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class WailaConfig extends SimpleConfigs implements IWailaConfigHandler {

    public static final ConfigBoolean showTooltip = new ConfigBoolean("choice.showhidewaila", true);
    public static final ConfigBoolean showMods = new ConfigBoolean("choice.showmods", true);
    public static final ConfigBoolean showEnts = new ConfigBoolean("choice.showEnts", true);
    public static final ConfigBoolean metadata = new ConfigBoolean("choice.showhideidmeta", false);
    public static final ConfigBoolean liquid = new ConfigBoolean("choice.showliquids", false);
    public static final ConfigBoolean shiftblock = new ConfigBoolean("choice.shifttoggledblock", false);
    public static final ConfigBoolean shiftents = new ConfigBoolean("choice.shifttoggledents", false);
    public static final ConfigBoolean devMoveDownTooltip = new ConfigBoolean("choice.devMoveDownTooltip", false);

    public static final ConfigBoolean showhp = new ConfigBoolean("option.general.showhp", true);
    public static final ConfigBoolean showatk = new ConfigBoolean("option.general.showatk", true);
    public static final ConfigBoolean showarmor = new ConfigBoolean("option.general.showarmor", true);
    public static final ConfigBoolean showcrop = new ConfigBoolean("option.general.showcrop", true);
    public static final ConfigBoolean spawnertype = new ConfigBoolean("option.vanilla.spawntype", true);
    public static final ConfigBoolean repeater = new ConfigBoolean("option.vanilla.repeater", true);
    public static final ConfigBoolean redstone = new ConfigBoolean("option.vanilla.redstone", true);
    public static final ConfigBoolean comparator = new ConfigBoolean("option.vanilla.comparator", true);
    public static final ConfigBoolean leverstate = new ConfigBoolean("option.vanilla.leverstate", true);
    public static final ConfigBoolean skulltype = new ConfigBoolean("option.vanilla.skulltype", true);

    public static final ConfigInteger posX = new ConfigInteger("screen.label.posX", 50, 0, 100, true, "");
    public static final ConfigInteger posY = new ConfigInteger("screen.label.posY", 1, 0, 100, true, "");
    public static final ConfigInteger alpha = new ConfigInteger("screen.label.alpha", 80, 0, 100, true, "");
    public static final ConfigDouble scale = new ConfigDouble("screen.label.scale", 1.0, 0.2, 2.0, true, "");
    public static final ConfigDouble lerpfactor = new ConfigDouble("screen.label.lerpfactor", 0.3, 0.1, 1.0, true, "");
    public static final ConfigBoolean icon = new ConfigBoolean("screen.label.icon", true);
    public static final ConfigEnum<EnumTooltipTheme> theme = new ConfigEnum<>("screen.label.TooltipTheme", EnumTooltipTheme.Waila);
    public static final ConfigColor bgcolor = new ConfigColor("screen.label.bgcolor", "#FF100010");
    public static final ConfigColor gradient1 = new ConfigColor("screen.label.gradient1", "#FF5000FF");
    public static final ConfigColor gradient2 = new ConfigColor("screen.label.gradient2", "#FF28007F");
    public static final ConfigColor fontcolor = new ConfigColor("screen.label.fontcolor", "#FFA0A0A0");

    public static final ConfigHotkey wailaconfig = new ConfigHotkey("waila.keybind.wailaconfig", Keyboard.KEY_NUMPAD0);
    public static final ConfigHotkey wailadisplay = new ConfigHotkey("waila.keybind.wailadisplay", Keyboard.KEY_NUMPAD1);
    public static final ConfigHotkey keyliquid = new ConfigHotkey("waila.keybind.liquid", Keyboard.KEY_NUMPAD2);
    public static final ConfigHotkey recipe = new ConfigHotkey("waila.keybind.recipe", Keyboard.KEY_NUMPAD3);
    public static final ConfigHotkey usage = new ConfigHotkey("waila.keybind.usage", Keyboard.KEY_NUMPAD4);

    private static WailaConfig Instance;
    public static List<ConfigBase> general;
    public static List<ConfigBase> features;
    public static List<ConfigBase> screen;
    public static List<ConfigHotkey> keybinding;

    public static final List<ConfigTab> tabs = new ArrayList<>();

    public WailaConfig() {
        super("Waila", keybinding, general);
    }

    static {
        general = List.of(showTooltip, showMods, showEnts, metadata, liquid, shiftblock, shiftents, devMoveDownTooltip);
        features = List.of(showhp, showatk, showarmor, showcrop, spawnertype, repeater, redstone, comparator, leverstate, skulltype);
        screen = List.of(posX, posY, alpha, scale, lerpfactor, icon, theme, bgcolor, gradient1, gradient2, fontcolor);
        keybinding = List.of(wailaconfig, wailadisplay, keyliquid, recipe, usage);
        ArrayList<ConfigBase> values = new ArrayList<>();
        values.addAll(features);
        values.addAll(keybinding);
        tabs.add(new ConfigTab("waila.general", general));
        tabs.add(new ConfigTab("waila.features", features));
        tabs.add(new ConfigTab("waila.screen", screen));
        tabs.add(new ConfigTab("waila.keybinding", keybinding));
        Instance = new WailaConfig();

        wailaconfig.getKeybind().setCallback((keyAction, iKeybind) -> {
            Minecraft.getMinecraft().displayGuiScreen(getInstance().getConfigScreen(null));
            return true;
        });
        wailadisplay.getKeybind().setCallback((keyAction, iKeybind) -> {
            showTooltip.toggleBooleanValue();
            return true;
        });
        keyliquid.getKeybind().setCallback((keyAction, iKeybind) -> {
            liquid.toggleBooleanValue();
            return true;
        });

        if (FishModLoader.hasMod("emi")) {
            try {
                recipe.getKeybind().setCallback(((keyAction, iKeybind) -> {
                    EMIHandler.displayRecipes();
                    return true;
                }));
                usage.getKeybind().setCallback(((keyAction, iKeybind) -> {
                    EMIHandler.displayUses();
                    return true;
                }));
            } catch (Exception ignored) {
                Waila.log.warn("You don't have EMI Mod installed");
            }
        }
    }

    @Override
    public List<ConfigTab> getConfigTabs() {
        return tabs;
    }

    public static WailaConfig getInstance() {
        return Instance;
    }

    @Override
    public GuiScreen getConfigScreen(GuiScreen parentScreen) {
        return new WailaConfigScreen(parentScreen, this);
    }

    @Override
    public void save() {
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "general", general);
        ConfigUtils.writeConfigBase(root, "features", features);
        ConfigUtils.writeConfigBase(root, "screen", screen);
        ConfigUtils.writeConfigBase(root, "keybinding", keybinding);
        JsonUtils.writeJsonToFile(root, this.optionsFile);
        OverlayConfig.updateColors();
    }

    @Override
    public void load() {
        if (!this.optionsFile.exists()) {
            this.save();
        } else {
            JsonElement jsonElement = JsonUtils.parseJsonFile(this.optionsFile);
            if (jsonElement != null && jsonElement.isJsonObject()) {
                JsonObject root = jsonElement.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "general", general);
                ConfigUtils.readConfigBase(root, "features", features);
                ConfigUtils.readConfigBase(root, "screen", screen);
                ConfigUtils.readConfigBase(root, "keybinding", keybinding);
            }
        }
    }

    @Override
    public Set<String> getModuleNames() {
        return Set.of();
    }

    @Override
    public HashMap<String, String> getConfigKeys(String modName) {
        return (HashMap<String, String>) wailaconfig.getKeybind();
    }

    @Override
    public boolean getConfig(String key, boolean defvalue) {
        return false;
    }

    @Override
    public boolean getConfig(String key) {
        return false;
    }
}
