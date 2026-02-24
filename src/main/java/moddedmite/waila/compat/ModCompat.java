package moddedmite.waila.compat;

import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.Entity;
import cn.wensc.mitemod.extreme.entity.EntityExchanger;
import moddedmite.waila.mixin.compat.EntityLichAccessor;
import moddedmite.waila.mixin.compat.EntityLichShadowAccessor;
import moddedmite.waila.mixin.compat.EntitySpiritAccessor;
import moddedmite.waila.mixin.compat.EntitySkeletonBossAccessor;
import moddedmite.waila.mixin.compat.EntitySkeletonShadowAccessor;
import moddedmite.waila.mixin.compat.EntityUltimateAnnihilationSkeletonAccessor;
import net.oilcake.mitelros.entity.boss.EntityLich;
import net.oilcake.mitelros.entity.mob.EntityLichShadow;
import net.oilcake.mitelros.entity.mob.EntitySpirit;
import net.moddedmite.mitemod.bex.entity.EntitySkeletonBoss;
import net.moddedmite.mitemod.bex.entity.EntitySkeletonShadow;
import net.moddedmite.mitemod.bex.entity.EntityUltimateAnnihilationSkeleton;
import net.xiaoyu233.fml.FishModLoader;

public class ModCompat {

    public static final boolean HAS_BEX = FishModLoader.hasMod("bex");
    public static final boolean HAS_ITFRB = FishModLoader.hasMod("mite-itf-reborn");

    public static int getITFRBEvasions(Entity entity) {

        if (entity instanceof EntityLichAccessor lich)
        {
            return lich.getNumEvasions();
        }

        if (entity instanceof EntityLichShadowAccessor lichshadow)
        {
            return lichshadow.getNumEvasions();
        }

        if (entity instanceof EntitySpiritAccessor spirit)
        {
            return spirit.getNumEvasions();
        }

        return -1;
    }

    public static boolean isITFRBEvasionEntity(Entity entity)
    {
        return entity instanceof EntityLichAccessor || entity instanceof EntityLichShadowAccessor || entity instanceof EntitySpiritAccessor;
    }

    public static int getExchangerEvasions(Entity entity)
    {
        if (entity instanceof IExchangerEvasions exchanger)
        {
            return exchanger.getNumEvasions();
        }
        return -1;
    }

    public static boolean isExchangerEntity(Entity entity)
    {
        return entity instanceof IExchangerEvasions;
    }

    public static int getBEXEvasions(Entity entity)
    {
        if (entity instanceof EntitySkeletonBossAccessor boss)
        {
            return boss.getNumEvasions();
        }
        if (entity instanceof EntitySkeletonShadowAccessor shadow)
        {
            return shadow.getNumEvasions();
        }
        if (entity instanceof EntityUltimateAnnihilationSkeletonAccessor uas)
        {
            return uas.getNumEvasions();
        }
        return -1;
    }

    public static boolean isBEXEvasionEntity(Entity entity)
    {
        return entity instanceof EntitySkeletonBossAccessor
                || entity instanceof EntitySkeletonShadowAccessor
                || entity instanceof EntityUltimateAnnihilationSkeletonAccessor;
    }

    public static void registerITFRBNBTProviders(IWailaEntityProvider provider) {
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityLich.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityLichShadow.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntitySpirit.class);
    }

    public static void registerBEXOnEXTREMENBTProviders(IWailaEntityProvider provider) {
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityExchanger.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntitySkeletonBoss.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntitySkeletonShadow.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityUltimateAnnihilationSkeleton.class);
    }
}