package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.SoundUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.util.SmoothChasingValue;
import moddedmite.waila.gui.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/** Left-hand section navigation for the single options list. */
public class OptionsNav extends WidgetBase {
    private final OptionsList list;
    private final List<TitleEntry> titles = new ArrayList<>();
    private final SmoothChasingValue scroll = new SmoothChasingValue().withSpeed(ScreenTheme.SCROLL_SPEED);
    private long lastNano;
    /** 本帧的整数滚动偏移，渲染与命中共用，避免半像素差异导致点错行。 */
    private int scrollPx;

    public OptionsNav(OptionsList list, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.list = list;
    }

    public void refresh() {
        this.titles.clear();
        for (Entry entry : this.list.getVisibleEntries()) {
            if (entry instanceof TitleEntry title) {
                this.titles.add(title);
            }
        }
        this.scroll.start(this.clamp(this.scroll.getTarget()));
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext context) {
        long now = System.nanoTime();
        float deltaTicks = this.lastNano == 0 ? 0 : (now - this.lastNano) / 50_000_000.0F;
        this.lastNano = now;
        this.scroll.tick(Math.max(0, Math.min(5, deltaTicks)));
        this.scroll.value = this.clamp(this.scroll.value);
        this.scrollPx = Math.round(this.scroll.value);

        int background = this.mc.theWorld == null ? ScreenTheme.NAV_BACKGROUND : ScreenTheme.NAV_BACKGROUND_INWORLD;
        RenderUtils.drawRect(this.x, this.y, this.width, this.height, background);
        RenderUtils.drawRect(this.x + this.width - 1, this.y, 1, this.height, ScreenTheme.NAV_SEPARATOR);
        int first = Math.max(0, Math.floorDiv(this.scrollPx, ScreenTheme.NAV_ROW_HEIGHT));
        int end = Math.min(this.titles.size(),
                Math.floorDiv(this.scrollPx + this.height, ScreenTheme.NAV_ROW_HEIGHT) + 1);
        // 行高不一定整除面板高，末行可能跨过下边界，所以要裁剪。
        // Layer 是逐个 widget 顺序 render 的，这里的 scissor 与列表的不会嵌套。
        RenderUtils.startScissor(this.x, this.y, this.width, this.height);
        for (int i = first; i < end; i++) {
            TitleEntry title = this.titles.get(i);
            int rowY = this.y - this.scrollPx + i * ScreenTheme.NAV_ROW_HEIGHT;
            if (title == this.list.currentTitle) {
                RenderUtils.drawRect(this.x, rowY + 2, 2, ScreenTheme.NAV_ROW_HEIGHT - 4,
                        ScreenTheme.NAV_CURRENT_BAR);
            }
            String text = TextUtil.clamp(title.getNavigationTitle(), this.width - 14);
            context.drawTextWithShadow(this.fontRenderer, text, this.x + 7,
                    rowY + (ScreenTheme.NAV_ROW_HEIGHT - this.fontHeight) / 2, ScreenTheme.NAV_TEXT);
        }
        RenderUtils.endScissor();
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int button) {
        if (button != 0 || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        int index = Math.floorDiv(mouseY - this.y + this.scrollPx, ScreenTheme.NAV_ROW_HEIGHT);
        if (index >= 0 && index < this.titles.size()) {
            this.list.scrollToTitle(this.titles.get(index));
            SoundUtils.click(this.mc);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double amount) {
        if (!this.isMouseOver(mouseX, mouseY) || amount == 0) {
            return false;
        }
        this.scroll.target(this.clamp(this.scroll.getTarget()
                - (float) Math.signum(amount) * ScreenTheme.NAV_ROW_HEIGHT * ScreenTheme.SCROLL_ROWS));
        return true;
    }

    private float clamp(float value) {
        float max = Math.max(0, this.titles.size() * ScreenTheme.NAV_ROW_HEIGHT - this.height);
        return Math.max(0, Math.min(value, max));
    }
}
