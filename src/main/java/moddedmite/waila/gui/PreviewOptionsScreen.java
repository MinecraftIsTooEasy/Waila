package moddedmite.waila.gui;

import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import mcp.mobius.waila.overlay.OverlayConfig;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.Tooltip;
import mcp.mobius.waila.overlay.WailaTickHandler;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

/** Option screen base with a live Waila overlay preview and position adjustment mode. */
public abstract class PreviewOptionsScreen extends BaseOptionsScreen {
    private boolean previewEnabled = true;
    private boolean adjustingPosition;
    private boolean draggingPosition;
    private float dragOffsetX;
    private float dragOffsetY;
    private @Nullable ButtonGeneric previewButton;

    protected PreviewOptionsScreen(@Nullable GuiScreen parent, String titleKey) {
        super(parent, titleKey);
    }

    @Override
    protected void initBaseLayer(Layer layer) {
        super.initBaseLayer(layer);
        this.previewButton = ButtonGeneric.builder(this.previewButtonText(), button -> {
                    this.previewEnabled = !this.previewEnabled;
                    button.setDisplayString(this.previewButtonText());
                })
                .dimensions(this.width - 290, this.height - 25, 90, 20)
                .build();
        layer.addWidget(this.previewButton);
    }

    public void startAdjustingPosition() {
        this.adjustingPosition = true;
        this.draggingPosition = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.adjustingPosition) {
            RenderUtils.drawRect(0, 0, this.width, this.height, 0x80808080);
        }

