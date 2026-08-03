package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.util.SmoothChasingValue;
import moddedmite.waila.gui.widget.ScrollBarV;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** One scissored, continuously scrolling list containing all option sections. */
public class OptionsList extends WidgetBase {
    private final List<Entry> allEntries = new ArrayList<>();
    private List<Entry> visibleEntries = new ArrayList<>();
    private final SmoothChasingValue scroll = new SmoothChasingValue().withSpeed(ScreenTheme.SCROLL_SPEED);
    public @Nullable TitleEntry currentTitle;
    private @Nullable Entry hovered;
    private final ScrollBarV scrollBar;
    private @Nullable Entry defaultParent;
    private long lastNano;
    /**
     * 本帧使用的整数滚动偏移。渲染与命中判定必须共用同一个值，
     * 否则平滑动画产生半像素 scroll 时两者取整方式不同，命中会偏一行。
     */
    private int scrollPx;

    public OptionsList(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.scrollBar = new ScrollBarV(x + width - ScreenTheme.SCROLLBAR_WIDTH, y,
                ScreenTheme.SCROLLBAR_WIDTH, height);
    }

    public <T extends Entry> T add(T entry) {
        this.allEntries.add(entry);
        if (entry instanceof TitleEntry) {
            this.defaultParent = entry;
        } else if (this.defaultParent != null) {
            entry.parent(this.defaultParent);
        }
        this.visibleEntries = new ArrayList<>(this.allEntries);
        return entry;
    }

    public TitleEntry title(String key) {
        return this.add(new TitleEntry(key));
    }

    public void updateSearch(String search) {
        String input = search == null ? "" : search.trim();
        if (input.isEmpty()) {
            this.visibleEntries = new ArrayList<>(this.allEntries);
        } else {
            String[] keywords = input.toLowerCase(java.util.Locale.ROOT).split("\\s+");
            Set<Entry> retained = new LinkedHashSet<>();
            for (Entry entry : this.allEntries) {
                if (entry.matches(keywords)) {
                    retained.add(entry);
                    this.walkChildren(entry, retained);
                    Entry ancestor = entry.parent();
                    while (ancestor != null) {
                        retained.add(ancestor);
                        ancestor = ancestor.parent();
                    }
                }
            }
            this.visibleEntries = new ArrayList<>();
            for (Entry entry : this.allEntries) {
                if (retained.contains(entry)) {
                    this.visibleEntries.add(entry);
                }
            }
            if (this.visibleEntries.isEmpty()) {
                String noResults = StringUtils.translate("gui.waila.no_results");
                this.visibleEntries.add(new TitleEntry("no_results", ScreenTheme.TXT_MUTED + noResults));
            }
        }
        this.forceScroll(Math.min(this.scroll.getTarget(), this.maxScroll()));
    }

    private void walkChildren(Entry entry, Set<Entry> retained) {
        for (Entry child : entry.children()) {
            retained.add(child);
            this.walkChildren(child, retained);
        }
    }

    public float maxScroll() {
        return Math.max(0, this.visibleEntries.size() * ScreenTheme.ROW_HEIGHT - this.height);
    }

    public void setScroll(float value) {
        this.scroll.target(this.clampScroll(value));
    }

    public void forceScroll(float value) {
        this.scroll.start(this.clampScroll(value));
    }

    public float getScrollTarget() {
        return this.scroll.getTarget();
    }

    public void scrollToTitle(TitleEntry title) {
        int index = this.visibleEntries.indexOf(title);
        if (index >= 0) {
            this.setScroll(index * ScreenTheme.ROW_HEIGHT);
        }
    }

