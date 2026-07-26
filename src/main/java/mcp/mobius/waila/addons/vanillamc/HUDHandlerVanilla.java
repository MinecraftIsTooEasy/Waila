package mcp.mobius.waila.addons.vanillamc;

import java.util.List;
import java.text.DecimalFormat;

import mcp.mobius.waila.Waila;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.*;
import net.minecraft.ServerPlayer;
import net.minecraft.Block;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.cbcore.LangUtil;

public class HUDHandlerVanilla implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        if (block == null) return null;

        if (block == Block.silverfish && config.getConfig("vanilla.silverfish")) {
            int metadata = accessor.getMetadata();
            return switch (metadata) {
                case 0 -> new ItemStack(Block.stone);
                case 1 -> new ItemStack(Block.cobblestone);
                case 2 -> new ItemStack(Block.brick);
                default -> null;
            };
        }

        if (block == Block.redstoneWire) {
            return new ItemStack(Item.redstone);
        }

        if (block instanceof BlockRedstoneOre) {
            return new ItemStack(Block.oreRedstone);
        }

        if (block == Block.crops) {
            return new ItemStack(Item.wheat);
        }

        if (block == Block.carrot) {
            return new ItemStack(Item.carrot);
        }

        if (block == Block.potato) {
            return new ItemStack(Item.potato);
        }

        if (block == Block.onions) {
            return new ItemStack(Item.onion);
        }

        if ((block == Block.leaves) && (accessor.getMetadata() > 3)) {
            return new ItemStack(block, 1, accessor.getMetadata() - 4);
        }

        if (block == Block.wood) {
            return new ItemStack(block, 1, accessor.getMetadata() % 4);
        }

        if ((block == Block.blockNetherQuartz) && (accessor.getMetadata() > 2)) {
            return new ItemStack(block, 1, 2);
        }

