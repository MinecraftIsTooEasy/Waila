package mcp.mobius.waila.overlay;

import fi.dy.masa.malilib.util.Color4f;
import mcp.mobius.waila.Waila;
import moddedmite.waila.api.IBreakingProgress;
import moddedmite.waila.config.EnumTooltipTheme;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.BossStatus;
import net.minecraft.Minecraft;
import net.minecraft.RaycastCollision;
import net.minecraft.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;


public class OverlayRenderer {
    protected static boolean hasBlending;
    protected static boolean hasLight;
    protected static boolean hasDepthTest;
    protected static boolean hasLight0;
    protected static boolean hasLight1;
    protected static boolean hasRescaleNormal;
    protected static boolean hasColorMaterial;
    protected static int boundTexIndex;
    private static int lastProgressLine = 0;
    private static int targetX = 0, targetY = 0, targetW = 0, targetH = 0;
    private static float currentX = 0, currentY = 0, currentW = 0, currentH = 0;
    private static float LERP_FACTOR = (float) WailaConfig.lerpfactor.getDoubleValue();
    private static float lastBreakProgress = 0f;
    private static float currentAlpha = 0f;
    private static final float FADE_SPEED = 0.1f;

    public OverlayRenderer() {
    }

    public void renderOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        RaycastCollision rc = mc.objectMouseOver;//mop

        // change too many && to simple returns
        if (mc.currentScreen != null) return;
        if (mc.theWorld == null) return;
        if (!Minecraft.isGuiEnabled()) return;
        if (mc.gameSettings.keyBindPlayerList.pressed) return;
        if (!WailaConfig.showTooltip.getBooleanValue()) return;
        if (RayTracing.instance().getTarget() == null) return;
        if (rc == null) return;

        Tooltip tooltip = WailaTickHandler.instance().tooltip;
        if (tooltip == null) return;// not ready

