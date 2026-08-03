package moddedmite.waila.gui.util;

import net.minecraft.EnumChatFormatting;

/**
 * 配置界面的布局与配色常量。
 * <p>
 * 数值照抄 Jade 的界面尺寸，保证观感一致；颜色用 ARGB 纯色/渐变替代 Jade 的九宫格贴图。
 */
public final class ScreenTheme {

    private ScreenTheme() {
    }

    // ---------------------------------------------------------------- 布局

    /** 左侧导航栏（含顶部搜索框）宽度。 */
    public static final int NAV_WIDTH = 120;
    /** 搜索框高度。 */
    public static final int SEARCH_HEIGHT = 18;
    /** 导航栏单行高度。 */
    public static final int NAV_ROW_HEIGHT = 18;
    /** 选项列表单行高度。 */
    public static final int ROW_HEIGHT = 26;
    /** 选项行内容最大宽度，超出则居中留白。 */
    public static final int ROW_MAX_WIDTH = 300;
    /** 底部按钮区高度，列表下边界 = height - FOOTER_HEIGHT。 */
    public static final int FOOTER_HEIGHT = 32;

    /** 标签文字相对内容区左边的偏移。 */
    public static final int TEXT_X = 10;
    /** 标签文字相对行垂直中心的偏移。 */
    public static final int TEXT_Y = -3;
    /** 子项相对父项的额外缩进。 */
    public static final int INDENT_STEP = 12;

    /** 值控件区宽度：控件 x = 内容区右边界 - VALUE_AREA_WIDTH + offsetX。 */
    public static final int VALUE_AREA_WIDTH = 110;
    /** 标准控件宽度。 */
    public static final int WIDGET_WIDTH = 100;
    /** 标准控件高度。 */
    public static final int WIDGET_HEIGHT = 20;

    /** 底部按钮宽度。 */
    public static final int BUTTON_WIDTH = 90;
    /** 底部按钮高度。 */
    public static final int BUTTON_HEIGHT = 20;
    /** 底部按钮距屏幕底边的距离。 */
    public static final int BUTTON_BOTTOM_MARGIN = 25;

    /** tooltip 换行宽度。 */
    public static final int TOOLTIP_WRAP_WIDTH = 255;

    // ---------------------------------------------------------------- 配色

    /** 导航栏背景（半透明深色）。 */
    public static final int NAV_BACKGROUND = 0x90101010;
    /** 世界内时的导航栏背景，比主菜单更透一点。 */
    public static final int NAV_BACKGROUND_INWORLD = 0x60101010;
    /** 导航栏右侧分隔线。 */
    public static final int NAV_SEPARATOR = 0x40FFFFFF;
    /** 导航栏当前分类左侧竖条。 */
    public static final int NAV_CURRENT_BAR = 0xFFFFFFFF;
    /** 导航栏当前分类文字。 */
    public static final int NAV_TEXT = 0xFFFFFFFF;

    /** 列表悬停行高亮。 */
    public static final int ROW_HOVER = 0x33FFFFFF;
    /** 列表底部分隔线（渐变起止）。 */
    public static final int LIST_SEPARATOR_TOP = 0x00000000;
    public static final int LIST_SEPARATOR_BOTTOM = 0x88000000;

    /** 分类标题文字。 */
    public static final int TITLE_TEXT = 0xFFFFFFFF;
    /** 普通选项标签文字。 */
    public static final int LABEL_TEXT = 0xFFFFFFFF;
    /** 被禁用选项的标签文字。 */
    public static final int LABEL_TEXT_DISABLED = 0xFFAAAAAA;

    /** 滚动条槽。 */
    public static final int SCROLLBAR_TRACK = 0xC0404040;
    /** 滚动条滑块。 */
    public static final int SCROLLBAR_THUMB = 0xFFFFFFFF;
    /** 滚动条宽度。 */
    public static final int SCROLLBAR_WIDTH = 6;

    /** 位置调整模式的全屏遮罩。 */
    public static final int ADJUST_OVERLAY = 0x80808080;
    /** 位置调整模式的居中辅助线。 */
    public static final int ADJUST_GUIDE_LINE = 0xFF0000FF;

    // ---------------------------------------------------------------- 文字色码

    /** 保存按钮的浅绿。 */
    public static final String TXT_SAVE = EnumChatFormatting.GREEN.toString();
    /** 危险操作的红色。 */
    public static final String TXT_DANGER = EnumChatFormatting.RED.toString();
    /** 次要说明的灰色。 */
    public static final String TXT_MUTED = EnumChatFormatting.GRAY.toString();
    /** 按键捕获中的黄色。 */
    public static final String TXT_CAPTURING = EnumChatFormatting.YELLOW.toString();
    /** 高亮当前枚举值的绿色。 */
    public static final String TXT_ACTIVE = EnumChatFormatting.GREEN.toString();
    public static final String TXT_RESET = EnumChatFormatting.RESET.toString();

    // ---------------------------------------------------------------- 动画

    /** 列表平滑滚动速度（Jade 用 0.6）。 */
    public static final float SCROLL_SPEED = 0.6F;
    /** 一次滚轮的行数。 */
    public static final int SCROLL_ROWS = 3;
    /** 按住 Ctrl 时一次滚轮的行数。 */
    public static final int SCROLL_ROWS_FAST = 9;
}
