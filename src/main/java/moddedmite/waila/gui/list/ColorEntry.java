package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.layer.ColorEditLayer;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.gui.screen.util.ColorBoard;
import fi.dy.masa.malilib.gui.widgets.WidgetTextFieldColor;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import net.minecraft.GuiScreen;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** ARGB text input and clickable color-board row. */
public class ColorEntry extends OptionEntry {
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?:0x|#)([a-fA-F0-9]{1,8})");
    private static final int TEXT_COLOR_NORMAL = 0xE0E0E0;
    private static final int TEXT_COLOR_INVALID = 0xFF5555;

    private final ConfigColor colorConfig;
    private final WidgetTextFieldColor textField;
    private final ColorBoard colorBoard;
    private final GuiScreen screen;
    private final Consumer<Supplier<Layer>> layerOpener;
    private boolean wasFocused;

    public ColorEntry(ConfigColor config, GuiScreen screen, Consumer<Supplier<Layer>> layerOpener) {
        super(config);
        this.colorConfig = config;
        this.screen = screen;
        this.layerOpener = layerOpener;
        this.textField = new WidgetTextFieldColor(0, 0, ScreenTheme.WIDGET_WIDTH - 22, 18);
        this.textField.setText(config.getColorString());
        this.colorBoard = new ColorBoard(config, 0, 0, 16, 16);
        this.colorBoard.setHoverStrings(StringUtils.translate("manyLib.gui.comment.clickToSelectColor"));
    }

    @Override
    protected void renderValueWidget(int mouseX, int mouseY, DrawContext context, int valueX, int valueY) {
        this.textField.setTextColor(this.isValidValue() ? TEXT_COLOR_NORMAL : TEXT_COLOR_INVALID);
        this.textField.xPos = valueX;
        this.textField.yPos = valueY + 1;
        this.textField.render(context, mouseX, mouseY, 0);
        this.colorBoard.setPosition(valueX + ScreenTheme.WIDGET_WIDTH - 18, valueY + 2);
        this.colorBoard.render(mouseX, mouseY, false, context);
        boolean focused = this.textField.isFocused();
        if (this.wasFocused && !focused) {
            this.syncFromConfig();
        }
        this.wasFocused = focused;
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (super.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (!this.disabled && this.colorBoard.isMouseOver(mouseX, mouseY)) {
            this.layerOpener.accept(() -> new ColorEditLayer(this.colorConfig, this.screen, this::syncFromConfig));
            return true;
        }
        boolean handled = this.textField.onMouseClicked(mouseX, mouseY, button);
        if (!handled && !this.textField.isFocused()) {
            this.syncFromConfig();
        }
        return handled;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int button) {
        super.onMouseReleased(mouseX, mouseY, button);
        this.textField.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean onCharTyped(char chr, int keyCode) {
        if (this.textField.isFocused()) {
            if (keyCode == 28 || keyCode == 156) {
                this.syncFromConfig();
                this.textField.setFocused(false);
                return true;
            }
            boolean handled = this.textField.charTyped(chr, keyCode);
            // ConfigColor.setValueFromString 解析失败时会退回 0（全透明黑），
            // 半成品文本（如刚输入的 "#"）会把配置写坏，所以只在文本合法时写入。
            if (handled && this.isValidValue()) {
                this.colorConfig.setValueFromString(this.textField.getText());
            }
            return handled;
        }
        return super.onCharTyped(chr, keyCode);
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY, DrawContext context) {
        if (this.colorBoard.isMouseOver(mouseX, mouseY)) {
            this.colorBoard.postRenderHovered(mouseX, mouseY, true, context);
            return;
        }
        super.renderTooltip(mouseX, mouseY, context);
    }

    @Override
    public boolean isValidValue() {
        String value = this.textField.getText();
        if (!COLOR_PATTERN.matcher(value).matches()) {
            return false;
        }
        try {
            Long.parseLong(value.substring(value.startsWith("#") ? 1 : 2), 16);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public void syncFromConfig() {
        this.wasFocused = false;
        this.textField.setText(this.colorConfig.getColorString());
    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);
        this.textField.setEnabled(!disabled);
    }
}
