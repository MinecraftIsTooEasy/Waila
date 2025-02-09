package moddedmite.waila.config;

import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.StringUtils;

public enum EnumTooltipTheme {
    Waila("#10010F", "#5001FE", "#28017E", true, true, true, false),
    Dark("#131313", "#383838", "#242424", true, true, true, false),
    TOP("#006699", "#9999ff", "#9999ff", true, false, true, false),
    Create("#000000", "#2A2626", "#1A1717", true, true, true, false),
    Tooltip("#130211", "#1F0639", "#160321", true, true, true, false),
    Achievement("#212121", "#555555", "#555555", true, true, true, false),
    Legacy("#010203", "#010203", "#010203", true, false, false, false),
    Custom();

    public final Color4f backgroundColor;
    public final Color4f frameColorTop;
    public final Color4f frameColorBottom;
    public final boolean center;
    public final boolean frame;
    public final boolean gradient;
    public final boolean coarseGradient;

    private EnumTooltipTheme(String backgroundColor, String frameColorTop, String frameColorBottom, boolean center, boolean frame, boolean gradient, boolean coarseGradient) {
        this.backgroundColor = Color4f.fromColor(StringUtils.getColor(backgroundColor, 0));
        this.frameColorTop = Color4f.fromColor(StringUtils.getColor(frameColorTop, 0));
        this.frameColorBottom = Color4f.fromColor(StringUtils.getColor(frameColorBottom, 0));
        this.center = center;
        this.frame = frame;
        this.gradient = gradient;
        this.coarseGradient = coarseGradient;
    }

    private EnumTooltipTheme(int BGColor, int frameColorTop, int frameColorBottom, boolean center, boolean frame, boolean gradient, boolean coarseGradient) {
        this.backgroundColor = Color4f.fromColor(BGColor);
        this.frameColorTop = Color4f.fromColor(frameColorTop);
        this.frameColorBottom = Color4f.fromColor(frameColorBottom);
        this.center = center;
        this.frame = frame;
        this.gradient = gradient;
        this.coarseGradient = coarseGradient;
    }

    private EnumTooltipTheme() {
        this.center = true;
        this.frame = true;
        this.gradient = true;
        this.coarseGradient = false;
        this.backgroundColor = null;
        this.frameColorTop = null;
        this.frameColorBottom = null;
    }
}
