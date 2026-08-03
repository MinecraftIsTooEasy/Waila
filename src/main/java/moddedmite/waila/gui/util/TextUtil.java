package moddedmite.waila.gui.util;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.FontRenderer;

/**
 * 1.6.4 只有 {@code String} + {@code §} 色码，没有 {@code Component}。
 * 这里集中处理色码安全的文本操作、多行拆分与颜色计算，替代 Jade 依赖的
 * {@code Component} / {@code Style} / {@code ARGB} 那一整套设施。
 */
public final class TextUtil {

    /** Minecraft 色码前缀。 */
    public static final char SECTION = '\u00a7';

    private TextUtil() {
    }

    private static FontRenderer font() {
        return RenderUtils.fontRenderer();
    }

    // ---------------------------------------------------------------- 基础度量

    /** 文本渲染宽度（已含色码剔除，由 FontRenderer 负责）。 */
    public static int width(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return font().getStringWidth(text);
    }

    /** 单行文字高度。 */
    public static int lineHeight() {
        return font().FONT_HEIGHT;
    }

    // ---------------------------------------------------------------- 色码处理

    /**
     * 剥掉所有 {@code §x} 色码，用于搜索匹配与长度比较。
     */
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

    /**
     * 截断到指定像素宽度，超出时追加省略号。色码由 FontRenderer 的
     * {@code trimStringToWidth} 保留处理。
     */
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

    // ---------------------------------------------------------------- 多行拆分

    /**
     * 把可能含 {@code \n} 的文本按给定宽度拆成多行，供 tooltip 使用。
     * <p>
     * {@link FontRenderer#listFormattedStringToWidth(String, int)} 返回裸 {@code List}，
     * 这里统一转成 {@code List<String>}。
     */
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

    // ---------------------------------------------------------------- 颜色

    /**
     * 按比例缩放 RGB 分量，保留 alpha。对应 Jade 的 {@code ARGB.scaleRGB}，
     * 用于渐变标题的扫光效果。
     *
     * @param argb  原始颜色
     * @param scale 缩放系数，会被夹到 [0, 1]
     */
    public static int scaleRGB(int argb, float scale) {
        float f = Math.max(0.0F, Math.min(1.0F, scale));
        int a = (argb >>> 24) & 0xFF;
        int r = (int) (((argb >> 16) & 0xFF) * f);
        int g = (int) (((argb >> 8) & 0xFF) * f);
        int b = (int) ((argb & 0xFF) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 按比例设置 alpha，保留 RGB。对应 Jade 的 {@code IWailaConfig.Overlay.applyAlpha}。
     *
     * @param argb  原始颜色
     * @param alpha 目标不透明度，会被夹到 [0, 1]
     */
    public static int applyAlpha(int argb, float alpha) {
        float f = Math.max(0.0F, Math.min(1.0F, alpha));
        int a = (int) (((argb >>> 24) & 0xFF) * f);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * 在两个颜色之间线性插值（含 alpha）。
     *
     * @param t 插值系数，会被夹到 [0, 1]
     */
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

    // ---------------------------------------------------------------- 变量替换

    /**
     * 把文本里的 {@code ${NAME}} 占位符替换成实际值。
     * 对应 Jade 的 {@code BaseOptionsScreen.processBuiltInVariables}。
     *
     * @param text        原文
     * @param placeholder 占位符名，不含 {@code ${}}
     * @param replacement 替换值，为 null 时按空串处理
     */
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
