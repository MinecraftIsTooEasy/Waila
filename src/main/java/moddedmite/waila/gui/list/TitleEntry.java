package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.util.TextUtil;

/** A centered section heading, shared by the list and navigation bar. */
public class TitleEntry extends Entry {
    public final String key;
    private int titleColor = ScreenTheme.TITLE_TEXT;

    public TitleEntry(String key) {
        this(key, StringUtils.getTranslatedOrFallback("config.tab." + key, key));
        String comment = StringUtils.getTranslatedOrFallback("config.tab." + key + ".comment", null);
        if (comment != null) {
            this.addDescription(comment);
        }
    }

    public TitleEntry(String key, String literalTitle) {
        super(literalTitle == null ? key : literalTitle);
        this.key = key;
    }

    @Override
    public int getTextX() {
        return (this.contentWidth - this.getTextWidth()) / 2 - ScreenTheme.TEXT_X;
    }

    @Override
    public int getTextY() {
        return 0;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean hovered, DrawContext context) {
        int y = this.contentY + (this.getHeight() - TextUtil.lineHeight()) / 2;
        context.drawTextWithShadow(RenderUtils.fontRenderer(), this.title,
                this.contentX + this.getTextX(), y, this.titleColor);
    }

    public TitleEntry titleColor(int color) {
        this.titleColor = color;
        return this;
    }

    public String getNavigationTitle() {
        return this.rawTitle;
    }
}
