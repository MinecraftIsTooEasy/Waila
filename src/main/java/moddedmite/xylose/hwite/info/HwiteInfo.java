package moddedmite.xylose.hwite.info;

import dev.emi.emi.api.stack.EmiStack;
import moddedmite.xylose.hwite.config.HwiteConfigs;
import net.minecraft.*;
import net.xiaoyu233.fml.api.block.IBlock;
import net.xiaoyu233.fml.api.entity.IEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class HwiteInfo extends Gui {
    private static final Logger LOGGER = LogManager.getLogger(HwiteInfo.class);
    static Minecraft mc = Minecraft.getMinecraft();
    static EntityPlayer player = mc.thePlayer;
    public static EnumChatFormatting gray = EnumChatFormatting.GRAY;
    public static String infoMain;
    public static String info_line_1 = "";
    public static String info_line_2 = "";
    public static String break_info = "";
    public static Entity entityInfo;
    public static Block blockInfo;
    public static ItemStack itemStackInfo;
    public static String unlocalizedNameInfo = "";
    public static int blockPosX = 0;
    public static int blockPosY = 0;
    public static int blockPosZ = 0;
    public static boolean hasIcon;
    public static boolean renderHealth = false;

    static int mobSpawnerID = Block.mobSpawner.blockID;
    static int wheatCropID = Block.crops.blockID;
    static int melonStemID = Block.melonStem.blockID;
    static int pumpkinStemID = Block.pumpkinStem.blockID;
    static int carrotCropID = Block.carrot.blockID;
    static int potatoID = Block.potato.blockID;
    static int onionsID = Block.onions.blockID;
    static int netherStalkID = Block.netherStalk.blockID;
    static int leverID = Block.lever.blockID;
    static int repeaterIdle = Block.redstoneRepeaterIdle.blockID;
    static int repeaterActv = Block.redstoneRepeaterActive.blockID;
    static int comparatorIdl = Block.redstoneComparatorIdle.blockID;
    static int comparatorAct = Block.redstoneComparatorActive.blockID;
    static int redstone = Block.redstoneWire.blockID;
    static int skull = Block.skull.blockID;

    public static void updateInfoForRC(RaycastCollision rc, EntityPlayer player) {
        try {
            updateInfoForRCInternal(rc, player);
        } catch (Exception e) {
            LOGGER.warn("exception while updating info", e);
        }
    }

    private static void updateInfoForRCInternal(RaycastCollision rc, EntityPlayer player) {
        if (rc == null) {
            infoMain = "";
            info_line_1 = "";
            info_line_2 = "";
            break_info = "";
            unlocalizedNameInfo = "";
            return;
        }
        entityInfo = null;
        if (rc.isEntity()) {
            if (HwiteConfigs.DisplayEntity.getBooleanValue())
                updateRCEntity(rc);
            updateDevInfoInfo(rc, player);
            updateHiwlaExtraInfo(rc, player);
        } else if (rc.isBlock()) {
            if (HwiteConfigs.DisplayBlock.getBooleanValue()) {
                updateRCBlock(rc, player);
                updateGrowthInfo(rc, player);
                updateRedStoneInfo(rc, player);
                updateMobSpawnerInfo(rc, player);
            }
            updateDevInfoInfo(rc, player);
        } else {
            info_line_1 = "";
            info_line_2 = "";
            unlocalizedNameInfo = "";
        }
    }

    /**
     * Already checked rc type is block
     */
    private static void updateRCBlock(RaycastCollision rc, EntityPlayer player) {
        blockPosX = rc.block_hit_x;
        blockPosY = rc.block_hit_y;
        blockPosZ = rc.block_hit_z;
        updateBlockInfo(rc, player);
        if (HwiteConfigs.BlockRender.getBooleanValue())
            hasIcon = true;
        renderHealth = false;
    }

    /**
     * Already checked rc is entity
     */
    private static void updateRCEntity(RaycastCollision rc) {
        Entity entity = rc.getEntityHit();
        if (entity instanceof EntityLivingBase entityLivingBase) {
            updateEntityLivingBaseInfo(entityLivingBase);
        } else {
            if (HwiteConfigs.ShowIDAndMetadata.getBooleanValue()) {
                infoMain = entity.getTranslatedEntityName() + " (" + EntityList.getEntityID(entity) + ")";
            } else {
                infoMain = entity.getTranslatedEntityName();
            }
            info_line_1 = " ";
            info_line_2 = " ";
            break_info = " ";
            unlocalizedNameInfo = "";
        }
        hasIcon = false;
    }

    public static int updateEntityLivingBaseMaxHealth(RaycastCollision rc) {
        if (rc != null && rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase entityLivingBase) {
            return (int) entityLivingBase.getMaxHealth();
        }
        renderHealth = false;
        return 0;
    }


    private static void updateEntityLivingBaseInfo(EntityLivingBase entityLivingBase) {
        entityInfo = entityLivingBase;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        float total_melee_damage;
        if (entityLivingBase.isEntityPlayer()) {
            total_melee_damage = Float.parseFloat(decimalFormat.format(entityLivingBase.getAsPlayer().calcRawMeleeDamageVs(entityLivingBase, false, false)));
        } else if (entityLivingBase.hasEntityAttribute(SharedMonsterAttributes.attackDamage)) {
            total_melee_damage = Float.parseFloat(decimalFormat.format((float) entityLivingBase.getEntityAttributeValue(SharedMonsterAttributes.attackDamage)));
        } else {
            total_melee_damage = 0.0F;
        }
        if (HwiteConfigs.ShowIDAndMetadata.getBooleanValue()) {
            infoMain = entityLivingBase.getEntityName() + " (" + EntityList.getEntityID(entityLivingBase) + ")";
        } else {
            infoMain = entityLivingBase.getEntityName();
        }

        if (!renderHealth) {
            if (entityLivingBase.getMaxHealth() > 20) {
                info_line_1 = gray + I18n.getString("hwite.info.health") + (int) entityLivingBase.getHealth() + "/" + (int) entityLivingBase.getMaxHealth();
                info_line_2 = " ";
                break_info = "  ";
                unlocalizedNameInfo = "";
                renderHealth = false;
            }
        }
    }

    /**
     * Already checked rc type is block
     */
    private static void updateBlockInfo(RaycastCollision rc, EntityPlayer player) {
        Block block = rc.getBlockHit();
        if (block != null) {
            int metadata = rc.block_hit_metadata;
            blockInfo = block;
            itemStackInfo = block.createStackedBlock(metadata);
            info_line_1 = "";
            info_line_2 = "";
            unlocalizedNameInfo = "";
            updateBreakInfo(rc, player);
            updateInfoMain(block.createStackedBlock(metadata), block, metadata);
        }
    }

    public static String updateInfoLine1(RaycastCollision rc, float block_hardness, EntityPlayer player) {
        String info1;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (player.getCurrentPlayerStrVsBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, true) <= 0.0) {
            return info1 = gray + I18n.getString("hwite.info.hardness") + block_hardness;
        } else {
            return info1 = gray + I18n.getString("hwite.info.hardness") + block_hardness + " " + I18n.getString("hwite.info.str_vs_block") + decimalFormat.format(player.getCurrentPlayerStrVsBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, true));
        }
    }

    public static String updateInfoLine2(RaycastCollision rc, EntityPlayer player) {
        String info2;
        if (rc != null && rc.isBlock()) {
            Block block = rc.getBlockHit();
            int metadata = rc.block_hit_metadata;
            if (player.getCurrentPlayerStrVsBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, true) > 0.0) {
                return info2 = EnumChatFormatting.DARK_GRAY + I18n.getString("hwite.info.str_vs_block") + (short) player.getCurrentPlayerStrVsBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, true);
            }
        }
        return "";
    }


    public static String updateMITEDetailsInfo(RaycastCollision rc, EntityPlayer player) {
        if (rc != null && rc.isBlock()) {
            float block_hardness = player.worldObj.getBlockHardness(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            if (HwiteConfigs.MITEDetailsInfo.getBooleanValue()) {
                return updateInfoLine1(rc, block_hardness, player);
            }
        }
        return "";
    }

    private static void updateBreakInfo(RaycastCollision rc, EntityPlayer player) {
        if (player.getCurrentPlayerStrVsBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, true) <= 0.0) {
            break_info = EnumChatFormatting.DARK_RED + HwiteConfigs.CannotBreakString.getStringValue() + EnumChatFormatting.WHITE;
        } else {
            break_info = EnumChatFormatting.DARK_GREEN + HwiteConfigs.CanBreakString.getStringValue() + EnumChatFormatting.WHITE;
        }
    }

    public static String updateGrowthInfo(RaycastCollision rc, EntityPlayer player) {
        String growthInfo;
        if (rc != null && rc.isBlock()) {
            int blockID = rc.getBlockHitID();
            int metadata = rc.block_hit_metadata;
            if (HwiteConfigs.GrowthValue.getBooleanValue()) {
                if (rc.getBlockHitID() == wheatCropID || (Block.blocksList[blockID] instanceof BlockCrops) || Block.blocksList[blockID] instanceof BlockStem || blockID == netherStalkID) {
                    int growthValue = (int) (blockID == netherStalkID ? metadata / 3.0F * 100F : (metadata & 7) / 7.0F * 100.0F);
                    if (growthValue != 100.0D) {
                        return growthInfo = gray + I18n.getString("hwite.info.growth_value") + growthValue + "%";
                    } else {
                        return growthInfo = gray + I18n.getString("hwite.info.growth_value_mature");
                    }
                }
            }
        }
        return "";
    }

    public static String updateRedStoneInfo(RaycastCollision rc, EntityPlayer player) {
        String redstoneInfo;
        if (rc != null && rc.isBlock()) {
            int blockID = rc.getBlockHitID();
            int metadata = rc.block_hit_metadata;
            if (HwiteConfigs.Redstone.getBooleanValue()) {
                if (blockID == leverID) {
                    String leverOn = ((metadata & 0x8) == 0) ? EnumChatFormatting.RED + I18n.getString("hwite.info.off") : EnumChatFormatting.GREEN + I18n.getString("hwite.info.on");
                    return redstoneInfo = gray + I18n.getString("hwite.info.state") + leverOn;
                }
                if ((Block.blocksList[blockID] instanceof BlockPressurePlate) || (Block.blocksList[blockID] instanceof BlockPressurePlateWeighted)) {
                    String plateOn = ((metadata & 1) == 0) ? EnumChatFormatting.RED + I18n.getString("hwite.info.off") : EnumChatFormatting.GREEN + I18n.getString("hwite.info.on");
                    return redstoneInfo = gray + I18n.getString("hwite.info.state") + plateOn;
                }
                if (blockID == repeaterIdle || blockID == repeaterActv) {
                    int tick = (metadata >> 2) + 1;
                    if (tick == 1) {
                        return redstoneInfo = gray + I18n.getString("hwite.info.delay") + tick + " tick";
                    } else {
                        return redstoneInfo = gray + I18n.getString("hwite.info.delay") + tick + " ticks";
                    }
                }
                if (blockID == comparatorIdl || blockID == comparatorAct) {
                    String mode = ((metadata >> 2 & 0x1) == 0) ? I18n.getString("hwite.info.comparator") : I18n.getString("hwite.info.subtractor");
                    return redstoneInfo = gray + I18n.getString("hwite.info.mode") + mode;
                }
                if (blockID == redstone) {
                    return redstoneInfo = gray + I18n.getString("hwite.info.power") + metadata;
                }
            }
        }
        return "";
    }

    public static String updateMobSpawnerInfo(RaycastCollision rc, EntityPlayer player) {
        String spawnerInfo;
        if (rc != null && rc.isBlock()) {
            int blockID = rc.getBlockHitID();
            TileEntity tileEntity = player.worldObj.getBlockTileEntity(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            if (HwiteConfigs.SpawnerType.getBooleanValue() && blockID == mobSpawnerID && tileEntity instanceof TileEntityMobSpawner) {
                return spawnerInfo = gray + I18n.getString("hwite.info.type") + (((TileEntityMobSpawner) tileEntity).getSpawnerLogic().getEntityNameToSpawn());
            }
        }
        return "";
    }

    public static String updateDevInfoInfo(RaycastCollision rc, EntityPlayer player) {
        String devInfo;
        String unlocalizedNameInfo;
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if (rc != null) {
            double distance = Double.parseDouble(decimalFormat.format(rc.getDistanceFromOriginToCollisionPoint()));
            if (rc.isBlock()) {
                String direction;
                try {
                    direction = rc.getBlockHit().getDirectionFacing(rc.block_hit_metadata).toString();
                } catch (Exception e) {
                    LOGGER.warn("illegal metadata {} for block {} with id {}", rc.block_hit_metadata, rc.getBlockHit(), rc.getBlockHit().blockID);
                    direction = "UNKNOWN";
                }
                if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue() && HwiteConfigs.ShowDistance.getBooleanValue() && HwiteConfigs.ShowDirection.getBooleanValue() && direction != null) {
                    return devInfo = gray + String.valueOf(rc.block_hit_x) + " " + rc.block_hit_y + " " + rc.block_hit_z + " [" + distance + "]" + " {" + direction + "}";
                } else if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue() && HwiteConfigs.ShowDistance.getBooleanValue()) {
                    return devInfo = gray + String.valueOf(rc.block_hit_x) + " " + rc.block_hit_y + " " + rc.block_hit_z + " [" + distance + "]";
                } else if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue() && HwiteConfigs.ShowDirection.getBooleanValue() && direction != null) {
                    return devInfo = gray + String.valueOf(rc.block_hit_x) + " " + rc.block_hit_y + " " + rc.block_hit_z + " {" + direction + "}";
                } else if (HwiteConfigs.ShowDistance.getBooleanValue() && HwiteConfigs.ShowDirection.getBooleanValue() && direction != null) {
                    return devInfo = gray + "[" + distance + "]" + " {" + direction + "}";
                } else if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue()) {
                    return devInfo = gray + String.valueOf(rc.block_hit_x) + " " + rc.block_hit_y + " " + rc.block_hit_z;
                } else if (HwiteConfigs.ShowDistance.getBooleanValue()) {
                    return devInfo = gray + "[" + distance + "]";
                } else if (HwiteConfigs.ShowDirection.getBooleanValue() && direction != null) {
                    return devInfo = gray + "{" + direction + "}";
                }
                if (HwiteConfigs.ShowBlockUnlocalizedName.getBooleanValue()) {
                    return unlocalizedNameInfo = gray + rc.getBlockHit().getUnlocalizedName();
                }
            } else if (rc.isEntity()) {
                if (rc.getEntityHit() instanceof EntityLivingBase) {
                    if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue() && HwiteConfigs.ShowDistance.getBooleanValue()) {
                        return devInfo = gray + String.valueOf((int) rc.getEntityHit().posX) + " " + (int) rc.getEntityHit().posY + " " + (int) rc.getEntityHit().posZ + " [" + distance + "]";
                    } else if (HwiteConfigs.ShowBlockOrEntityCoord.getBooleanValue()) {
                        return devInfo = gray + String.valueOf((int) rc.getEntityHit().posX) + " " + (int) rc.getEntityHit().posY + " " + (int) rc.getEntityHit().posZ;
                    } else if (HwiteConfigs.ShowDistance.getBooleanValue()) {
                        return devInfo = gray + "[" + distance + "]";
                    }
                }
            }
        }
        return "";
    }


    public static String updateFurnaceInputItemInfo(RaycastCollision rc) {
        String inputItem;
        if (rc != null && rc.isBlock()) {
            TileEntity tileEntity = rc.world.getBlockTileEntity(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            if (rc.getBlockHit() instanceof BlockFurnace && tileEntity instanceof TileEntityFurnace tileEntityFurnace && tileEntityFurnace.getInputItemStack() != null && GuiScreen.isShiftKeyDown()) {
                return inputItem = gray + I18n.getString("hiwla.info.furnace.input") + tileEntityFurnace.getInputItemStack().getDisplayName() + "x" + tileEntityFurnace.getInputItemStack().stackSize;
            }
        }
        return "";
    }

    public static String updateFurnaceOutputItemInfo(RaycastCollision rc) {
        String outputItem;
        if (rc != null && rc.isBlock()) {
            TileEntity tileEntity = rc.world.getBlockTileEntity(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            if (rc.getBlockHit() instanceof BlockFurnace && tileEntity instanceof TileEntityFurnace tileEntityFurnace && tileEntityFurnace.getOutputItemStack() != null && GuiScreen.isShiftKeyDown()) {
                return outputItem = gray + I18n.getString("hiwla.info.furnace.output") + tileEntityFurnace.getOutputItemStack().getDisplayName() + "x" + tileEntityFurnace.getOutputItemStack().stackSize;
            }
        }
        return "";
    }

    public static String updateFurnaceFuelItemInfo(RaycastCollision rc) {
        String fuelItem;
        if (rc != null && rc.isBlock()) {
            TileEntity tileEntity = rc.world.getBlockTileEntity(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            if (rc.getBlockHit() instanceof BlockFurnace && tileEntity instanceof TileEntityFurnace tileEntityFurnace && tileEntityFurnace.getFuelItemStack() != null && GuiScreen.isShiftKeyDown()) {
                return fuelItem = gray + I18n.getString("hiwla.info.furnace.fuel") + tileEntityFurnace.getFuelItemStack().getDisplayName() + "x" + tileEntityFurnace.getFuelItemStack().stackSize;
            }
        }
        return "";
    }

    public static String updateHorseInfo(RaycastCollision rc) {
        String horseInfo;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (rc != null && rc.isEntity()) {
            if (rc.getEntityHit() instanceof EntityHorse horse && HwiteConfigs.HorseInfo.getBooleanValue()) {
                return horseInfo = gray + I18n.getString("hiwla.info.horse.jump") + decimalFormat.format(1 + horse.getHorseJumpStrength()) + " " + I18n.getString("hiwla.info.horse.speed") + decimalFormat.format(horse.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            }
        }
        return "";
    }

    public static String updateBreakInfo(RaycastCollision rc) {
        String breakInfo;
        if (rc != null && rc.isBlock()) {
            int metadata = rc.block_hit_metadata;
            if (HwiteConfigs.BreakInfo.getBooleanValue()) {
                for (int i = 0; i < Item.itemsList.length; ++i) {
                    Item item = Item.getItem(i);
                    if (item instanceof ItemTool tool) {
                        if (tool.getStrVsBlock(rc.getBlockHit(), metadata) > 1.5F) {
                            if (rc.getBlockHit().getMinHarvestLevel(metadata) > 1) {
                                return breakInfo = gray + tool.getToolType() + " : " + rc.getBlockHit().getMinHarvestLevel(metadata) + " :" + " " + break_info;
                            } else {
                                return breakInfo = gray + tool.getToolType() + " :" + " " + break_info;
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String updateEffectInfo(RaycastCollision rc) {
        String effectInfo;

        if (rc != null && rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase entityLivingBase && entityLivingBase.hasActivePotionEffects()) {
            Collection potion_effects = entityLivingBase.getActivePotionEffects();
            Iterator i = potion_effects.iterator();
            PotionEffect potion_effect = (PotionEffect) i.next();
            Potion potion = Potion.get(potion_effect.getPotionID());
            int level = potion_effect.getAmplifier();
            Map attribute_modifiers = potion.func_111186_k();
            int duration = potion_effect.getDuration();
            if (duration != 0 && HwiteConfigs.EffectInfo.getBooleanValue()) {
                return effectInfo = gray + I18n.getString(potion.getName()) + " " + StringHelper.getRomanNumeral(level + 1) + " (" + StringUtils.ticksToElapsedTime(duration) + ")";
            }
        }
        return "";
    }

    public static String updateHiwlaExtraInfo(RaycastCollision rc, EntityPlayer player) {
        String hiwlaInfo;
        if (rc != null) {
            if (rc.isBlock()) {
                TileEntity tileEntity = rc.world.getBlockTileEntity(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
                int metadata = rc.block_hit_metadata;
                if (HwiteConfigs.FurnaceInfo.getBooleanValue() && rc.getBlockHit() instanceof BlockFurnace furnace && tileEntity instanceof TileEntityFurnace tileEntityFurnace && tileEntityFurnace.furnaceBurnTime != 0 && tileEntityFurnace.getFuelItemStack() != null) {
                    if (tileEntityFurnace.isBurning() && furnace.isActive)
                        return hiwlaInfo = gray + I18n.getString("hiwla.info.furnace.burn_time") + tileEntityFurnace.furnaceBurnTime / 20 + I18n.getString("hwite.info.second") + I18n.getString("hiwla.info.furnace.heat_level") + tileEntityFurnace.heat_level;
                }
                if (HwiteConfigs.BeaconLevel.getBooleanValue() && rc.getBlockHit() instanceof BlockBeacon && tileEntity instanceof TileEntityBeacon tileEntityBeacon && tileEntityBeacon.getLevels() > -1) {
                    return hiwlaInfo = gray + I18n.getString("hiwla.info.beacon.level") + tileEntityBeacon.getLevels();
                }

            } else if (rc.isEntity()) {
                if (rc.getEntityHit() instanceof EntityLivingBase living) {
                    float total_melee_damage;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    if (living.isEntityPlayer()) {
                        total_melee_damage = Float.parseFloat(decimalFormat.format(living.getAsPlayer().calcRawMeleeDamageVs(living, false, false)));
                    } else if (living.hasEntityAttribute(SharedMonsterAttributes.attackDamage)) {
                        total_melee_damage = Float.parseFloat(decimalFormat.format((float) living.getEntityAttributeValue(SharedMonsterAttributes.attackDamage)));
                    } else {
                        total_melee_damage = 0.0F;
                    }
                    if (HwiteConfigs.LivingProtectionAttack.getBooleanValue()) {
                        if (living.getTotalProtection(DamageSource.causeMobDamage((EntityLivingBase) null)) > 0 && total_melee_damage > 0.0F) {
                            return hiwlaInfo = gray + I18n.getString("hiwla.info.protection") + decimalFormat.format((living.getTotalProtection(DamageSource.causeMobDamage((EntityLivingBase) null)))) + " " + I18n.getString("hwite.info.attack") + total_melee_damage;
                        } else if (living.getTotalProtection(DamageSource.causeMobDamage((EntityLivingBase) null)) > 0) {
                            return hiwlaInfo = gray + I18n.getString("hiwla.info.protection") + decimalFormat.format((living.getTotalProtection(DamageSource.causeMobDamage((EntityLivingBase) null))));
                        } else if (total_melee_damage > 0.0F) {
                            return hiwlaInfo = gray + I18n.getString("hwite.info.attack") + total_melee_damage;
                        }

                    }
                    if (rc.getEntityHit() instanceof EntityAnimal animal) {
                        if (HwiteConfigs.AnimalGrowthTime.getBooleanValue() && animal.getGrowingAge() < 0) {
                            return hiwlaInfo = gray + I18n.getString("hiwla.info.animal_growing_time") + Math.abs(animal.getGrowingAge()) / 20;
                        }
                    }
                    if (rc.getEntityHit() instanceof EntityVillager villager) {
                        String villagerProfession = getProfession(villager);
                        if (HwiteConfigs.VillagerProfession.getBooleanValue() && villager.getProfession() > -1) {
                            return hiwlaInfo = gray + I18n.getString("hiwla.info.profession") + villagerProfession;
                        }
                    }
                }
            }
        }
        return "";
    }


    private static String getProfession(EntityVillager villager) {
        String villagerProfession = I18n.getString("hiwla.info.profession.villager");
        if (villager.getProfession() == 5) {
            villagerProfession = I18n.getString("hiwla.info.profession.villager");
        } else if (villager.getProfession() == 0) {
            villagerProfession = I18n.getString("hiwla.info.profession.farmer");
        } else if (villager.getProfession() == 1) {
            villagerProfession = I18n.getString("hiwla.info.profession.librarian");
        } else if (villager.getProfession() == 2) {
            villagerProfession = I18n.getString("hiwla.info.profession.priest");
        } else if (villager.getProfession() == 3) {
            villagerProfession = I18n.getString("hiwla.info.profession.smith");
        } else if (villager.getProfession() == 4) {
            villagerProfession = I18n.getString("hiwla.info.profession.butcher");
        }
        return villagerProfession;
    }


    private static void updateInfoMain(ItemStack itemStack, Block block, int metadata) {
        if (itemStack != null) {
            if (HwiteConfigs.ShowIDAndMetadata.getBooleanValue()) {
                infoMain = itemStack.getDisplayName() + " (" + itemStack.itemID + ":" + metadata + ")";
            } else {
                infoMain = itemStack.getDisplayName();
            }
        } else {
            if (HwiteConfigs.ShowIDAndMetadata.getBooleanValue()) {
                infoMain = block.getLocalizedName() + " (" + block.blockID + ":" + metadata + ")";
            } else {
                infoMain = block.getLocalizedName();
            }
        }
    }

    private static String updateModInfoByBlock(Block block) {
        String modInfo;
        int id = block.blockID;
        if (id < 256) {
            if (id >= 164 && id < 170 || id >= 198 || id == 95) {
                return modInfo = "§9§o" + "MITE";
            } else if (id <= 163 || id >= 170 && id <= 174) {
                return modInfo = "§9§o" + "Minecraft";
            }
        } else {
            return modInfo = "§9§o" + ((IBlock) block).getNamespace();
        }
        return modInfo = "§9§o" + ((IBlock) block).getNamespace();
    }

    private static String updateModInfoByEntity(Entity entity) {
        String modInfo;
        int id = EntityList.getEntityID(entity);
        if (id <= 100 || id == 120 || id == 200) {
            return modInfo = "§9§o" + "Minecraft";
        } else if (id >= 512 && id <= 540) {
            return modInfo = "§9§o" + "MITE";
        } else {
            return modInfo = "§9§o" + ((IEntity) entity).getNamespace();
        }
    }

    public static String updateModInfo(RaycastCollision rc) {
        if (rc != null) {
            if (rc.isEntity()) {
                Entity entity = rc.getEntityHit();
                return updateModInfoByEntity(entity);
            }
            if (rc.isBlock()) {
                Block block = rc.getBlockHit();
                return updateModInfoByBlock(block);
            }
        }
        return "§9§o" + "Minecraft";
    }

    public static EmiStack updateEmiStack() {
        RaycastCollision rc = mc.objectMouseOver;
        if (rc != null && rc.isBlock()) {
            return dev.emi.emi.api.stack.EmiStack.of(rc.getBlockHit().createStackedBlock(rc.block_hit_metadata));
        }
        return null;
    }

}
