package moddedmite.waila.gui;

import fi.dy.masa.malilib.config.interfaces.IStringRepresentable;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.render.RenderUtils;
import moddedmite.waila.config.WailaConfig;
import moddedmite.waila.gui.list.Entry;
import moddedmite.waila.gui.list.OptionsList;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.util.TextUtil;
import net.minecraft.GuiScreen;

import javax.annotation.Nullable;

/** P1 settings screen: all current configs are rows with read-only value placeholders. */
public class WailaSettingsScreen extends BaseOptionsScreen {
    public WailaSettingsScreen(@Nullable GuiScreen parent, String titleKey) {
        super(parent, titleKey);
        this.saver = () -> WailaConfig.getInstance().save();
    }

    @Override
    protected void buildOptions(OptionsList options) {
        options.title("waila.general");
        for (ConfigBase config : WailaConfig.general) {
            options.add(this.makePlaceholder(config));
        }
        options.title("waila.features");
        for (ConfigBase config : WailaConfig.features) {
            options.add(this.makePlaceholder(config));
        }
        options.title("waila.screen");
        for (ConfigBase config : WailaConfig.screen) {
            options.add(this.makePlaceholder(config));
        }
        options.title("waila.keybinding");
        for (ConfigHotkey config : WailaConfig.keybinding) {
            options.add(this.makePlaceholder(config));
        }
    }

    private Entry makePlaceholder(ConfigBase config) {
        String value = config instanceof IStringRepresentable representable
                ? representable.getStringValue() : config.getType().name();
        Entry entry = new PlaceholderEntry(config.getConfigGuiDisplayName(), value);
        String comment = config.getConfigGuiDisplayComment();
        if (comment != null) {
            entry.addDescription(comment);
        }
        return entry;
    }

    private static class PlaceholderEntry extends Entry {
        private final String value;

        private PlaceholderEntry(String title, String value) {
            super(title);
            this.value = value;
        }

        @Override
        public void render(int mouseX, int mouseY, boolean hovered, DrawContext context) {
            super.render(mouseX, mouseY, hovered, context);
            String shown = TextUtil.clamp(ScreenTheme.TXT_MUTED + this.value, ScreenTheme.WIDGET_WIDTH);
            int x = this.contentX + this.contentWidth - ScreenTheme.VALUE_AREA_WIDTH;
            int y = this.contentY + (this.getHeight() - TextUtil.lineHeight()) / 2 + this.getTextY();
            context.drawTextWithShadow(RenderUtils.fontRenderer(), shown, x, y, ScreenTheme.LABEL_TEXT_DISABLED);
        }
    }
}
