package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ConfigVariables;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.util.TextUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

/** A lightweight, continuously repositioned row in the options list. */
public class Entry {
    protected String title;
    protected final String rawTitle;
    protected final List<String> searchKeywords = new ArrayList<>();
    protected final List<String> description = new ArrayList<>();
    protected @Nullable Entry parent;
    protected @Nullable BooleanSupplier disableWhen;
    protected final List<Entry> children = new ArrayList<>();
    protected int indent;
    protected boolean disabled;
    protected int contentX;
    protected int contentY;
    protected int contentWidth;

    public Entry(String title) {
        this.title = title == null ? "" : title;
        this.rawTitle = this.title;
        this.addKeyword(this.title);
    }

    public Entry parent(Entry parent) {
        if (this.parent == parent) {
            return this;
        }
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
        this.indent = parent instanceof OptionEntry ? parent.indent + ScreenTheme.INDENT_STEP : 0;
        if (parent != null && !parent.children.contains(this)) {
            parent.children.add(this);
        }
        return this;
    }

    public Entry disableWhen(BooleanSupplier disableWhen) {
        this.disableWhen = disableWhen;
        this.refreshDisabledState();
        return this;
    }

    public void refreshDisabledState() {
        if (this.disableWhen != null) {
            boolean shouldDisable = this.disableWhen.getAsBoolean();
            if (this.disabled != shouldDisable) {
                this.setDisabled(shouldDisable);
            }
        }
    }

    public Entry root() {
        Entry result = this;
        while (result.parent != null) {
            result = result.parent;
        }
        return result;
    }

    public Entry addKeyword(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            this.searchKeywords.add(TextUtil.stripColor(keyword).toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public Entry addDescription(String line) {
        if (line != null && !line.isEmpty()) {
            this.description.addAll(TextUtil.wrap(line, ScreenTheme.TOOLTIP_WRAP_WIDTH));
            this.addKeyword(line);
        }
        return this;
    }

    public int getHeight() {
        return ScreenTheme.ROW_HEIGHT;
    }

    public int getTextX() {
        return this.indent + ScreenTheme.TEXT_X;
    }

    public int getTextY() {
        return ScreenTheme.TEXT_Y;
    }

    public int getTextWidth() {
        return TextUtil.width(this.title);
    }

    public void setDisabled(boolean disabled) {
        if (this.disabled == disabled) {
            return;
        }
        this.disabled = disabled;
        this.title = disabled ? ScreenTheme.TXT_MUTED + TextUtil.stripColor(this.rawTitle) : this.rawTitle;
    }

    public void setGeometry(int contentX, int contentY, int contentWidth) {
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentWidth = contentWidth;
    }

    public void render(int mouseX, int mouseY, boolean hovered, DrawContext context) {
        int y = this.contentY + (this.getHeight() - TextUtil.lineHeight()) / 2;
        int color = this.disabled ? ScreenTheme.LABEL_TEXT_DISABLED : ScreenTheme.LABEL_TEXT;
        context.drawTextWithShadow(RenderUtils.fontRenderer(), this.title, this.contentX + this.getTextX(), y, color);
    }

    public void renderTooltip(int mouseX, int mouseY, DrawContext context) {
        int left = this.contentX + this.getTextX();
        if (!this.description.isEmpty() && mouseX >= left && mouseX < left + this.getTextWidth()) {
            List<String> resolved = new ArrayList<>(this.description.size());
            for (String line : this.description) {
                resolved.add(ConfigVariables.replaceKeybindVariables(line));
            }
            RenderUtils.drawHoverText(mouseX, mouseY, resolved, context);
        }
    }

    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    public void onMouseReleased(int mouseX, int mouseY, int button) {
    }

    public boolean onCharTyped(char chr, int keyCode) {
        return false;
    }

    public void tick() {
    }

    public boolean matches(String[] keywords) {
        for (String keyword : keywords) {
            boolean matched = false;
            for (String candidate : this.searchKeywords) {
                if (StringUtils.stringMatchesInput(candidate, keyword)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    List<Entry> children() {
        return this.children;
    }

    @Nullable Entry parent() {
        return this.parent;
    }
}
