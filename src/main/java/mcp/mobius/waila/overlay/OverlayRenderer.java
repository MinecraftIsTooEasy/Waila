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
	private static float currentX = 0, currentY = 0, currentW = 0, currentH = 0;
    private static boolean snapNext = false;
    private static float lastBreakProgress = 0f;

    public OverlayRenderer() {
    }

    public void renderOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        RaycastCollision rc = RayTracing.instance().getTarget();

        // change too many && to simple returns
        if (mc.currentScreen != null) return;
        if (mc.theWorld == null) return;
        if (!Minecraft.isGuiEnabled()) return;
        if (mc.gameSettings.keyBindPlayerList.pressed) return;
        if (!WailaConfig.showTooltip.getBooleanValue()) return;
        if (rc == null) return;

        Tooltip tooltip = WailaTickHandler.instance().getTooltip();
        if (tooltip == null) return;// not ready

        if (rc.isBlock()
                && RayTracing.instance().getTargetStack() != null) {
            renderOverlay(tooltip);
        }
        if (rc.isEntity()
                && WailaConfig.showEnts.getBooleanValue()) {
            renderOverlay(tooltip);
        }
        tooltip.w = -200;
        tooltip.h = -200;
    }

    public void renderOverlay(Tooltip tooltip) {
        GL11.glPushMatrix();
        saveGLState();

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
            tooltipBGColor = Color4f.fromColor(theme.backgroundColor, configAlpha);
            tooltipFrameColorTop = Color4f.fromColor(theme.frameColorTop, configAlpha);
            tooltipFrameColorBottom = Color4f.fromColor(theme.frameColorBottom, configAlpha);
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

    /**
     * 让下一帧直接跳到目标位置，不做平滑插值。
     * 配置界面的位置调整模式用它，否则框会以 lerpfactor 的速度追鼠标，定位手感很差。
     */
    public static void snapAnimation() {
        snapNext = true;
    }

    /**
     * 把平滑插值恢复到「从未绘制」状态，使下一次绘制走正常的渐入动画。
     * 配置界面关闭时调用，避免游戏内 HUD 从预览残留的位置飘进来。
     */
    public static void resetAnimation() {
        currentX = 0;
        currentY = 0;
        currentW = 0;
        currentH = 0;
        snapNext = false;
    }

    public static void drawTooltipBox(int x, int y, int w, int h, int bg, int grad1, int grad2, boolean center, boolean frame, boolean gradient) {
        float lerpFactor = (float) WailaConfig.lerpfactor.getDoubleValue();

        int centerX = x + w / 2;

        if (snapNext) {
            snapNext = false;
            currentX = centerX;
            currentY = y;
            currentW = w;
            currentH = h;
        } else if (currentW == 0 && currentH == 0) {
            currentX = centerX;
            currentY = y;
            currentW = 0;
            currentH = 0;
        }

        currentX = DisplayUtil.lerp(currentX, centerX, lerpFactor);
        currentY = DisplayUtil.lerp(currentY, y, lerpFactor);
        currentW = DisplayUtil.lerp(currentW, w, lerpFactor);
        currentH = DisplayUtil.lerp(currentH, h, lerpFactor);
        
        int drawX = (int) (currentX - currentW / 2);
        int drawY = (int) currentY;
        int drawW = (int) currentW;
        int drawH = (int) currentH;

        EnumTooltipTheme theme = WailaConfig.theme.getEnumValue();
        if (theme.center) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 1, drawW - 1, drawH - 1, bg, bg);
        }
        if (theme.frame) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY, drawW - 1, 1, bg, bg);
            DisplayUtil.drawGradientRect(drawX + 1, drawY + drawH, drawW - 1, 1, bg, bg);
            DisplayUtil.drawGradientRect(drawX, drawY + 1, 1, drawH - 1, bg, bg);
            DisplayUtil.drawGradientRect(drawX + drawW, drawY + 1, 1, drawH - 1, bg, bg);
        }
        if (theme.gradient) {
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 1, drawW - 1, 1, grad1, grad1);
            DisplayUtil.drawGradientRect(drawX + 1, drawY + drawH - 1, drawW - 1, 1, grad2, grad2);
            DisplayUtil.drawGradientRect(drawX + 1, drawY + 2, 1, drawH - 3, grad1, grad2);
            DisplayUtil.drawGradientRect(drawX + drawW - 1, drawY + 2, 1, drawH - 3, grad1, grad2);
        }
        if (theme.coarseGradient) {
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
