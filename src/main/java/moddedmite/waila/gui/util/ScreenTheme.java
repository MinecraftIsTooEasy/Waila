package moddedmite.waila.gui.util;

import net.minecraft.EnumChatFormatting;

public final class ScreenTheme {

    private ScreenTheme() {
    }

    // ---------------------------------------------------------------- Layout

    public static final int NAV_WIDTH = 120;
    public static final int SEARCH_HEIGHT = 18;
    public static final int NAV_ROW_HEIGHT = 18;
    public static final int ROW_HEIGHT = 26;
    public static final int ROW_MAX_WIDTH = 300;
    public static final int FOOTER_HEIGHT = 32;

    public static final int TEXT_X = 10;
    public static final int TEXT_Y = -3;
    public static final int INDENT_STEP = 12;

    public static final int VALUE_AREA_WIDTH = 124;
    public static final int WIDGET_WIDTH = 100;
    public static final int WIDGET_HEIGHT = 20;
    public static final int RESET_BUTTON_SIZE = 20;
    public static final int RESET_BUTTON_GAP = 4;

    public static final int BUTTON_WIDTH = 90;
    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_BOTTOM_MARGIN = 25;

    public static final int TOOLTIP_WRAP_WIDTH = 255;

    // ---------------------------------------------------------------- Screen Color

    public static final int NAV_BACKGROUND = 0x90101010;
    public static final int NAV_BACKGROUND_INWORLD = 0x60101010;
    public static final int NAV_SEPARATOR = 0x40FFFFFF;
    public static final int NAV_CURRENT_BAR = 0xFFFFFFFF;
    public static final int NAV_TEXT = 0xFFFFFFFF;

    public static final int ROW_HOVER = 0x33FFFFFF;
    public static final int LIST_SEPARATOR_TOP = 0x00000000;
    public static final int LIST_SEPARATOR_BOTTOM = 0x88000000;

    public static final int TITLE_TEXT = 0xFFFFFFFF;
    public static final int LABEL_TEXT = 0xFFFFFFFF;
    public static final int LABEL_TEXT_DISABLED = 0xFFAAAAAA;

    public static final int SCROLLBAR_TRACK = 0xC0404040;
    public static final int SCROLLBAR_THUMB = 0xFFFFFFFF;
    public static final int SCROLLBAR_WIDTH = 6;

    public static final int ADJUST_OVERLAY = 0x80808080;
    public static final int ADJUST_GUIDE_LINE = 0xFF0000FF;

    // ---------------------------------------------------------------- Text Color

    public static final String TXT_SAVE = EnumChatFormatting.GREEN.toString();
    public static final String TXT_DANGER = EnumChatFormatting.RED.toString();
    public static final String TXT_MUTED = EnumChatFormatting.GRAY.toString();
    public static final String TXT_CAPTURING = EnumChatFormatting.YELLOW.toString();
    public static final String TXT_ACTIVE = EnumChatFormatting.GREEN.toString();
    public static final String TXT_RESET = EnumChatFormatting.RESET.toString();

    // ---------------------------------------------------------------- Animation

    public static final float SCROLL_SPEED = 0.6F;
    public static final int SCROLL_ROWS = 3;
    public static final int SCROLL_ROWS_FAST = 9;
}
