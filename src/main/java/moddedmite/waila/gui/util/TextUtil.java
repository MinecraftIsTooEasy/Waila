package moddedmite.waila.gui.util;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.FontRenderer;

public final class TextUtil {

    public static final char SECTION = '\u00a7';

    private TextUtil() {
    }

    private static FontRenderer font() {
        return RenderUtils.fontRenderer();
    }

    public static int width(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return font().getStringWidth(text);
    }

    public static int lineHeight() {
        return font().FONT_HEIGHT;
    }

    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.indexOf(SECTION) < 0) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == SECTION && i + 1 < text.length()) {
                i++; // 跳过色码字符本身
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String clamp(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (width(text) <= maxWidth) {
            return text;
        }
        int ellipsisWidth = width("...");
        if (maxWidth <= ellipsisWidth) {
            return font().trimStringToWidth(text, Math.max(0, maxWidth));
        }
        return font().trimStringToWidth(text, maxWidth - ellipsisWidth) + "...";
    }

    public static List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        for (String paragraph : text.split("\\n")) {
            @SuppressWarnings("unchecked")
            List<String> lines = font().listFormattedStringToWidth(paragraph, maxWidth);
            out.addAll(lines);
        }
        return out;
    }

    public static int scaleRGB(int argb, float scale) {
        float f = Math.max(0.0F, Math.min(1.0F, scale));
        int a = (argb >>> 24) & 0xFF;
        int r = (int) (((argb >> 16) & 0xFF) * f);
        int g = (int) (((argb >> 8) & 0xFF) * f);
        int b = (int) ((argb & 0xFF) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int applyAlpha(int argb, float alpha) {
        float f = Math.max(0.0F, Math.min(1.0F, alpha));
        int a = (int) (((argb >>> 24) & 0xFF) * f);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerpColor(int from, int to, float t) {
        float f = Math.max(0.0F, Math.min(1.0F, t));
        int a = lerpChannel(from >>> 24, to >>> 24, f);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, f);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, f);
        int b = lerpChannel(from & 0xFF, to & 0xFF, f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return (from & 0xFF) + (int) (((to & 0xFF) - (from & 0xFF)) * t);
    }

    public static String replaceVariable(String text, String placeholder, String replacement) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String token = "${" + placeholder + "}";
        if (!text.contains(token)) {
            return text;
        }
        return text.replace(token, replacement == null ? "" : replacement);
    }
}
