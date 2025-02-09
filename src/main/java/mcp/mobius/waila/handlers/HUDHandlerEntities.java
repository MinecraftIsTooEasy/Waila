package mcp.mobius.waila.handlers;

import static mcp.mobius.waila.api.SpecialChars.BLUE;
import static mcp.mobius.waila.api.SpecialChars.GRAY;
import static mcp.mobius.waila.api.SpecialChars.ITALIC;
import static mcp.mobius.waila.api.SpecialChars.WHITE;
import static mcp.mobius.waila.api.SpecialChars.getRenderString;

import java.util.List;

import mcp.mobius.waila.utils.ModIdentification;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.*;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;

public class HUDHandlerEntities implements IWailaEntityProvider {

    public static int nhearts = 20;
    public static float maxhpfortext = 40.0f;

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
        if (!WailaConfig.showhp.getBooleanValue()) return currenttip;

        if (entity instanceof EntityLivingBase) {

            nhearts = nhearts <= 0 ? 20 : nhearts;

            float health = ((EntityLivingBase) entity).getHealth() / 2.0f;
            float maxhp = ((EntityLivingBase) entity).getMaxHealth() / 2.0f;

            if (((EntityLivingBase) entity).getMaxHealth() > maxhpfortext) currenttip.add(
                    String.format(
                            "HP : " + WHITE + "%.0f" + GRAY + " / " + WHITE + "%.0f",
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

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
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
