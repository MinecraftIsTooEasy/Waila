package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.config.interfaces.IConfigDisplay;
import fi.dy.masa.malilib.config.interfaces.IConfigSlideable;
import fi.dy.masa.malilib.config.interfaces.IStringRepresentable;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.ManyLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.SliderButton;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetTextField;
import fi.dy.masa.malilib.gui.widgets.WidgetTextFieldDouble;
import fi.dy.masa.malilib.gui.widgets.WidgetTextFieldInteger;
import moddedmite.waila.gui.util.ScreenTheme;

import java.util.List;

/** Numeric row switchable between slider and text input. */
public class SliderEntry<T extends ConfigBase<T> & IConfigSlideable & IConfigDisplay & IStringRepresentable>
        extends OptionEntry {
    private final T typedConfig;
    private final SliderButton<T> slider;
    private final WidgetTextField textField;
    private ButtonGeneric modeButton;
    private boolean wasFocused;

    public SliderEntry(T config) {
        super(config);
        this.typedConfig = config;
        this.slider = new SliderButton<>(0, 0, ScreenTheme.WIDGET_WIDTH - 20, ScreenTheme.WIDGET_HEIGHT, config);
        this.textField = config instanceof ConfigDouble
                ? new WidgetTextFieldDouble(0, 0, ScreenTheme.WIDGET_WIDTH - 22, 18)
                : new WidgetTextFieldInteger(0, 0, ScreenTheme.WIDGET_WIDTH - 22, 18);
        this.textField.setText(config.getStringValue());
        this.rebuildModeButton();
    }

    private void rebuildModeButton() {
        this.modeButton = ButtonGeneric.builder(this.typedConfig.shouldUseSlider()
                        ? ManyLibIcons.BTN_TXTFIELD : ManyLibIcons.BTN_SLIDER,
                button -> this.toggleMode())
                .size(16, 16).renderDefaultBackground(false).build();
        this.modeButton.setEnabled(!this.disabled);
    }

    private void toggleMode() {
        if (this.typedConfig.shouldUseSlider()) {
            this.textField.setText(this.typedConfig.getStringValue());
        } else {
            this.applyAndNormalizeText();
            this.slider.updateString();
            this.slider.updateSliderRatioByConfig();
        }
        this.typedConfig.toggleUseSlider();
        this.rebuildModeButton();
    }

    private void applyAndNormalizeText() {
        this.typedConfig.setValueFromString(this.textField.getText());
        this.textField.setText(this.typedConfig.getStringValue());
    }

    @Override
    protected void renderValueWidget(int mouseX, int mouseY, DrawContext context, int valueX, int valueY) {
        if (this.typedConfig.shouldUseSlider()) {
            this.slider.setPosition(valueX, valueY);
            this.slider.render(mouseX, mouseY, false, context);
        } else {
            this.textField.xPos = valueX;
            this.textField.yPos = valueY + 1;
            this.textField.render(context, mouseX, mouseY, 0);
        }
        this.modeButton.setPosition(valueX + ScreenTheme.WIDGET_WIDTH - 18, valueY + 2);
        this.modeButton.render(mouseX, mouseY, false, context);

        boolean focused = this.textField.isFocused();
        if (this.wasFocused && !focused) {
            this.applyAndNormalizeText();
        }
        this.wasFocused = focused;
    }

    @Override
    protected List<? extends WidgetBase> valueWidgets() {
        return this.typedConfig.shouldUseSlider() ? List.of(this.slider, this.modeButton) : List.of(this.modeButton);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (super.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (!this.typedConfig.shouldUseSlider()) {
            boolean handled = this.textField.onMouseClicked(mouseX, mouseY, button);
            if (!handled && !this.textField.isFocused()) {
                this.applyAndNormalizeText();
            }
            return handled;
        }
        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int button) {
        super.onMouseReleased(mouseX, mouseY, button);
        this.textField.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean onCharTyped(char chr, int keyCode) {
        if (!this.typedConfig.shouldUseSlider() && this.textField.isFocused()) {
            if (keyCode == 28 || keyCode == 156) {
                this.applyAndNormalizeText();
                this.textField.setFocused(false);
                return true;
            }
            boolean handled = this.textField.charTyped(chr, keyCode);
            if (handled) {
                this.typedConfig.setValueFromString(this.textField.getText());
            }
            return handled;
        }
        return super.onCharTyped(chr, keyCode);
    }

    @Override
    protected void onValueReset() {
        this.slider.updateString();
        this.slider.updateSliderRatioByConfig();
        this.textField.setText(this.typedConfig.getStringValue());
    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);
        this.textField.setEnabled(!disabled);
    }
}
