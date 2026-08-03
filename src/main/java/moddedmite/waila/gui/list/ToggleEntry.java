package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.config.interfaces.IConfigPeriodic;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigEnum;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.PeriodicButton;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import net.minecraft.GuiScreen;

import java.util.ArrayList;
import java.util.List;

/** Boolean and enum cycling row. */
public class ToggleEntry extends OptionEntry {
    private final PeriodicButton button;

    public ToggleEntry(ConfigBase<?> config) {
        super(config);
        if (!(config instanceof IConfigPeriodic periodic)) {
            throw new IllegalArgumentException("Config is not periodic: " + config.getName());
        }
        this.button = new PeriodicButton(0, 0, ScreenTheme.WIDGET_WIDTH, ScreenTheme.WIDGET_HEIGHT, periodic);
    }

    @Override
    protected void renderValueWidget(int mouseX, int mouseY, DrawContext context, int valueX, int valueY) {
        this.button.setPosition(valueX, valueY);
        this.button.render(mouseX, mouseY, false, context);
    }

    @Override
    protected List<? extends WidgetBase> valueWidgets() {
        return List.of(this.button);
    }

    @Override
    protected void onValueReset() {
        this.button.updateString();
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY, DrawContext context) {
        if (this.config instanceof ConfigEnum<?> configEnum && this.button.isMouseOver(mouseX, mouseY)
                && GuiScreen.isShiftKeyDown()) {
            List<String> lines = new ArrayList<>();
            lines.add(StringUtils.translate("manyLib.gui.comment.available_values") + ":");
            int current = configEnum.getOrdinal();
            Enum<?>[] values = configEnum.getAllEnumValues();
            for (int i = 0; i < values.length; i++) {
                Enum<?> value = values[i];
                String translated = StringUtils.getTranslatedOrFallback(
                        "config.enum." + this.config.getName() + "." + value.name(), value.name());
                lines.add((i == current ? ScreenTheme.TXT_ACTIVE : "") + translated + ScreenTheme.TXT_RESET);
            }
            RenderUtils.drawTextList(lines, mouseX, mouseY, context);
            return;
        }
        super.renderTooltip(mouseX, mouseY, context);
    }
}
