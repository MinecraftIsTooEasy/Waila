package moddedmite.waila.gui.widget;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.render.RenderUtils;
import moddedmite.waila.gui.util.ScreenTheme;

/** A scrollbar whose state is a continuous pixel offset. */
public class ScrollBarV {
    private int x;
    private int y;
    private int width;
    private int height;
    private float viewportHeight;
    private float contentHeight;
    private float scroll;
    private float thumbHeight;
    private boolean dragging;

    public ScrollBarV(int x, int y, int width, int height) {
        this.setGeometry(x, y, width, height);
    }

    public void setGeometry(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setContent(float viewportHeight, float contentHeight) {
        this.viewportHeight = Math.max(0, viewportHeight);
        this.contentHeight = Math.max(0, contentHeight);
        this.thumbHeight = contentHeight <= 0 ? this.height
                : Math.max(8.0F, Math.min(this.height, this.height * viewportHeight / contentHeight));
        this.setScroll(this.scroll);
    }

    public void setScroll(float scroll) {
        this.scroll = Math.max(0, Math.min(scroll, this.maxScroll()));
    }

    public float getScroll() {
        return this.scroll;
    }

    public boolean isNeeded() {
        return this.contentHeight > this.viewportHeight;
    }

    /** 拖动中时滚动条是滚动位置的权威，调用方不要反向覆盖它。 */
    public boolean isDragging() {
        return this.dragging && this.isNeeded();
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (!this.isNeeded()) {
            this.dragging = false;
            return;
        }
        if (this.dragging) {
            this.jumpTo(mouseY);
        }
        RenderUtils.drawRect(this.x, this.y, this.width, this.height, ScreenTheme.SCROLLBAR_TRACK);
        RenderUtils.drawRect(this.x, this.thumbY(), this.width, Math.round(this.thumbHeight), ScreenTheme.SCROLLBAR_THUMB);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && this.isNeeded() && mouseX >= this.x && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + this.height) {
            this.dragging = true;
            this.jumpTo(mouseY);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        this.dragging = false;
    }

    private float maxScroll() {
        return Math.max(0, this.contentHeight - this.viewportHeight);
    }

    private int thumbY() {
        float travel = this.height - this.thumbHeight;
        float ratio = this.maxScroll() <= 0 ? 0 : this.scroll / this.maxScroll();
        return this.y + Math.round(travel * ratio);
    }

    private void jumpTo(int mouseY) {
        float travel = this.height - this.thumbHeight;
        if (travel <= 0) {
            this.scroll = 0;
        } else {
            float ratio = (mouseY - this.y - this.thumbHeight / 2.0F) / travel;
            this.scroll = Math.max(0, Math.min(1, ratio)) * this.maxScroll();
        }
    }
}
