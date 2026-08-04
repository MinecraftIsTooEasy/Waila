package moddedmite.waila.gui;

import fi.dy.masa.malilib.config.interfaces.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.layer.ColorEditLayer;
import fi.dy.masa.malilib.gui.layer.KeySettingsLayer;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.util.StringUtils;
import mcp.mobius.waila.overlay.OverlayConfig;
import moddedmite.waila.config.EnumTooltipTheme;
import moddedmite.waila.config.WailaConfig;
import moddedmite.waila.gui.list.ButtonEntry;
import moddedmite.waila.gui.list.ColorEntry;
import moddedmite.waila.gui.list.Entry;
import moddedmite.waila.gui.list.KeybindEntry;
import moddedmite.waila.gui.list.OptionEntry;
import moddedmite.waila.gui.list.OptionsList;
import moddedmite.waila.gui.list.SliderEntry;
import moddedmite.waila.gui.list.ToggleEntry;
import net.minecraft.GuiScreen;
import net.minecraft.GuiYesNoMITE;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Settings screen containing all four Waila config sections. */
public class WailaSettingsScreen extends BaseOptionsScreen {
    private static final int RESET_ALL_FLAG = 0x5741494C;

    public WailaSettingsScreen(@Nullable GuiScreen parent, String titleKey) {
        super(parent, titleKey);
        this.saver = () -> WailaConfig.getInstance().save();
        this.canceller = this::rollback;
    }

    @Override
    protected void buildOptions(OptionsList options) {
        Map<ConfigBase<?>, Entry> entries = new IdentityHashMap<>();
        this.addSection(options, entries, "waila.general", WailaConfig.general);
        this.addSection(options, entries, "waila.features", WailaConfig.features);
        this.addSection(options, entries, "waila.screen", WailaConfig.screen);
        this.addSection(options, entries, "waila.keybinding", WailaConfig.keybinding);

        this.linkBooleanChildren(entries, WailaConfig.showTooltip,
                WailaConfig.shiftblock, WailaConfig.shiftents);
        this.linkBooleanChildren(entries, WailaConfig.showEnts,
                WailaConfig.showhp, WailaConfig.showatk, WailaConfig.showarmor,
                WailaConfig.showanimal, WailaConfig.showlivestock, WailaConfig.showzombieconversion,
                WailaConfig.showspiderweb, WailaConfig.showphaseevasions);
        this.linkBooleanChildren(entries, WailaConfig.showcrop, WailaConfig.showcropdetails);

        Entry theme = entries.get(WailaConfig.theme);
        for (ConfigBase<?> childConfig : List.of(
                WailaConfig.bgcolor, WailaConfig.gradient1, WailaConfig.gradient2, WailaConfig.fontcolor)) {
            Entry child = entries.get(childConfig);
            child.parent(theme).disableWhen(() -> WailaConfig.theme.getEnumValue() != EnumTooltipTheme.Custom);
        }

        options.title("waila.danger_zone").titleColor(0xFFFF5555);
        options.add(new ButtonEntry(StringUtils.translate("config.name.waila.reset_all"),
                StringUtils.translate("gui.waila.reset_all.button"), button -> this.confirmResetAll()));
    }

    private void confirmResetAll() {
        GuiYesNoMITE dialog = new GuiYesNoMITE(this,
                StringUtils.translate("gui.waila.reset_all.question"), "",
                StringUtils.translate("gui.yes"), StringUtils.translate("gui.no"), RESET_ALL_FLAG);
        this.mc.displayGuiScreen(dialog);
    }

    @Override
    public void confirmClicked(boolean result, int flag) {
        if (result && flag == RESET_ALL_FLAG) {
            this.resetSection(WailaConfig.general);
            this.resetSection(WailaConfig.features);
            this.resetSection(WailaConfig.screen);
            this.resetSection(WailaConfig.keybinding);
            OverlayConfig.updateColors();
            for (Entry entry : this.options.allEntries()) {
                if (entry instanceof OptionEntry optionEntry) {
                    optionEntry.syncFromConfig();
                    optionEntry.refreshDisabledState();
                }
            }
        }
        this.mc.displayGuiScreen(this);
    }

    private void resetSection(List<? extends ConfigBase> configs) {
        for (ConfigBase config : configs) {
            config.resetToDefault();
        }
    }

    private void linkBooleanChildren(Map<ConfigBase<?>, Entry> entries, ConfigBoolean parentConfig,
                                     ConfigBase<?>... childConfigs) {
        Entry parent = entries.get(parentConfig);
        for (ConfigBase<?> childConfig : childConfigs) {
            entries.get(childConfig).parent(parent).disableWhen(() -> !parentConfig.getBooleanValue());
        }
    }

    private void addSection(OptionsList options, Map<ConfigBase<?>, Entry> entries,
                            String title, List<? extends ConfigBase> configs) {
        options.title(title);
        for (ConfigBase config : configs) {
            Entry entry = this.createEntry(config);
            if (entry != null) {
                options.add(entry);
                entries.put(config, entry);
            }
        }
    }

    private @Nullable Entry createEntry(ConfigBase config) {
        ConfigType type = config.getType();
        if (type == ConfigType.COLOR) {
            return new ColorEntry((ConfigColor) config, this, this::openColorLayer);
        }
        if (type == ConfigType.BOOLEAN || type == ConfigType.ENUM) {
            return new ToggleEntry(config);
        }
        if (type == ConfigType.INTEGER) {
            return new SliderEntry<>((ConfigInteger) config);
        }
        if (type == ConfigType.DOUBLE) {
            return new SliderEntry<>((ConfigDouble) config);
        }
        if (type == ConfigType.HOTKEY) {
            return new KeybindEntry((ConfigHotkey) config, this, this::openKeybindLayer);
        }
        return null;
    }

    private void openColorLayer(Supplier<Layer> factory) {
        this.toggleLayer(layer -> layer instanceof ColorEditLayer, factory);
    }

    private void openKeybindLayer(Supplier<Layer> factory) {
        this.toggleLayer(layer -> layer instanceof KeySettingsLayer, factory);
    }
}
