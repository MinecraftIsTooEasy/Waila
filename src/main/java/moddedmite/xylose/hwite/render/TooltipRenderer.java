package moddedmite.xylose.hwite.render;

import moddedmite.xylose.hwite.api.IBreakingProgress;
import moddedmite.xylose.hwite.config.HwiteConfigs;
import moddedmite.xylose.hwite.info.HwiteInfo;
import moddedmite.xylose.hwite.info.InfoContext;
import moddedmite.xylose.hwite.info.InfoEntry;
import moddedmite.xylose.hwite.render.util.TTRenderHealth;
import moddedmite.xylose.hwite.util.DisplayUtil;
import net.minecraft.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TooltipRenderer {
    private static final Logger LOGGER = LogManager.getLogger(TooltipRenderer.class);
    private static Minecraft mc = Minecraft.getMinecraft();
    protected static boolean hasBlending;
    protected static boolean hasLight;
    protected static boolean hasDepthTest;
    protected static boolean hasLight1;
    protected static int boundTexIndex;

    public static void renderHWITEHud(Gui gui, Minecraft mc) {
        try {
            renderHWITEHudInternal(gui, mc);
        } catch (Exception e) {
            LOGGER.warn("exception while rendering hwite hud", e);
        }
    }

    private static void renderHWITEHudInternal(Gui gui, Minecraft mc) {
        ArrayList<String> list = new ArrayList<>();
        ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        TTRenderHealth ttRenderHealth = new TTRenderHealth();
        Point pos = new Point(HwiteConfigs.TooltipX.getIntegerValue(), HwiteConfigs.TooltipY.getIntegerValue());
        Tooltip.Renderable renderable = new Tooltip.Renderable(ttRenderHealth, pos);
        RaycastCollision rc = mc.objectMouseOver;
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        //draw model
        boolean mainInfoNotEmpty = !Objects.equals(HwiteInfo.infoMain, "");
        if (mainInfoNotEmpty && HwiteInfo.entityInfo != null && HwiteConfigs.EntityRender.getBooleanValue()) {
            //x, y, size, ?, ?
            GuiInventory.func_110423_a(HwiteConfigs.EntityInfoX.getIntegerValue(), HwiteConfigs.EntityInfoY.getIntegerValue(), HwiteConfigs.EntityInfoSize.getIntegerValue(), 0, 0, (EntityLivingBase) HwiteInfo.entityInfo);
        }

        //draw text and tooltip box
        if (rc != null) {
            List enumRenderFlag = addInfoToList(list);
            TooltipBGRender hudBackGroundRender = new TooltipBGRender();

            GL11.glPushMatrix();
            saveGLState();

            GL11.glScalef((float) HwiteConfigs.TooltipScale.getDoubleValue(), (float) HwiteConfigs.TooltipScale.getDoubleValue(), 1.0f);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            hudBackGroundRender.drawTooltipBackGround(list, false, mc);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_BLEND);

            if (!Objects.equals(HwiteInfo.infoMain, "") && HwiteInfo.hasIcon && HwiteConfigs.BlockRender.getBooleanValue()) {
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                if (HwiteInfo.blockInfo instanceof BlockDoor door) {
                    renderStack(new ItemStack(door.getDoorItem(), 1, mc.theWorld.getBlockMetadata(HwiteInfo.blockPosX, HwiteInfo.blockPosY, HwiteInfo.blockPosZ)), list);
                } else {
                    renderStack(new ItemStack(HwiteInfo.blockInfo, 1, mc.theWorld.getBlockMetadata(HwiteInfo.blockPosX, HwiteInfo.blockPosY, HwiteInfo.blockPosZ)), list);
                }
            }
            loadGLState();

            if (rc.getEntityHit() != null && rc.getEntityHit() instanceof EntityLivingBase livingBase && livingBase.getMaxHealth() <= 20) {
                int healthY = getHealthY(list);
                hudBackGroundRender.drawIcons(rc, TooltipBGRender.x + 8, healthY);
            }
            GL11.glPopMatrix();
        }
    }

    private static void renderStack(ItemStack itemStack, List list) {
        DisplayUtil.renderStack(
                TooltipBGRender.x + 5,
                list.size() == 1 ? TooltipBGRender.y - 6 : (TooltipBGRender.y - TooltipBGRender.h / 2) - 3,
                itemStack);
    }

    private static int getHealthY(ArrayList<String> list) {
        int healthY = (TooltipBGRender.y - TooltipBGRender.h / 2) + 3;
        if (list.size() == 4) {
            healthY = (TooltipBGRender.y - TooltipBGRender.h / 2) - 3;
        } else if (list.size() == 5) {
            healthY = (TooltipBGRender.y - TooltipBGRender.h / 2) - 7;
        } else if (list.size() == 6) {
            healthY = (TooltipBGRender.y - TooltipBGRender.h / 2) - 11;
        } else if (list.size() == 7) {
            healthY = (TooltipBGRender.y - TooltipBGRender.h / 2) - 14;
        }
        return healthY;
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

    private static List addInfoToList(List<String> list) {
        boolean mainInfoEmpty = Objects.equals(HwiteInfo.infoMain, "");
        if (mainInfoEmpty) {
            return null;
        }

        int breakProgress = (int) (((IBreakingProgress) Minecraft.getMinecraft().playerController).getCurrentBreakingProgress() * 100);
        boolean line2Empty = Objects.equals(HwiteInfo.info_line_2, "");
        boolean line1Empty = Objects.equals(HwiteInfo.info_line_1, "");
        boolean line1IsABlank = Objects.equals(HwiteInfo.info_line_1, " ");
        boolean line2IsABlank = Objects.equals(HwiteInfo.info_line_2, " ");
        boolean breadInfoEmpty = Objects.equals(HwiteInfo.break_info, "");

        if (HwiteInfo.entityInfo != null) {
            list.add(HwiteInfo.infoMain);
            if (HwiteInfo.renderHealth) {
                list.add("");
            }
            tryAddExtraInfo(list, breakProgress);
            if (!line1Empty) {
                list.add(HwiteInfo.info_line_1);
            }
        } else if (line2Empty) {

            list.add(HwiteInfo.infoMain);
            tryAddExtraInfo(list, breakProgress);
            if (!line1Empty) {
                list.add(HwiteInfo.info_line_1);
            }
        } else if (line1IsABlank && line2IsABlank) {
            list.add(HwiteInfo.infoMain);
            if (breakProgress > 0) {
                list.add(String.format("%d", breakProgress) + "%");
            }
        } else {
            list.add(HwiteInfo.infoMain);
            tryAddExtraInfo(list, breakProgress);
            if (!line1Empty) {
                list.add(HwiteInfo.info_line_1);
                list.add(HwiteInfo.info_line_2);
            }
        }

        if (HwiteConfigs.DisplayModName.getBooleanValue()) {
            list.add(HwiteInfo.updateModInfo(mc.objectMouseOver));
        }

        return List.of();
    }

    private static final List<InfoEntry> INFO_ENTRIES = new ArrayList<>();

    private static void tryAddExtraInfo(List<String> list, int breakProgress) {
        InfoContext context = new InfoContext(list, mc.objectMouseOver, mc.thePlayer, breakProgress);
        for (InfoEntry infoEntry : INFO_ENTRIES) {
            infoEntry.tryAddToList(context);
        }
    }

    static {
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.BreakInfo, HwiteInfo::updateBreakInfo));
        INFO_ENTRIES.add(new InfoEntry(HwiteConfigs.BreakProgress::getBooleanValue, context -> {
            int progress = context.breakProgress();
            if (progress > 0)
                return String.format(EnumChatFormatting.DARK_GRAY + I18n.getString("hwite.info.breakProgress") + "%d", progress) + "%";
            return "";
        }));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.GrowthValue, HwiteInfo::updateGrowthInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteInfo::updateEffectInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.HorseInfo, HwiteInfo::updateHorseInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.FurnaceInfo, HwiteInfo::updateFurnaceInputItemInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.FurnaceInfo, HwiteInfo::updateFurnaceOutputItemInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.FurnaceInfo, HwiteInfo::updateFurnaceFuelItemInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.MITEDetailsInfo, HwiteInfo::updateMITEDetailsInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.MITEDetailsInfo, HwiteInfo::updateInfoLine2));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.Redstone, HwiteInfo::updateRedStoneInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteConfigs.SpawnerType, HwiteInfo::updateMobSpawnerInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteInfo::updateDevInfoInfo));
        INFO_ENTRIES.add(InfoEntry.of(() -> HwiteInfo.unlocalizedNameInfo));
        INFO_ENTRIES.add(InfoEntry.of(HwiteInfo::updateHiwlaExtraInfo));
    }
}
