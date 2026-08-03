package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.interfaces.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import moddedmite.waila.gui.util.ScreenTheme;

import java.util.List;

/** Row with one arbitrary action button. */
public class ButtonEntry extends Entry {
    private final ButtonGeneric button;

    public ButtonEntry(String title, String buttonText, IButtonActionListener listener) {
        super(title);
        this.button = ButtonGeneric.builder(buttonText, listener)
                .size(ScreenTheme.WIDGET_WIDTH, ScreenTheme.WIDGET_HEIGHT).build();
    }

    @Override
    public void render(int mouseX, int mouseY, boolean hovered, DrawContext context) {
        super.render(mouseX, mouseY, hovered, context);
        int x = this.contentX + this.contentWidth - ScreenTheme.VALUE_AREA_WIDTH;
        int y = this.contentY + (ScreenTheme.ROW_HEIGHT - ScreenTheme.WIDGET_HEIGHT) / 2;
        this.button.setPosition(x, y);
        this.button.render(mouseX, mouseY, false, context);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        return this.button.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.button.onMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onCharTyped(char chr, int keyCode) {
        return this.button.onCharTyped(chr, keyCode);
    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);
        this.button.setEnabled(!disabled);
    }
}
