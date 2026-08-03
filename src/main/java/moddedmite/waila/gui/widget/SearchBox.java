package moddedmite.waila.gui.widget;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetTextField;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.function.Consumer;

/** Permanently visible search input with hint text and change notification. */
public class SearchBox extends WidgetBase {
    private final WidgetTextField textField;
    private final Consumer<String> responder;
    private final String hint;

    public SearchBox(int x, int y, int width, int height, Consumer<String> responder) {
        super(x, y, width, height);
        this.responder = responder;
        this.hint = StringUtils.translate("gui.waila.search.hint");
        this.textField = new WidgetTextField(x, y, width, height);
        this.textField.setEnableBackgroundDrawing(true);
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext context) {
        this.textField.render(context, mouseX, mouseY, 0);
        if (this.textField.getText().isEmpty() && !this.textField.isFocused()) {
            context.drawTextWithShadow(RenderUtils.fontRenderer(), this.hint, this.x + 4,
                    this.y + (this.height - this.fontHeight) / 2, 0xFF888888);
        }
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int button) {
        return this.textField.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void onMouseReleasedImpl(int mouseX, int mouseY, int button) {
        this.textField.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean onCharTypedImpl(char chr, int keyCode) {
        String old = this.textField.getText();
        boolean handled = this.textField.charTyped(chr, keyCode);
        String value = this.textField.getText();
        if (!old.equals(value)) {
            this.responder.accept(value);
        }
        return handled;
    }

    @Override
    public void tick() {
        this.textField.updateCursorCounter();
    }

    public String getValue() {
        return this.textField.getText();
    }

    public void setValue(String value) {
        String old = this.textField.getText();
        this.textField.setText(value == null ? "" : value);
        if (!old.equals(this.textField.getText())) {
            this.responder.accept(this.textField.getText());
        }
    }

    public void setFocused(boolean focused) {
        this.textField.setFocused(focused);
    }

    public boolean isFocused() {
        return this.textField.isFocused();
    }
}
