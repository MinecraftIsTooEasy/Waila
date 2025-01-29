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
import net.minecraft.Minecraft;
import net.xiaoyu233.fml.FishModLoader;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class WailaConfig extends SimpleConfigs implements IWailaConfigHandler {

    public static final ConfigBoolean showTooltip = new ConfigBoolean("choice.showhidewaila", true);
    public static final ConfigBoolean showEnts = new ConfigBoolean("choice.showEnts", true);
    public static final ConfigBoolean metadata = new ConfigBoolean("choice.showhideidmeta", false);
    public static final ConfigBoolean liquid = new ConfigBoolean("choice.showliquids", false);
    public static final ConfigBoolean shiftblock = new ConfigBoolean("choice.shifttoggledblock", false);
    public static final ConfigBoolean shiftents = new ConfigBoolean("choice.shifttoggledents", false);

    public static final ConfigBoolean showhp = new ConfigBoolean("option.general.showhp", true);
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
    public static final ConfigColor bgcolor = new ConfigColor("screen.label.bgcolor", "#FF100010");
    public static final ConfigColor gradient1 = new ConfigColor("screen.label.gradient1", "#FF5000FF");
    public static final ConfigColor gradient2 = new ConfigColor("screen.label.gradient2", "#FF28007F");
    public static final ConfigColor fontcolor = new ConfigColor("screen.label.fontcolor", "#FFFFFFFF");
    public static final ConfigDouble scale = new ConfigDouble("screen.label.scale", 1, 0.2, 2, true, "");

    public static final ConfigHotkey wailaconfig = new ConfigHotkey("waila.keybind.wailaconfig", Keyboard.KEY_NUMPAD0);
    public static final ConfigHotkey wailadisplay = new ConfigHotkey("waila.keybind.wailadisplay", Keyboard.KEY_NUMPAD1);
    public static final ConfigHotkey keyliquid = new ConfigHotkey("waila.keybind.liquid", Keyboard.KEY_NUMPAD2);
    public static final ConfigHotkey recipe = new ConfigHotkey("waila.keybind.recipe", Keyboard.KEY_NUMPAD3);
    public static final ConfigHotkey usage = new ConfigHotkey("waila.keybind.usage", Keyboard.KEY_NUMPAD4);

    private static WailaConfig Instance;
    public static List<ConfigBase> main;
    public static List<ConfigBase> general;
    public static List<ConfigBase> screen;
    public static List<ConfigHotkey> key;

    public static final List<ConfigTab> tabs = new ArrayList<>();

    public WailaConfig() {
        super("Waila", key, main);
    }

    static {
        main = List.of(showTooltip, showEnts, metadata, liquid, shiftblock, shiftents);
        general = List.of(showhp, showcrop, spawnertype, repeater, redstone, comparator, leverstate, skulltype);
        screen = List.of(posX, posY, alpha, bgcolor, gradient1, gradient2, fontcolor, scale);
        key = List.of(wailaconfig, wailadisplay, keyliquid, recipe, usage);
        ArrayList<ConfigBase> values = new ArrayList<>();
        values.addAll(general);
        values.addAll(key);
        tabs.add(new ConfigTab("waila.main", main));
        tabs.add(new ConfigTab("waila.general", general));
        tabs.add(new ConfigTab("waila.screen", screen));
        tabs.add(new ConfigTab("waila.key", key));
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
                    dev.emi.emi.api.EmiApi.displayRecipes(Objects.requireNonNull(EMIHandler.updateEmiStack()));
                    return true;
                }));
                usage.getKeybind().setCallback(((keyAction, iKeybind) -> {
                    dev.emi.emi.api.EmiApi.displayUses(Objects.requireNonNull(EMIHandler.updateEmiStack()));
                    return true;
                }));
            } catch (Exception ignored) {
                Waila.log.warn("You don't have EMI mod installed");
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
    public void save() {
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "main", main);
        ConfigUtils.writeConfigBase(root, "general", general);
        ConfigUtils.writeConfigBase(root, "screen", screen);
        ConfigUtils.writeConfigBase(root, "key", key);
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
                ConfigUtils.readConfigBase(root, "main", main);
                ConfigUtils.readConfigBase(root, "general", general);
                ConfigUtils.readConfigBase(root, "screen", screen);
                ConfigUtils.readConfigBase(root, "key", key);
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
