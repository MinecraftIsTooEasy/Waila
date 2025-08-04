package mcp.mobius.waila.handlers;

import static mcp.mobius.waila.api.SpecialChars.BLUE;
import static mcp.mobius.waila.api.SpecialChars.GRAY;
import static mcp.mobius.waila.api.SpecialChars.ITALIC;
import static mcp.mobius.waila.api.SpecialChars.WHITE;
import static mcp.mobius.waila.api.SpecialChars.getRenderString;

import java.text.DecimalFormat;
import java.util.List;

import mcp.mobius.waila.cbcore.LangUtil;
import mcp.mobius.waila.utils.ModIdentification;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.*;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;

public class HUDHandlerEntities implements IWailaEntityProvider {

    public static int nhearts = 20;
    public static float maxhpfortext = 40.0f;
    public static int nArmorIconsPerLine = 20;
    public static float maxArmorForText = 20.0f;

    @Override
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        try {
            currenttip.add(WHITE + entity.getEntityName());
        } catch (Exception e) {
            currenttip.add(WHITE + "Unknown");
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        this.getEntityHeath(entity, currenttip, accessor, config);
        this.getEntityArmor(entity, currenttip, accessor, config);
        this.getEntityAttack(entity, currenttip, accessor, config);
        return currenttip;
    }

    public void getEntityHeath(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!WailaConfig.showhp.getBooleanValue()) return;

        if (entity instanceof EntityLivingBase entityLivingBase) {

            nhearts = nhearts <= 0 ? 20 : nhearts;

            float health = entityLivingBase.getHealth() / 2.0f;
            float maxhp = entityLivingBase.getMaxHealth() / 2.0f;
            if (maxhp <= 0) return;

            if (entityLivingBase.getMaxHealth() > maxhpfortext) currenttip.add(
                    String.format(
                            LangUtil.translateG("hud.msg.health") + WHITE + "%.0f" + GRAY + " / " + WHITE + "%.0f",
                            ((EntityLivingBase) entity).getHealth(),
                            ((EntityLivingBase) entity).getMaxHealth()));

            else {
                currenttip.add(
                        getRenderString(
                                "waila.health",
                                String.valueOf(nhearts),
                                String.valueOf(health),
                                String.valueOf(maxhp)));
            }
        }
    }

    public void getEntityArmor(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
                               IWailaConfigHandler config) {
        if (!WailaConfig.showarmor.getBooleanValue()) return;

        if (entity instanceof EntityLivingBase entityLivingBase) {

            float armor = entityLivingBase.getTotalProtection(DamageSource.causeMobDamage((EntityLivingBase) null));
            if (armor <= 0) return;

            if (armor > maxArmorForText) {
                currenttip.add(
                        String.format(LangUtil.translateG("hud.msg.armor", armor))
                );
            } else {
                currenttip.add(
                        getRenderString(
                                "waila.armor",
                                String.valueOf(nArmorIconsPerLine),
                                String.valueOf(armor),
                                String.valueOf(armor)));
            }
        }
    }

    public void getEntityAttack(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
                               IWailaConfigHandler config) {
        if (!WailaConfig.showatk.getBooleanValue()) return;

        if (entity instanceof EntityLivingBase entityLivingBase) {
            float total_melee_damage = 0.0F;
            DecimalFormat damageFormat = new DecimalFormat("0.00");
            if (entityLivingBase.isEntityPlayer()) {
                total_melee_damage = Float.parseFloat(damageFormat.format(entityLivingBase.getAsPlayer().calcRawMeleeDamageVs(entityLivingBase, false, false)));
            } else if (entityLivingBase.hasEntityAttribute(SharedMonsterAttributes.attackDamage)) {
                total_melee_damage = Float.parseFloat(damageFormat.format((float) entityLivingBase.getEntityAttributeValue(SharedMonsterAttributes.attackDamage)));
            }
            if (total_melee_damage > 0.0F)
                currenttip.add(LangUtil.translateG("hud.msg.attack", total_melee_damage));
        }
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        if (!WailaConfig.showMods.getBooleanValue()) return currenttip;
        try {
            currenttip.add(BLUE + ITALIC + ModIdentification.getEntityMod(entity));
        } catch (Exception e) {
            currenttip.add(BLUE + ITALIC + "Unknown");
        }
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, Entity te, NBTTagCompound tag, World world) {
        return tag;
    }
}
