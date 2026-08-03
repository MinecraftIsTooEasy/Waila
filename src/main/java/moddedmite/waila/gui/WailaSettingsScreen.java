package moddedmite.waila.gui;

import fi.dy.masa.malilib.config.interfaces.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.layer.ColorEditLayer;
import fi.dy.masa.malilib.gui.layer.KeySettingsLayer;
import fi.dy.masa.malilib.gui.layer.Layer;
import moddedmite.waila.config.WailaConfig;
import moddedmite.waila.gui.list.ColorEntry;
import moddedmite.waila.gui.list.Entry;
import moddedmite.waila.gui.list.KeybindEntry;
import moddedmite.waila.gui.list.OptionsList;
import moddedmite.waila.gui.list.SliderEntry;
import moddedmite.waila.gui.list.ToggleEntry;
import net.minecraft.GuiScreen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/** Settings screen containing all four Waila config sections. */
public class WailaSettingsScreen extends BaseOptionsScreen {
    public WailaSettingsScreen(@Nullable GuiScreen parent, String titleKey) {
        super(parent, titleKey);
        this.saver = () -> WailaConfig.getInstance().save();
    }

    @Override
    protected void buildOptions(OptionsList options) {
        this.addSection(options, "waila.general", WailaConfig.general);
        this.addSection(options, "waila.features", WailaConfig.features);
        this.addSection(options, "waila.screen", WailaConfig.screen);
        this.addSection(options, "waila.keybinding", WailaConfig.keybinding);
    }

    private void addSection(OptionsList options, String title, List<? extends ConfigBase> configs) {
        options.title(title);
        for (ConfigBase config : configs) {
            Entry entry = this.createEntry(config);
            if (entry != null) {
                options.add(entry);
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