        if (this.previewEnabled || this.adjustingPosition) {
            OverlayConfig.updateColors();
            Tooltip tooltip = this.buildPreviewTooltip();
            if (tooltip == null && this.adjustingPosition) {
                tooltip = this.buildNoTargetTooltip();
            }
            if (tooltip == null) {
                return;
            }
            if (this.adjustingPosition) {
                // drawTooltipBox 的平滑插值是 static，调整位置时必须每帧吸附，
                // 否则框会以 lerpfactor 的速度滞后于鼠标，定位无法对准。
                OverlayRenderer.snapAnimation();
                this.drawAdjustmentGuides(tooltip);
            }
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT);
            try {
                new OverlayRenderer().renderOverlay(tooltip);
            } finally {
                GL11.glPopAttrib();
            }
            if (this.adjustingPosition) {
                this.drawExitHint(context);
            }
        }
    }

    private @Nullable Tooltip buildPreviewTooltip() {
        return WailaTickHandler.instance().getTooltip();
    }

    private Tooltip buildNoTargetTooltip() {
        return new Tooltip(List.of(StringUtils.translate("gui.waila.preview.no_target")), false);
    }

    private void drawAdjustmentGuides(Tooltip tooltip) {
        if (WailaConfig.posX.getIntegerValue() == 50) {
            RenderUtils.drawRect(this.width / 2, 0, 1, this.height, 0xFF409CFF);
        }
        PositionBounds bounds = this.positionBounds(tooltip.getBoxWidth(), tooltip.getBoxHeight());
        if (WailaConfig.posY.getIntegerValue() == clamp(50, 0, bounds.maxY)) {
            RenderUtils.drawRect(0, this.height / 2, this.width, 1, 0xFF409CFF);
        }
    }

    private void drawExitHint(DrawContext context) {
        String text = StringUtils.translate("gui.waila.overlay_pos.exit");
        int textWidth = RenderUtils.fontRenderer().getStringWidth(text);
        int x = (this.width - textWidth) / 2;
        int y = this.height / 2 - RenderUtils.fontRenderer().FONT_HEIGHT / 2;
        RenderUtils.drawRect(x - 5, y - 4, textWidth + 10,
                RenderUtils.fontRenderer().FONT_HEIGHT + 8, 0xA0000000);
        context.drawTextWithShadow(RenderUtils.fontRenderer(), text, x, y, 0xFFFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.adjustingPosition) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (mouseButton != 0) {
            return;
        }

        Tooltip tooltip = this.freshTooltip();
        float scaledMouseX = mouseX / OverlayConfig.scale;
        float scaledMouseY = mouseY / OverlayConfig.scale;
        if (scaledMouseX >= tooltip.getBoxX()
                && scaledMouseX < tooltip.getBoxX() + tooltip.getBoxWidth()
                && scaledMouseY >= tooltip.getBoxY()
                && scaledMouseY < tooltip.getBoxY() + tooltip.getBoxHeight()) {
            this.draggingPosition = true;
            this.dragOffsetX = scaledMouseX - (tooltip.getBoxX() + tooltip.getBoxWidth() / 2.0f);
            this.dragOffsetY = scaledMouseY - (tooltip.getBoxY() + tooltip.getBoxHeight() / 2.0f);
            return;
        }

        int xIndex = clamp((int) (mouseX / (this.width / 3.0)), 0, 2);
        int yIndex = clamp((int) (mouseY / (this.height / 3.0)), 0, 2);
        if (xIndex == 1 && yIndex == 1) {
            this.adjustingPosition = false;
            this.draggingPosition = false;
            return;
        }

        PositionBounds bounds = this.positionBounds(tooltip.getBoxWidth(), tooltip.getBoxHeight());
        int posX = xIndex == 0 ? bounds.minX : xIndex == 1 ? clamp(50, bounds.minX, bounds.maxX) : bounds.maxX;
        int posY = yIndex == 0 ? 0 : yIndex == 1 ? clamp(50, 0, bounds.maxY) : bounds.maxY;
        this.applyPosition(posX, posY, tooltip.getBoxWidth(), tooltip.getBoxHeight());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceClick) {
        if (!this.adjustingPosition) {
            super.mouseClickMove(mouseX, mouseY, button, timeSinceClick);
            return;
        }
        if (!this.draggingPosition || button != 0) {
            return;
        }

        Tooltip tooltip = this.freshTooltip();
        PositionBounds bounds = this.positionBounds(tooltip.getBoxWidth(), tooltip.getBoxHeight());
        float scaledMouseX = mouseX / OverlayConfig.scale;
        float scaledMouseY = mouseY / OverlayConfig.scale;
        float centerX = scaledMouseX - this.dragOffsetX;
        float topY = scaledMouseY - this.dragOffsetY - tooltip.getBoxHeight() / 2.0f;
        int posX = Math.round(centerX * 100.0f / bounds.screenW);
        int posY = Math.round(topY * 100.0f / bounds.screenH);
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        if (!ctrl && posX >= 48 && posX <= 52) {
            posX = 50;
        }
        this.applyPosition(posX, posY, tooltip.getBoxWidth(), tooltip.getBoxHeight());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.adjustingPosition) {
            this.draggingPosition = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!this.adjustingPosition) {
            return super.charTyped(chr, keyCode);
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.adjustingPosition = false;
            this.draggingPosition = false;
            return true;
        }

        int dx = 0;
        int dy = 0;
        int step = GuiScreen.isShiftKeyDown() ? 5 : 1;
        if (keyCode == Keyboard.KEY_LEFT) dx = -step;
        else if (keyCode == Keyboard.KEY_RIGHT) dx = step;
        else if (keyCode == Keyboard.KEY_UP) dy = -step;
        else if (keyCode == Keyboard.KEY_DOWN) dy = step;
        else return true;

        Tooltip tooltip = this.freshTooltip();
        this.applyPosition(WailaConfig.posX.getIntegerValue() + dx,
                WailaConfig.posY.getIntegerValue() + dy,
                tooltip.getBoxWidth(), tooltip.getBoxHeight());
        return true;
    }

    private Tooltip freshTooltip() {
        OverlayConfig.updateColors();
        Tooltip tooltip = this.buildPreviewTooltip();
        return tooltip != null ? tooltip : this.buildNoTargetTooltip();
    }

    private void applyPosition(int posXPercent, int posYPercent, int boxW, int boxH) {
        PositionBounds bounds = this.positionBounds(boxW, boxH);
        WailaConfig.posX.setIntegerValue(clamp(posXPercent, bounds.minX, bounds.maxX));
        WailaConfig.posY.setIntegerValue(clamp(posYPercent, 0, bounds.maxY));
    }

    private PositionBounds positionBounds(int boxW, int boxH) {
        int screenW = Math.max(1, (int) (this.width / OverlayConfig.scale) - 1);
        int screenH = Math.max(1, (int) (this.height / OverlayConfig.scale) - 1);
        int minX = (int) Math.ceil(50.0 * boxW / screenW);
        int maxX = (int) Math.floor(100.0 - 50.0 * boxW / screenW);
        int maxY = (int) Math.floor(100.0 - 100.0 * boxH / screenH);
        minX = clamp(minX, 0, 100);
        maxX = clamp(maxX, minX, 100);
        maxY = clamp(maxY, 0, 100);
        return new PositionBounds(screenW, screenH, minX, maxX, maxY);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // 平滑插值是 static，预览会把它留在预览框的位置。
        // 不复位的话，回到游戏后 HUD 会从这个残留位置飘过去。
        OverlayRenderer.resetAnimation();
    }

    private String previewButtonText() {
        return StringUtils.translate(this.previewEnabled ? "gui.waila.preview.on" : "gui.waila.preview.off");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record PositionBounds(int screenW, int screenH, int minX, int maxX, int maxY) {
    }
}
