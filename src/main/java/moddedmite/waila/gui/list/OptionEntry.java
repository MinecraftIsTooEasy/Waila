package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ResetButton;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;

import java.util.List;

/** Base row for a single mutable ManyLib config value. */
public abstract class OptionEntry extends Entry {
    protected final ConfigBase<?> config;
    protected final ResetButton resetButton;

    protected OptionEntry(ConfigBase<?> config) {
        super(config.getConfigGuiDisplayName());
        this.config = config;
        String comment = config.getConfigGuiDisplayComment();
        if (comment != null) {
            this.addDescription(comment);
        }
        String extraMessage = StringUtils.getTranslatedOrFallback(
                "config.name." + config.getName() + ".extra_msg", null);
        if (extraMessage != null && !extraMessage.isEmpty()) {
            this.addKeyword(extraMessage);
        }
        this.resetButton = new ResetButton(0, 0, () -> !this.disabled && config.isModified(), button -> {
            config.resetToDefault();
            this.onValueReset();
        });
        // WidgetBase.setHoverStrings 内部会自己 translate，所以直接传语言键。
        this.resetButton.setHoverStrings("gui.waila.reset");
    }

    protected abstract void renderValueWidget(int mouseX, int mouseY, DrawContext context, int valueX, int valueY);

    public ConfigBase<?> getConfig() {
        return this.config;
    }

    public boolean isValidValue() {
        return true;
    }

    public void syncFromConfig() {
    }

    protected void onValueReset() {
        this.syncFromConfig();
    }

    protected List<? extends WidgetBase> valueWidgets() {
        return List.of();
    }

    @Override
    public void render(int mouseX, int mouseY, boolean hovered, DrawContext context) {
        super.render(mouseX, mouseY, hovered, context);
        int valueX = this.contentX + this.contentWidth - ScreenTheme.VALUE_AREA_WIDTH;
        int valueY = this.contentY + (ScreenTheme.ROW_HEIGHT - ScreenTheme.WIDGET_HEIGHT) / 2;
        this.renderValueWidget(mouseX, mouseY, context, valueX, valueY);
        this.resetButton.setPosition(this.contentX + this.contentWidth - ScreenTheme.RESET_BUTTON_SIZE, valueY);
        this.resetButton.render(mouseX, mouseY, false, context);
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY, DrawContext context) {
        if (this.resetButton.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawHoverText(mouseX, mouseY, this.resetButton.getHoverStrings(), context);
            return;
        }
        super.renderTooltip(mouseX, mouseY, context);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (this.resetButton.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (WidgetBase widget : this.valueWidgets()) {
            if (widget.onMouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int button) {
        this.resetButton.onMouseReleased(mouseX, mouseY, button);
        for (WidgetBase widget : this.valueWidgets()) {
            widget.onMouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean onCharTyped(char chr, int keyCode) {
        for (WidgetBase widget : this.valueWidgets()) {
            if (widget.onCharTyped(chr, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);
        // resetButton 的启禁用由构造时的 predicate 逐帧接管（ManyLib ResetButton 自带
        // setOnUpdate），这里不能手动 setEnabled，否则下一帧就被覆盖。
        for (WidgetBase widget : this.valueWidgets()) {
            if (widget instanceof ButtonBase button) {
                button.setEnabled(!disabled);
            }
        }
    }
}