    public @Nullable Entry getEntryAt(int mouseX, int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return null;
        }
        int index = Math.floorDiv(mouseY - this.y + this.scrollPx, ScreenTheme.ROW_HEIGHT);
        return index >= 0 && index < this.visibleEntries.size() ? this.visibleEntries.get(index) : null;
    }

    /** 行的屏幕 y 坐标。渲染与命中判定共用，保证两者严格自洽。 */
    private int rowTop(int index) {
        return this.y - this.scrollPx + index * ScreenTheme.ROW_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext context) {
        long now = System.nanoTime();
        float deltaTicks = this.lastNano == 0 ? 0 : (now - this.lastNano) / 50_000_000.0F;
        this.lastNano = now;
        this.scroll.tick(Math.max(0, Math.min(5, deltaTicks)));
        this.scroll.value = this.clampScroll(this.scroll.value);
        this.scrollPx = Math.round(this.scroll.value);

        this.hovered = this.getEntryAt(mouseX, mouseY);
        this.updateCurrentTitle();
        int usableWidth = Math.max(0, this.width - ScreenTheme.SCROLLBAR_WIDTH);
        int contentWidth = Math.max(0, Math.min(usableWidth - 4, ScreenTheme.ROW_MAX_WIDTH));
        int contentX = this.x + (usableWidth - contentWidth) / 2;
        int first = Math.max(0, Math.floorDiv(this.scrollPx, ScreenTheme.ROW_HEIGHT));
        int end = Math.min(this.visibleEntries.size(),
                Math.floorDiv(this.scrollPx + this.height, ScreenTheme.ROW_HEIGHT) + 1);

        RenderUtils.startScissor(this.x, this.y, this.width, this.height);
        for (int i = first; i < end; i++) {
            Entry entry = this.visibleEntries.get(i);
            int rowY = this.rowTop(i);
            entry.setGeometry(contentX, rowY, contentWidth);
            if (entry == this.hovered) {
                RenderUtils.drawRect(this.x, rowY, this.width, ScreenTheme.ROW_HEIGHT, ScreenTheme.ROW_HOVER);
            }
            entry.render(mouseX, mouseY, entry == this.hovered, context);
        }
        RenderUtils.endScissor();
        // drawGradientRect 是「边界」语义 (left, top, right, bottom)，
        // 与 drawRect 的「尺寸」语义不同，不能传 width/height。
        RenderUtils.drawGradientRect(this.x, this.y + this.height - 2,
                this.x + this.width, this.y + this.height, 0,
                ScreenTheme.LIST_SEPARATOR_TOP, ScreenTheme.LIST_SEPARATOR_BOTTOM);

        this.scrollBar.setGeometry(this.x + this.width - ScreenTheme.SCROLLBAR_WIDTH, this.y,
                ScreenTheme.SCROLLBAR_WIDTH, this.height);
        this.scrollBar.setContent(this.height, this.visibleEntries.size() * ScreenTheme.ROW_HEIGHT);
        if (this.scrollBar.isDragging()) {
            // 拖动中：滚动条是权威，直接吸附，不走平滑动画。
            this.scrollBar.render(context, mouseX, mouseY);
            this.forceScroll(this.scrollBar.getScroll());
        } else {
            this.scrollBar.setScroll(this.scroll.value);
            this.scrollBar.render(context, mouseX, mouseY);
        }
    }

    private void updateCurrentTitle() {
        Entry reference = this.hovered;
        if (reference == null && !this.visibleEntries.isEmpty()) {
            int index = Math.min(this.visibleEntries.size() - 1,
                    Math.max(0, (int) (this.scroll.value / ScreenTheme.ROW_HEIGHT)));
            reference = this.visibleEntries.get(index);
        }
        if (reference instanceof TitleEntry title) {
            this.currentTitle = title;
        } else if (reference != null && reference.root() instanceof TitleEntry title) {
            this.currentTitle = title;
        } else {
            this.currentTitle = null;
        }
    }

    @Override
    public void postRenderHovered(int mouseX, int mouseY, boolean selected, DrawContext context) {
        if (this.hovered != null && this.isMouseOver(mouseX, mouseY)) {
            this.hovered.renderTooltip(mouseX, mouseY, context);
        }
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (this.scrollBar.mouseClicked(mouseX, mouseY, button)) {
            this.forceScroll(this.scrollBar.getScroll());
            return true;
        }
        if (mouseX >= this.x + this.width - ScreenTheme.SCROLLBAR_WIDTH) {
            return false;
        }
        Entry entry = this.getEntryAt(mouseX, mouseY);
        return entry != null && entry.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void onMouseReleasedImpl(int mouseX, int mouseY, int button) {
        this.scrollBar.mouseReleased();
        if (this.hovered != null) {
            this.hovered.onMouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double amount) {
        if (!this.isMouseOver(mouseX, mouseY) || amount == 0) {
            return false;
        }
        int rows = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
                ? ScreenTheme.SCROLL_ROWS_FAST : ScreenTheme.SCROLL_ROWS;
        this.setScroll(this.scroll.getTarget() - (float) Math.signum(amount) * rows * ScreenTheme.ROW_HEIGHT);
        return true;
    }

    @Override
    protected boolean onCharTypedImpl(char chr, int keyCode) {
        return this.hovered != null && this.hovered.onCharTyped(chr, keyCode);
    }

    @Override
    public void tick() {
        for (Entry entry : this.visibleEntries) {
            entry.tick();
        }
    }

    public void setViewport(int x, int y, int width, int height) {
        this.setPosition(x, y);
        this.setWidth(width);
        this.setHeight(height);
        this.forceScroll(this.scroll.value);
    }

    public List<Entry> getVisibleEntries() {
        return new ArrayList<>(this.visibleEntries);
    }

    private float clampScroll(float value) {
        return Math.max(0, Math.min(value, this.maxScroll()));
    }
}
