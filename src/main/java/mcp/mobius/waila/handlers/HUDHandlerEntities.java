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
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import moddedmite.waila.mixin.accessor.EntityArachnidAccessor;
import moddedmite.waila.mixin.accessor.EntityLivestockAccessor;
import moddedmite.waila.mixin.accessor.EntityPhaseSpiderAccessor;
import moddedmite.waila.compat.ModCompat;
import net.minecraft.server.MinecraftServer;

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
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        try {
            currenttip.add(WHITE + entity.getEntityName());
        } catch (Exception e) {
            currenttip.add(WHITE + "Unknown");
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        this.getEntityHeath(entity, currenttip, accessor, config);
        this.getEntityArmor(entity, currenttip, accessor, config);
        this.getEntityAttack(entity, currenttip, accessor, config);
        this.getAnimalInfo(entity, currenttip, accessor, config);
        this.getLivestockInfo(entity, currenttip, accessor, config);
        this.getSpiderWebInfo(entity, currenttip, accessor, config);
        this.getITFRBEvasionInfo(entity, currenttip, accessor, config);
        this.getExtremeExchangerInfo(entity, currenttip, accessor, config);
        this.getBEXEvasionInfo(entity, currenttip, accessor, config);
        return currenttip;
    }

    public void getEntityHeath(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

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

    public void getAnimalInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!(entity instanceof EntityAnimal animal)) return;

        if (!WailaConfig.showanimal.getBooleanValue()) return;


        int growingAge = animal.getGrowingAge();

        if (growingAge < 0)
        {
            int seconds = -growingAge / 20;
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.animal.grow", seconds));
        }
        else if (growingAge > 0)
        {
            int seconds = growingAge / 20;
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.animal.breed_cooldown", seconds));
        }
    }

    public void getLivestockInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!WailaConfig.showlivestock.getBooleanValue()) return;

        if (!(entity instanceof EntityLivestock)) return;

        float food, water, freedom;

        MinecraftServer server = MinecraftServer.getServer();

        if (server != null)
        {
            Entity serverEntity = null;
            for (World w : server.worldServers)
            {
                if (w != null)
                {
                    serverEntity = w.getEntityByID(entity.entityId);

                    if (serverEntity != null) break;
                }
            }
            if (!(serverEntity instanceof EntityLivestockAccessor livestock)) return;

            food = livestock.getFood();
            water = livestock.getWater();
            freedom = livestock.getFreedom();
        }
        else
        {
            NBTTagCompound tag = accessor.getNBTData();

            if (tag == null || !tag.hasKey("WailaFood")) return;

            food = tag.getFloat("WailaFood");
            water = tag.getFloat("WailaWater");
            freedom = tag.getFloat("WailaFreedom");
        }

        if (food < 0.05F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.food.desperate"));
        }
        else if (food < 0.25F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.food.very_hungry"));
        }
        else if (food < 0.5F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.food.hungry"));
        }

        if (water < 0.05F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.water.desperate"));
        }
        else if (water < 0.25F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.water.very_thirsty"));
        }
        else if (water < 0.5F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.water.thirsty"));
        }

        if (freedom < 0.25F)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.livestock.freedom.crowded"));
        }
    }

    public void getSpiderWebInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!WailaConfig.showspiderweb.getBooleanValue()) return;

        if (!(entity instanceof EntityArachnid)) return;

        int numWebs = -1;

        MinecraftServer server = MinecraftServer.getServer();
        if (server != null)
        {
            for (World world : server.worldServers)
            {
                if (world != null)
                {
                    Entity serverEntity = world.getEntityByID(entity.entityId);

                    if (serverEntity instanceof EntityArachnidAccessor arachnid)
                    {
                        numWebs = arachnid.getNumWebs();
                        break;
                    }
                }
            }
        }
        else
        {
            NBTTagCompound tag = accessor.getNBTData();
            if (tag != null && tag.hasKey("WailaNumWebs"))
            {
                numWebs = tag.getInteger("WailaNumWebs");
            }
        }

        if (numWebs < 0) return;

        if (numWebs > 0)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.spider.web_count", numWebs));
        }
        else
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.spider.no_web"));
        }

        if (entity instanceof EntityPhaseSpider && WailaConfig.showphaseevasions.getBooleanValue())
        {
            int numEvasions = -1;

            if (server != null)
            {
                for (World w : server.worldServers)
                {
                    if (w != null)
                    {
                        Entity serverEntity = w.getEntityByID(entity.entityId);

                        if (serverEntity instanceof EntityPhaseSpiderAccessor ps)
                        {
                            numEvasions = ps.getNumEvasions();
                            break;
                        }
                    }
                }
            }
            else
            {
                NBTTagCompound tag = accessor.getNBTData();

                if (tag != null && tag.hasKey("WailaNumEvasions"))
                {
                    numEvasions = tag.getInteger("WailaNumEvasions");
                }
            }
            if (numEvasions >= 0)
            {
                currenttip.add(GRAY + LangUtil.translateG("hud.msg.phase_evasions", numEvasions));
            }
        }
    }

    public void getITFRBEvasionInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!WailaConfig.showphaseevasions.getBooleanValue()) return;

        if (!ModCompat.HAS_ITFRB) return;

        if (!ModCompat.isITFRBEvasionEntity(entity)) return;

        int numEvasions = -1;

        MinecraftServer server = MinecraftServer.getServer();

        if (server != null)
        {
            for (World world : server.worldServers)
            {
                if (world != null)
                {
                    Entity serverEntity = world.getEntityByID(entity.entityId);

                    if (serverEntity != null)
                    {
                        numEvasions = ModCompat.getITFRBEvasions(serverEntity);
                        break;
                    }
                }
            }
        }
        else
        {
            NBTTagCompound tag = accessor.getNBTData();

            if (tag != null && tag.hasKey("WailaITFRBEvasions"))
            {
                numEvasions = tag.getInteger("WailaITFRBEvasions");
            }
        }

        if (numEvasions >= 0)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.phase_evasions", numEvasions));
        }
    }

    public void getExtremeExchangerInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!WailaConfig.showphaseevasions.getBooleanValue()) return;

        if (!ModCompat.HAS_BEX) return;

        if (!ModCompat.isExchangerEntity(entity)) return;

        int numEvasions = -1;

        MinecraftServer server = MinecraftServer.getServer();

        if (server != null)
        {
            for (World w : server.worldServers)
            {
                if (w != null)
                {
                    Entity serverEntity = w.getEntityByID(entity.entityId);

                    if (serverEntity != null)
                    {
                        numEvasions = ModCompat.getExchangerEvasions(serverEntity);
                        break;
                    }
                }
            }
        }
        else
        {
            NBTTagCompound tag = accessor.getNBTData();

            if (tag != null && tag.hasKey("WailaExchangerEvasions"))
            {
                numEvasions = tag.getInteger("WailaExchangerEvasions");
            }
        }

        if (numEvasions >= 0)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.phase_evasions", numEvasions));
        }
    }

    public void getBEXEvasionInfo(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (!WailaConfig.showphaseevasions.getBooleanValue()) return;

        if (!ModCompat.HAS_BEX) return;

        if (!ModCompat.isBEXEvasionEntity(entity)) return;

        int numEvasions = -1;

        MinecraftServer server = MinecraftServer.getServer();

        if (server != null)
        {
            for (World w : server.worldServers)
            {
                if (w != null)
                {
                    Entity serverEntity = w.getEntityByID(entity.entityId);

                    if (serverEntity != null)
                    {
                        numEvasions = ModCompat.getBEXEvasions(serverEntity);
                        break;
                    }
                }
            }
        }
        else
        {
            NBTTagCompound tag = accessor.getNBTData();

            if (tag != null && tag.hasKey("WailaBEXEvasions"))
            {
                numEvasions = tag.getInteger("WailaBEXEvasions");
            }
        }

        if (numEvasions >= 0)
        {
            currenttip.add(GRAY + LangUtil.translateG("hud.msg.phase_evasions", numEvasions));
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
    public NBTTagCompound getNBTData(ServerPlayer player, Entity entity, NBTTagCompound tag, World world) {

        if (tag == null || entity == null) return tag;

        tag.setInteger("WailaEntityID", entity.entityId);

        if (entity instanceof EntityArachnidAccessor arachnid)
        {
            tag.setInteger("WailaNumWebs", arachnid.getNumWebs());
        }

        if (entity instanceof EntityPhaseSpiderAccessor ps)
        {
            tag.setInteger("WailaNumEvasions", ps.getNumEvasions());
        }

        if (entity instanceof EntityLivestockAccessor ls)
        {
            tag.setFloat("WailaFood", ls.getFood());
            tag.setFloat("WailaWater", ls.getWater());
            tag.setFloat("WailaFreedom", ls.getFreedom());
        }

        if (ModCompat.HAS_ITFRB && ModCompat.isITFRBEvasionEntity(entity))
        {
            int evasions = ModCompat.getITFRBEvasions(entity);

            if (evasions >= 0) tag.setInteger("WailaITFRBEvasions", evasions);
        }

        if (ModCompat.HAS_BEX && ModCompat.isExchangerEntity(entity))
        {
            int evasions = ModCompat.getExchangerEvasions(entity);

            if (evasions >= 0) tag.setInteger("WailaExchangerEvasions", evasions);
        }

        if (ModCompat.HAS_BEX && ModCompat.isBEXEvasionEntity(entity))
        {
            int evasions = ModCompat.getBEXEvasions(entity);

            if (evasions >= 0) tag.setInteger("WailaBEXEvasions", evasions);
        }

        return tag;
    }

    public static void register() {
        HUDHandlerEntities provider = new HUDHandlerEntities();
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityArachnid.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityPhaseSpider.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, EntityLivestock.class);

        if (ModCompat.HAS_ITFRB)
        {
            ModCompat.registerITFRBNBTProviders(provider);
        }

        if (ModCompat.HAS_BEX)
        {
            ModCompat.registerBEXOnEXTREMENBTProviders(provider);
        }
    }
}