//        if (block == anvil) {
//            return new ItemStack(block, 1, block.damageDropped(accessor.getMetadata()));
//        }
//
//        if (block == sapling) {
//            return new ItemStack(block, 1, block.damageDropped(accessor.getMetadata()));
//        }
//
//        if (block instanceof BlockStoneSlab || block instanceof BlockWoodSlab) {
//            return new ItemStack(block, 1, block.damageDropped(accessor.getMetadata()));
//        }

        return null;

    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        if (block == null) return currenttip;

        /* Mob spawner handler */
        if (block == Block.mobSpawner && accessor.getTileEntity() instanceof TileEntityMobSpawner
                && config.getConfig("vanilla.spawntype")) {
            String name = currenttip.get(0);
            String mobname = ((TileEntityMobSpawner) accessor.getTileEntity()).getSpawnerLogic().getEntityNameToSpawn();
            currenttip.set(0, String.format("%s (%s)", name, mobname));
        }

        if (block == Block.redstoneWire) {
            String name = currenttip.get(0).replaceFirst(String.format(" %s", accessor.getMetadata()), "");
            currenttip.set(0, name);
        }

        if (block == Block.melonStem) {
            currenttip.set(0, SpecialChars.WHITE + I18n.getString("tile.melon_stem.name"));
        }

        if (block == Block.pumpkinStem) {
            currenttip.set(0, SpecialChars.WHITE + I18n.getString("tile.pumpkin_stem.name"));
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        Block block = accessor.getBlock();
        if (block == null) return currenttip;

        String skull2;

        if (WailaConfig.showcrop.getBooleanValue()) {
            if (block instanceof BlockCrops cropBlock) {
                if (cropBlock.isDead()) {
                    currenttip.add(SpecialChars.GRAY + LangUtil.translateG("hud.msg.crop.dead"));
                    return currenttip;
                }

                int rawMeta = accessor.getMetadata();
                int growth       = cropBlock.getGrowth(rawMeta);
                int maxGrowth    = cropBlock.getMaxGrowth();
                float growthPct  = maxGrowth > 0 ? (growth / (float) maxGrowth) * 100.0F : 100.0F;
                boolean blighted = cropBlock.isBlighted(rawMeta);
                boolean mature   = cropBlock.isMature(rawMeta);

                if (blighted) {
                    String matStr = mature ? "(" + LangUtil.translateG("hud.msg.mature") + ")" : String.format("(%.0f %%)", growthPct);
                    currenttip.add(LangUtil.translateG("hud.msg.crop.blighted") + " " + matStr);
                } else if (mature) {
                    currenttip.add(LangUtil.translateG("hud.msg.growth") + " : " + LangUtil.translateG("hud.msg.mature"));
                } else {
                    currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthPct));
                }

                if (WailaConfig.showcropdetails.getBooleanValue() && !mature && !blighted) {
                    World world2 = accessor.getWorld();
                    RaycastCollision pos = accessor.getPosition();
                    int bx = pos.block_hit_x, by = pos.block_hit_y, bz = pos.block_hit_z;
                    Block below = world2.getBlock(bx, by - 1, bz);

                    if (below == Block.tilledField) {

                        if (!BlockFarmland.isWaterNearby(world2, bx, by - 1, bz)) {
                                currenttip.add(SpecialChars.YELLOW + LangUtil.translateG("hud.msg.crop.drying_out"));
                        }
                    }

                    int lightLevel = world2.getBlockLightValue(bx, by + 1, bz);

                    if (!cropBlock.isLightLevelSuitableForGrowth(lightLevel)) {
                        currenttip.add(SpecialChars.RED + String.format("%s (%d/%d)", LangUtil.translateG("hud.msg.crop.no_light"), lightLevel, cropBlock.getMinAllowedLightValueForGrowth()));
                    }

                    float growthRate = cropBlock.getGrowthRate(world2, bx, by, bz);

                    if (growthRate > 0.0F) {
                        currenttip.add(String.format("%s: %.2f", LangUtil.translateG("hud.msg.crop.growth_rate"), growthRate));
                    } else {
                        currenttip.add(SpecialChars.RED + LangUtil.translateG("hud.msg.crop.stopped"));
                    }

                    if (cropBlock.chanceOfBlightPerRandomTick() > 0.0F) {
                        currenttip.add(String.format("%s: %s%%", LangUtil.translateG("hud.msg.crop.blight_chance"), new DecimalFormat("0.##").format(cropBlock.chanceOfBlightPerRandomTick() * 100.0F)));
                    }
                }

                return currenttip;
            }

            if (block == Block.melonStem || block == Block.pumpkinStem) {
                int rawMeta = accessor.getMetadata();

                if (BlockStem.isDead(rawMeta)) {
                    currenttip.add(SpecialChars.GRAY + LangUtil.translateG("hud.msg.crop.dead"));
                    return currenttip;
                }

                int growth = BlockStem.getGrowth(rawMeta);
                float growthPct = (growth / 7.0F) * 100.0F;

                if (growth < 7) {
                    currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthPct));

                    if (WailaConfig.showcropdetails.getBooleanValue()) {
                        World world2 = accessor.getWorld();
                        RaycastCollision pos = accessor.getPosition();
                        int bx = pos.block_hit_x, by = pos.block_hit_y, bz = pos.block_hit_z;
                        Block below = world2.getBlock(bx, by - 1, bz);

                        if (below == Block.tilledField) {
                            int farmMeta = world2.getBlockMetadata(bx, by - 1, bz);
                            int wetness = BlockFarmland.getWetness(farmMeta);

                            if (!BlockFarmland.isWaterNearby(world2, bx, by - 1, bz)) {
                                if (wetness > 0)
                                    currenttip.add(SpecialChars.RED + LangUtil.translateG("hud.msg.crop.drying_out"));
                                else
                                    currenttip.add(SpecialChars.RED + LangUtil.translateG("hud.msg.crop.no_water"));
                            }
                        }

                        float growthRate = ((BlockStem) block).getGrowthRate(world2, bx, by, bz);

                        if (growthRate > 0.0F)
                            currenttip.add(String.format("%s: %.2f", LangUtil.translateG("hud.msg.crop.growth_rate"), growthRate));
                        else
                            currenttip.add(SpecialChars.YELLOW + LangUtil.translateG("hud.msg.crop.stopped"));
                    }
                } else {
                    World world2 = accessor.getWorld();
                    RaycastCollision pos = accessor.getPosition();
                    int bx = pos.block_hit_x, by = pos.block_hit_y, bz = pos.block_hit_z;
                    Block fruitBlock = (block == Block.melonStem) ? Block.melon : Block.pumpkin;
                    boolean hasFruit = world2.getBlockId(bx - 1, by, bz) == fruitBlock.blockID
                            || world2.getBlockId(bx + 1, by, bz) == fruitBlock.blockID
                            || world2.getBlockId(bx, by, bz - 1) == fruitBlock.blockID
                            || world2.getBlockId(bx, by, bz + 1) == fruitBlock.blockID;

                    if (hasFruit)
                        currenttip.add(LangUtil.translateG("hud.msg.growth") + " : " + LangUtil.translateG("hud.msg.mature") + " \u2713");
                    else
                        currenttip.add(LangUtil.translateG("hud.msg.growth") + " : " + LangUtil.translateG("hud.msg.mature"));
                }

                return currenttip;
            }
        }

        if (block == Block.sapling && WailaConfig.showcrop.getBooleanValue()) {
            World world2 = accessor.getWorld();
            RaycastCollision pos = accessor.getPosition();
            int bx = pos.block_hit_x, by = pos.block_hit_y, bz = pos.block_hit_z;
            int rawMeta = accessor.getMetadata();
            boolean marked = (rawMeta & 8) != 0;
            int subtype = rawMeta & 3;

            if (marked) {
                currenttip.add(LangUtil.translateG("hud.msg.sapling.stage2_3"));
            } else {
                currenttip.add(LangUtil.translateG("hud.msg.sapling.stage1_3"));
            }

            if (WailaConfig.showcropdetails.getBooleanValue()) {
                int lightLevel = world2.getBlockLightValue(bx, by + 1, bz);
                if (lightLevel < 9) {
                    currenttip.add(SpecialChars.YELLOW + String.format("%s (%d/9)", LangUtil.translateG("hud.msg.crop.no_light"), lightLevel));
                }

                BiomeGenBase biome = world2.getBiomeGenForCoords(bx, bz);
                boolean canGrow = BlockSapling.canGrowInBiome(subtype, biome);
                if (!canGrow) {
                    currenttip.add(SpecialChars.RED + LangUtil.translateG("hud.msg.sapling.wrong_biome"));
                }
            }

            return currenttip;
        }

        if (block == Block.cocoaPlant && WailaConfig.showcrop.getBooleanValue()) {
            float growthValue = ((accessor.getMetadata() >> 2) / 2.0F) * 100.0F;

            if (growthValue < 100.0)
                currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthValue));

            else
                currenttip.add(String.format("%s : %s", LangUtil.translateG("hud.msg.growth"), LangUtil.translateG("hud.msg.mature")));

            return currenttip;
        }

        if (block == Block.reed && WailaConfig.showcrop.getBooleanValue()) {
            World world2 = accessor.getWorld();
            RaycastCollision pos = accessor.getPosition();
            int bx = pos.block_hit_x, by = pos.block_hit_y, bz = pos.block_hit_z;

            int height = 1;
            int checkY = by;
            while (world2.getBlock(bx, checkY - 1, bz) == Block.reed) {
                ++height;
                --checkY;
            }

            int topY = by;
            while (world2.getBlock(bx, topY + 1, bz) == Block.reed) {
                ++topY;
            }
            int topMeta = world2.getBlockMetadata(bx, topY, bz);
            float growthPct = (topMeta / 16.0F) * 100.0F;

            currenttip.add(String.format("%s: %d/3", LangUtil.translateG("hud.msg.reed.height"), height));
            if (height < 3) {
                currenttip.add(String.format("%s: %.0f%%", LangUtil.translateG("hud.msg.reed.next_growth"), growthPct));

                if (WailaConfig.showcropdetails.getBooleanValue()) {
                    int lightLevel = world2.getBlockLightValue(bx, topY, bz);
                    if (lightLevel < 15) {
                        currenttip.add(SpecialChars.YELLOW + String.format("%s (%d/15)", LangUtil.translateG("hud.msg.crop.no_light"), lightLevel));
                    }
                }
            }

            return currenttip;
        }

        if (WailaConfig.leverstate.getBooleanValue()) if (block == Block.lever) {
            String redstoneOn = (accessor.getMetadata() & 8) == 0 ? LangUtil.translateG("hud.msg.off") : LangUtil.translateG("hud.msg.on");
            currenttip.add(String.format("%s : %s", LangUtil.translateG("hud.msg.state"), redstoneOn));

            return currenttip;
        }

        if (WailaConfig.repeater.getBooleanValue())
            if ((block == Block.redstoneRepeaterIdle) || (block == Block.redstoneRepeaterActive)) {
                int tick = (accessor.getMetadata() >> 2) + 1;

                if (tick == 1)
                    currenttip.add(String.format("%s : %s tick", LangUtil.translateG("hud.msg.delay"), tick));

                else currenttip.add(String.format("%s : %s ticks", LangUtil.translateG("hud.msg.delay"), tick));

                return currenttip;
            }

        if (WailaConfig.comparator.getBooleanValue())
            if ((block == Block.redstoneComparatorIdle) || (block == Block.redstoneComparatorActive)) {
                String mode = ((accessor.getMetadata() >> 2) & 1) == 0 ? LangUtil.translateG("hud.msg.comparator") : LangUtil.translateG("hud.msg.substractor");
                currenttip.add("Mode : " + mode);
                
                return currenttip;
            }

        if (WailaConfig.redstone.getBooleanValue()) if (block == Block.redstoneWire) {
            currenttip.add(String.format("%s : %s", LangUtil.translateG("hud.msg.power"), accessor.getMetadata()));
            
            return currenttip;
        }

        if (WailaConfig.spawnertype.getBooleanValue() && block == Block.mobSpawner && (accessor.getTileEntity() instanceof TileEntityMobSpawner)) {
            currenttip.add(String.format("Type: %s", ((TileEntityMobSpawner) accessor.getTileEntity()).getSpawnerLogic().getEntityNameToSpawn()));
        }

        if (WailaConfig.skulltype.getBooleanValue() && block == Block.skull && (accessor.getTileEntity() instanceof TileEntitySkull) && Waila.instance.serverPresent) {
            NBTTagCompound tag = accessor.getNBTData();
            byte type = tag.getByte("SkullType");

            skull2 = switch (type) {
                case 0 -> StatCollector.translateToLocal("item.skull.skeleton.name");
                case 1 -> StatCollector.translateToLocal("item.skull.wither.name");
                case 2 -> StatCollector.translateToLocal("item.skull.zombie.name");
                case 3 ->
                        String.format(StatCollector.translateToLocal("item.skull.player.name"), tag.getString("ExtraType"));
                case 4 -> StatCollector.translateToLocal("item.skull.creeper.name");
                case 5 -> StatCollector.translateToLocal("item.skull.infused.name");
                default -> Block.skull.unlocalizedName;
            };
            currenttip.add(skull2);
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
        if (te != null) te.writeToNBT(tag);
        return tag;
    }

    public static void register() {
        IWailaDataProvider provider = new HUDHandlerVanilla();

        ModuleRegistrar.instance().registerStackProvider(provider, Block.class);
        ModuleRegistrar.instance().registerHeadProvider(provider, Block.class);
        ModuleRegistrar.instance().registerBodyProvider(provider, Block.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, Block.class);
    }

}
