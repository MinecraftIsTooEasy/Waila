package moddedmite.waila.gui;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.gui.screen.LayeredScreen;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.FontRenderer;
import net.minecraft.GuiScreen;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/** Jade-style landing page for the Waila configuration screens. */
public class WailaHomeScreen extends LayeredScreen {
    private static final String MOD_ID = "waila";
    private static final int HOME_BUTTON_WIDTH = 120;
    private static final int HOME_BUTTON_HEIGHT = 20;
    private static final int TITLE_LEFT_OFFSET = 105;
    private static final int TITLE_TOP_OFFSET = 20;
    private static final float GLINT_PERIOD_TICKS = 90.0F;
    private static final float GLINT_HALF_PERIOD_TICKS = 45.0F;
    private static final float GLINT_RADIUS = 20.0F;
    private static final int FANCY_TEXT_COLOR = 0xFFAAAAAA;

    private long lastNano;
    private float animationTicks;

    public WailaHomeScreen(@Nullable GuiScreen parent) {
        this.setParent(parent);
    }

    @Override
    protected void initBaseLayer(Layer layer) {
        super.initBaseLayer(layer);
        int x = (this.width - HOME_BUTTON_WIDTH) / 2;
        layer.addWidget(ButtonGeneric.builder(StringUtils.translate("gui.waila.home.settings"),
                        button -> this.mc.displayGuiScreen(
                                new WailaSettingsScreen(this, "gui.waila.waila_settings")))
                .dimensions(x, this.height / 2 - 10, HOME_BUTTON_WIDTH, HOME_BUTTON_HEIGHT).build());
        layer.addWidget(ButtonGeneric.builder(StringUtils.translate("gui.done"), button -> this.close())
                .dimensions(x, this.height / 2 + 20, HOME_BUTTON_WIDTH, HOME_BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        super.render(context, mouseX, mouseY, partialTicks);
        this.updateAnimationTime();

        FontRenderer font = RenderUtils.fontRenderer();
        int left = this.width / 2 - TITLE_LEFT_OFFSET;
        int top = this.height / 4 - TITLE_TOP_OFFSET;
        String title = "Waila";

        GL11.glPushMatrix();
        GL11.glTranslatef(left, top, 0.0F);
        GL11.glScalef(2.0F, 2.0F, 1.0F);
        font.drawString(title, 0, 0, 0xFFFFFFFF);

        // Return to normal GUI scale relative to the 2x title before drawing small text.
        GL11.glScalef(0.5F, 0.5F, 1.0F);
        font.drawString(StringUtils.getModVersionString(MOD_ID), font.getStringWidth(title) * 2 + 6, 4,
                0xFFAAAAAA);
        String desc1 = StringUtils.translate("gui.waila.configuration.desc1");
        String desc2 = StringUtils.translate("gui.waila.configuration.desc2");
        this.drawFancyTitle(font, desc1, 20, mouseX - left, mouseY - top);
        if (!desc2.isEmpty()) {
            this.drawFancyTitle(font, desc2, 32, mouseX - left, mouseY - top);
        }
        GL11.glPopMatrix();
    }

    private void updateAnimationTime() {
        long now = System.nanoTime();
        float deltaTicks = this.lastNano == 0L ? 0.0F : (now - this.lastNano) / 50_000_000.0F;
        this.lastNano = now;
        deltaTicks = Math.max(0.0F, Math.min(5.0F, deltaTicks));
        this.animationTicks += deltaTicks;
    }

    private void drawFancyTitle(FontRenderer font, String text, int y, float mouseX, float mouseY) {
        float movingCenter = ((this.animationTicks - y / 5.0F) % GLINT_PERIOD_TICKS)
                / GLINT_HALF_PERIOD_TICKS * this.width;
        if (movingCenter < 0.0F) {
            movingCenter += this.width * 2.0F;
        }
        float mouseStrength = 1.0F - clamp01(Math.abs(mouseY - y) / GLINT_RADIUS);
        int x = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int charWidth = font.getCharWidth(character);
            float center = x + charWidth / 2.0F;
            float moving = 0.65F + clamp01(1.0F - Math.abs(center - movingCenter) / GLINT_RADIUS) * 0.35F;
            float following = 0.65F + clamp01(1.0F - Math.abs(center - mouseX) / GLINT_RADIUS)
                    * 0.35F * mouseStrength;
            font.drawString(String.valueOf(character), x, y, scaleRgb(FANCY_TEXT_COLOR, Math.max(moving, following)));
            x += charWidth;
        }
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int scaleRgb(int argb, float multiplier) {
        int alpha = argb >>> 24;
        int red = Math.min(255, (int) (((argb >> 16) & 0xFF) * multiplier));
        int green = Math.min(255, (int) (((argb >> 8) & 0xFF) * multiplier));
        int blue = Math.min(255, (int) ((argb & 0xFF) * multiplier));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