        if (rc.isBlock()
                && RayTracing.instance().getTargetStack() != null) {
            renderOverlay(tooltip);
        }
        if (rc.isEntity()
                && WailaConfig.showEnts.getBooleanValue()) {
            renderOverlay(tooltip);
        }
    }

    public void renderOverlay(Tooltip tooltip) {
        GL11.glPushMatrix();
        saveGLState();

        handleLerp(tooltip);
        if (currentAlpha <= 0f) {
            loadGLState();
            GL11.glPopMatrix();
            return;
        }

        GL11.glScalef(OverlayConfig.scale, OverlayConfig.scale, 1.0f);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        EnumTooltipTheme theme = WailaConfig.theme.getEnumValue();
        Color4f tooltipBGColor = WailaConfig.bgcolor.getColor();
        Color4f tooltipFrameColorTop = WailaConfig.gradient1.getColor();
        Color4f tooltipFrameColorBottom = WailaConfig.gradient2.getColor();
        float configAlpha = WailaConfig.alpha.getIntegerValue() / 100.0f;

        if (theme != EnumTooltipTheme.Custom) {
            tooltipBGColor = Color4f.fromColor(theme.backgroundColor, currentAlpha * configAlpha);
            tooltipFrameColorTop = Color4f.fromColor(theme.frameColorTop, currentAlpha * configAlpha);
            tooltipFrameColorBottom = Color4f.fromColor(theme.frameColorBottom, currentAlpha * configAlpha);
        }

        drawTooltipBox(
                tooltip.x,
                tooltip.y,
                tooltip.w,
                tooltip.h,
                tooltipBGColor.intValue,
                tooltipFrameColorTop.intValue,
                tooltipFrameColorBottom.intValue,
                theme.center,
                theme.frame,
                theme.gradient);

        drawBreakProgress(
                tooltip.x,
                tooltip.y,
                tooltip.w,
                tooltip.h);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        tooltip.draw();
        GL11.glDisable(GL11.GL_BLEND);

        tooltip.draw2nd();

        if (tooltip.hasIcon) RenderHelper.enableGUIStandardItemLighting();

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        if (tooltip.hasIcon && tooltip.stack != null && tooltip.stack.getItem() != null)
            DisplayUtil.renderStack(tooltip.x + 5, tooltip.y + tooltip.h / 2 - 8, tooltip.stack);

        loadGLState();
        GL11.glPopMatrix();
    }

    public static void saveGLState() {
        hasBlending = GL11.glGetBoolean(GL11.GL_BLEND);
        hasLight = GL11.glGetBoolean(GL11.GL_LIGHTING);
        hasDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boundTexIndex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
    }

    public static void loadGLState() {
        if (hasBlending) GL11.glEnable(GL11.GL_BLEND);
        else GL11.glDisable(GL11.GL_BLEND);
        if (hasLight1) GL11.glEnable(GL11.GL_LIGHT1);
        else GL11.glDisable(GL11.GL_LIGHT1);
        if (hasDepthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTexIndex);
        GL11.glPopAttrib();
    }

    public static void handleLerp(Tooltip tooltip) {
        if (tooltip != null) {
            currentAlpha = DisplayUtil.lerp(currentAlpha, 1f, FADE_SPEED);
            if (currentAlpha > 0.99f) {
                currentAlpha = 1f;
            }
        } else {
            currentAlpha = DisplayUtil.lerp(currentAlpha, 0f, FADE_SPEED);
            if (currentAlpha < 0.01f) {
                currentAlpha = 0f;
            }
        }
    }

    public static void drawTooltipBox(int x, int y, int w, int h, int bg, int grad1, int grad2, boolean center, boolean frame, boolean gradient) {
        targetX = x;
        targetY = y;
        targetW = w;
        targetH = h;

        currentX = DisplayUtil.lerp(currentX, targetX, LERP_FACTOR);
        currentY = DisplayUtil.lerp(currentY, targetY, LERP_FACTOR);
        currentW = DisplayUtil.lerp(currentW, targetW, LERP_FACTOR);
        currentH = DisplayUtil.lerp(currentH, targetH, LERP_FACTOR);

        int drawX = (int) currentX;
        int drawY = (int) currentY;
        int drawW = (int) currentW;
        int drawH = (int) currentH;

        EnumTooltipTheme theme = WailaConfig.theme.getEnumValue();
        if (theme.center) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 1, drawW - 1, drawH - 1, bg, bg); // center
        }
        if (theme.frame) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY, drawW - 1, 1, bg, bg); // top frame
            DisplayUtil.drawGradientRect(drawX + 1, drawY + drawH, drawW - 1, 1, bg, bg); // bottom frame
            DisplayUtil.drawGradientRect(drawX, drawY + 1, 1, drawH - 1, bg, bg); // left frame
            DisplayUtil.drawGradientRect(drawX + drawW, drawY + 1, 1, drawH - 1, bg, bg); // right frame
        }
        if (theme.gradient) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 1, drawW - 1, 1, grad1, grad1); // top gradient
            DisplayUtil.drawGradientRect(drawX + 1, drawY + drawH - 1, drawW - 1, 1, grad2, grad2); // bottom gradient
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 2, 1, drawH - 3, grad1, grad2); // left gradient
            DisplayUtil.drawGradientRect(drawX + drawW - 1, drawY + 2, 1, drawH - 3, grad1, grad2); // right gradient
        }
        if (theme.coarseGradient) {//WIP
            DisplayUtil.drawGradientRect(drawX, drawY + 2, 3, drawH - 3, grad1, grad2);
            DisplayUtil.drawGradientRect(drawX + drawW - 3, drawY + 2, 3, drawH - 3, grad1, grad2);
            DisplayUtil.drawGradientRect(drawX, drawY, drawW, 3, grad1, grad1);
            DisplayUtil.drawGradientRect(drawX, drawY + drawH - 3, drawW, 3, grad2, grad2);
        }
    }

    public void drawBreakProgress(int x, int y, int w, int h) {
        float breakProgress;
        if (Minecraft.getMinecraft().playerController != null) {
            breakProgress = ((IBreakingProgress) Minecraft.getMinecraft().playerController).getCurrentBreakingProgress();
            int currentProgressLine = 0;

            if (breakProgress > 0.0f) {
                int progress = (int) (breakProgress * 100.0f);
                currentProgressLine = (int) (progress / 100.0 * w);
                lastProgressLine = currentProgressLine;
                lastBreakProgress = breakProgress;
            } else {
                if (lastBreakProgress > 0.0f) {
                    lastProgressLine = (int) (lastProgressLine * 0.9f);
                    currentProgressLine = lastProgressLine;
                    if (currentProgressLine < 1) {
                        currentProgressLine = 0;
                        lastBreakProgress = 0.0f;
                    }
                }
            }

            if (currentProgressLine > 0) {
                DisplayUtil.drawGradientRect(x + 1, y + (h - 2), currentProgressLine, 1, 0xFF74766B, 0xFF74766B);
            }
        }
    }
}
