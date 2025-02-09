package mcp.mobius.waila.addons.vanillamc;

import java.util.List;

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

    static Block mobSpawner = Block.mobSpawner;
    static Block crops = Block.crops;
    static Block melonStem = Block.melonStem;
    static Block pumpkinStem = Block.pumpkinStem;
    static Block carrot = Block.carrot;
    static Block potato = Block.potato;
    static Block lever = Block.lever;
    static Block repeaterIdle = Block.redstoneRepeaterIdle;
    static Block repeaterActv = Block.redstoneRepeaterActive;
    static Block comparatorIdl = Block.redstoneComparatorIdle;
    static Block comparatorAct = Block.redstoneComparatorActive;
    static Block redstone = Block.redstoneWire;
    static Block jukebox = Block.jukebox;
    static Block cocoa = Block.cocoaPlant;
    static Block netherwart = Block.netherStalk;
    static Block silverfish = Block.silverfish;
    static Block leave = Block.leaves;
    static Block log = Block.wood;
    static Block quartz = Block.blockNetherQuartz;
    static Block anvil = Block.anvil;
    static Block sapling = Block.sapling;
    static Block skull = Block.skull;

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();

        if (block == silverfish && config.getConfig("vanilla.silverfish")) {
            int metadata = accessor.getMetadata();
            return switch (metadata) {
                case 0 -> new ItemStack(Block.stone);
                case 1 -> new ItemStack(Block.cobblestone);
                case 2 -> new ItemStack(Block.brick);
                default -> null;
            };
        }

        if (block == redstone) {
            return new ItemStack(Item.redstone);
        }

        if (block instanceof BlockRedstoneOre) {
            return new ItemStack(Block.oreRedstone);
        }

        if (block == crops) {
            return new ItemStack(Item.wheat);
        }

        if (block == carrot) {
            return new ItemStack(Item.carrot);
        }

        if (block == potato) {
            return new ItemStack(Item.potato);
        }

        if ((block == leave) && (accessor.getMetadata() > 3)) {
            return new ItemStack(block, 1, accessor.getMetadata() - 4);
        }

        if (block == log) {
            return new ItemStack(block, 1, accessor.getMetadata() % 4);
        }

        if ((block == quartz) && (accessor.getMetadata() > 2)) {
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

        /* Mob spawner handler */
        if (block == mobSpawner && accessor.getTileEntity() instanceof TileEntityMobSpawner
                && config.getConfig("vanilla.spawntype")) {
            String name = currenttip.get(0);
            String mobname = ((TileEntityMobSpawner) accessor.getTileEntity()).getSpawnerLogic().getEntityNameToSpawn();
            currenttip.set(0, String.format("%s (%s)", name, mobname));
        }

        if (block == redstone) {
            String name = currenttip.get(0).replaceFirst(String.format(" %s", accessor.getMetadata()), "");
            currenttip.set(0, name);
        }

        if (block == melonStem) {
            currenttip.set(0, SpecialChars.WHITE + I18n.getString("tile.melon_stem.name"));
        }

        if (block == pumpkinStem) {
            currenttip.set(0, SpecialChars.WHITE + I18n.getString("tile.pumpkin_stem.name"));
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        String skull2;
        /* Crops */
        boolean iscrop = crops.getClass().isInstance(block); // Done to cover all inheriting mods
        if (WailaConfig.showcrop.getBooleanValue())
            if (iscrop || block == melonStem || block == pumpkinStem || block == carrot || block == potato) {
                float growthValue = (accessor.getMetadata() / 7.0F) * 100.0F;
                if (growthValue < 100.0)
                    currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthValue));
                else currenttip.add(
                        String.format(
                                "%s : %s",
                                LangUtil.translateG("hud.msg.growth"),
                                LangUtil.translateG("hud.msg.mature")));
                return currenttip;
            }

        if (block == cocoa && WailaConfig.showcrop.getBooleanValue()) {

            float growthValue = ((accessor.getMetadata() >> 2) / 2.0F) * 100.0F;
            if (growthValue < 100.0)
                currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthValue));
            else currenttip.add(
                    String.format(
                            "%s : %s",
                            LangUtil.translateG("hud.msg.growth"),
                            LangUtil.translateG("hud.msg.mature")));
            return currenttip;
        }

        if (block == netherwart && WailaConfig.showcrop.getBooleanValue()) {
            float growthValue = (accessor.getMetadata() / 3.0F) * 100.0F;
            if (growthValue < 100.0)
                currenttip.add(String.format("%s : %.0f %%", LangUtil.translateG("hud.msg.growth"), growthValue));
            else currenttip.add(
                    String.format(
                            "%s : %s",
                            LangUtil.translateG("hud.msg.growth"),
                            LangUtil.translateG("hud.msg.mature")));
            return currenttip;
        }

        if (WailaConfig.leverstate.getBooleanValue()) if (block == lever) {
            String redstoneOn = (accessor.getMetadata() & 8) == 0 ? LangUtil.translateG("hud.msg.off")
                    : LangUtil.translateG("hud.msg.on");
            currenttip.add(String.format("%s : %s", LangUtil.translateG("hud.msg.state"), redstoneOn));
            return currenttip;
        }

        if (WailaConfig.repeater.getBooleanValue()) if ((block == repeaterIdle) || (block == repeaterActv)) {
            int tick = (accessor.getMetadata() >> 2) + 1;
            if (tick == 1) currenttip.add(String.format("%s : %s tick", LangUtil.translateG("hud.msg.delay"), tick));
            else currenttip.add(String.format("%s : %s ticks", LangUtil.translateG("hud.msg.delay"), tick));
            return currenttip;
        }

        if (WailaConfig.comparator.getBooleanValue()) if ((block == comparatorIdl) || (block == comparatorAct)) {
            String mode = ((accessor.getMetadata() >> 2) & 1) == 0 ? LangUtil.translateG("hud.msg.comparator")
                    : LangUtil.translateG("hud.msg.substractor");
            currenttip.add("Mode : " + mode);
            return currenttip;
        }

        if (WailaConfig.redstone.getBooleanValue()) if (block == redstone) {
            currenttip.add(String.format("%s : %s", LangUtil.translateG("hud.msg.power"), accessor.getMetadata()));
            return currenttip;
        }

        if (WailaConfig.spawnertype.getBooleanValue() && block == mobSpawner && (accessor.getTileEntity() instanceof TileEntityMobSpawner)) {
            currenttip.add(String.format("Type: %s", ((TileEntityMobSpawner) accessor.getTileEntity()).getSpawnerLogic().getEntityNameToSpawn()));
        }

        if (WailaConfig.skulltype.getBooleanValue() && block == skull && (accessor.getTileEntity() instanceof TileEntitySkull) && Waila.instance.serverPresent) {
            NBTTagCompound tag = accessor.getNBTData();
            byte type = tag.getByte("SkullType");
            skull2 = switch (type) {
                case 0 -> StatCollector.translateToLocal("item.skull.skeleton.name");
                case 1 -> StatCollector.translateToLocal("item.skull.wither.name");
                case 2 -> StatCollector.translateToLocal("item.skull.zombie.name");
                case 3 -> String.format(StatCollector.translateToLocal("item.skull.player.name"), tag.getString("ExtraType"));
                case 4 -> StatCollector.translateToLocal("item.skull.creeper.name");
                case 5 -> StatCollector.translateToLocal("item.skull.infused.name");
                default -> skull.unlocalizedName;
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

        ModuleRegistrar.instance().registerStackProvider(provider, silverfish.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, redstone.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, BlockRedstoneOre.class);
        ModuleRegistrar.instance().registerStackProvider(provider, crops.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, leave.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, log.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, quartz.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, anvil.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, sapling.getClass());
        ModuleRegistrar.instance().registerStackProvider(provider, BlockSlab.class);

        ModuleRegistrar.instance().registerHeadProvider(provider, mobSpawner.getClass());
        ModuleRegistrar.instance().registerHeadProvider(provider, melonStem.getClass());
        ModuleRegistrar.instance().registerHeadProvider(provider, pumpkinStem.getClass());

        ModuleRegistrar.instance().registerBodyProvider(provider, crops.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, melonStem.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, pumpkinStem.getClass());

        ModuleRegistrar.instance().registerBodyProvider(provider, lever.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, repeaterIdle.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, repeaterActv.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, comparatorIdl.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, comparatorAct.getClass());
        ModuleRegistrar.instance().registerHeadProvider(provider, redstone.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, redstone.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, jukebox.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, cocoa.getClass());
        ModuleRegistrar.instance().registerBodyProvider(provider, netherwart.getClass());

        ModuleRegistrar.instance().registerNBTProvider(provider, mobSpawner.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, crops.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, melonStem.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, pumpkinStem.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, carrot.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, potato.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, lever.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, repeaterIdle.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, repeaterActv.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, comparatorIdl.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, comparatorAct.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, redstone.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, jukebox.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, cocoa.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, netherwart.getClass());
        ModuleRegistrar.instance().registerNBTProvider(provider, silverfish.getClass());
    }

}
