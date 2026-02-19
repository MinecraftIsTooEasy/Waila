package mcp.mobius.waila.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.BlockAnvil;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;
import net.minecraft.TileEntityAnvil;
import net.minecraft.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * WAILA铁砧耐久显示处理器
 * 核心功能：通过反射读取服务端铁砧TileEntity的真实耐久值，解决客户端读取不到准确耐久的问题
 * 优化点：添加日志频率限制，避免重复刷屏；兼容不同类型铁砧（铁/铜/银/金/秘银等）的耐久配置
 */
public class HUDHandlerAnvil implements IWailaDataProvider {
    // 单例实例，供WAILA注册使用
    public static final HUDHandlerAnvil INSTANCE = new HUDHandlerAnvil();
    // 日志工具，用于输出调试/错误信息
    private static final Logger LOGGER = LogManager.getLogger(HUDHandlerAnvil.class);

    // ===================== 日志频率限制模块 =====================
    // 功能：控制同一铁砧的日志输出频率，避免短时间内重复打印相同日志导致刷屏
    // 存储结构：key=铁砧坐标(x,y,z)，value=最后一次日志输出时间戳
    private static final Map<String, Long> LAST_LOG_TIME = new HashMap<>();
    // 日志冷却时间（5000毫秒=5秒）：同一铁砧5秒内仅输出一次日志
    private static final long LOG_COOLDOWN = 5000L;

    // ===================== 铁砧类型配置模块 =====================
    // 功能：定义不同类型铁砧的核心配置（方块ID、名称、最大耐久值）
    // 解决不同材质铁砧耐久值不同的适配问题
    public enum AnvilType {
        IRON(145, "铁", 396800),          // 铁砧
        COPPER(229, "铜", 198400),        // 铜砧
        SILVER(230, "银", 198400),        // 银砧
        GOLDEN(231, "金", 198400),        // 金砧
        MITHRIL(232, "秘银", 3174400),    // 秘银砧
        ADAMANTIUM(233, "艾德曼", 12697600), // 艾德曼砧
        ANCIENT_METAL(254, "远古金属", 793600); // 远古金属砧

        private final int blockId;         // 铁砧对应的方块ID
        private final String name;         // 铁砧名称（用于显示）
        private final int maxDurability;   // 该类型铁砧的最大耐久值

        /**
         * 铁砧类型构造方法
         * @param blockId 方块ID
         * @param name 显示名称
         * @param maxDurability 最大耐久值
         */
        AnvilType(int blockId, String name, int maxDurability) {
            this.blockId = blockId;
            this.name = name;
            this.maxDurability = maxDurability;
        }

        // 获取方块ID
        public int getBlockId() {
            return blockId;
        }

        // 获取显示名称
        public String getName() {
            return name;
        }

        // 获取最大耐久值
        public int getMaxDurability() {
            return maxDurability;
        }

        /**
         * 根据方块ID匹配铁砧类型
         * @param blockId 目标方块ID
         * @return 匹配的铁砧类型，无匹配则返回默认铁砧(IRON)
         */
        public static AnvilType getByBlockId(int blockId) {
            for (AnvilType type : values()) {
                if (type.blockId == blockId) {
                    return type;
                }
            }
            LOGGER.warn("未知的铁砧方块ID: " + blockId + "，默认使用铁砧配置");
            return IRON;
        }
    }

    // ===================== 服务端耐久读取核心模块 =====================
    // 功能：通过反射获取服务端铁砧TileEntity，读取真实的耐久损伤值
    // 解决客户端TE与服务端TE数据不一致的问题
    private int getServerSideAnvilDamage(int x, int y, int z) {
        int rawDamage = 0;
        try {
            // 1. 获取服务端世界实例
            Object world = getServerWorld();
            if (world == null) {
                LOGGER.warn("无法获取服务端世界，降级到客户端TE");
                return 0;
            }

            // 2. 反射获取服务端已加载的TileEntity列表（兼容不同字段名）
            Object tileEntityList = null;
            try {
                tileEntityList = world.getClass().getField("loadedTileEntityList").get(world);
            } catch (Exception e) {
                tileEntityList = world.getClass().getField("loadedTileEntities").get(world);
            }
            if (tileEntityList == null || !(tileEntityList instanceof Iterable)) {
                LOGGER.warn("无法获取服务端TE列表");
                return 0;
            }

            // 3. 线程安全地拷贝TE列表，避免遍历过程中列表修改导致异常
            List<Object> teList = new ArrayList<>();
            synchronized (tileEntityList) {
                Iterator<?> iterator = ((Iterable<?>) tileEntityList).iterator();
                while (iterator.hasNext()) {
                    teList.add(iterator.next());
                }
            }

            // 4. 获取世界的getBlockId方法（用于验证铁砧类型）
            Method getBlockIdMethod = world.getClass().getMethod("getBlockId", int.class, int.class, int.class);

            // 5. 遍历服务端TE，匹配目标坐标的铁砧并读取耐久
            for (Object te : teList) {
                if (te == null || !te.getClass().getName().contains("TileEntityAnvil")) continue;

                // 获取TE的坐标（兼容不同字段名：xCoord/x、yCoord/y、zCoord/z）
                int teX = getTEField(te, "xCoord", "x");
                int teY = getTEField(te, "yCoord", "y");
                int teZ = getTEField(te, "zCoord", "z");

                // 匹配目标铁砧坐标
                if (teX == x && teY == y && teZ == z) {
                    String anvilKey = x + "," + y + "," + z;
                    // 反射读取耐久损伤值（兼容不同字段名：damage、field_70343_b、anvilDamage）
                    try {
                        rawDamage = te.getClass().getField("damage").getInt(te);
                        if (canLog(anvilKey)) {
                            LOGGER.info("从服务端TE的damage字段读取到耐久损伤值: " + rawDamage);
                        }
                    } catch (Exception e) {
                        try {
                            rawDamage = te.getClass().getField("field_70343_b").getInt(te);
                            if (canLog(anvilKey)) {
                                LOGGER.info("从服务端TE的field_70343_b字段读取到耐久损伤值: " + rawDamage);
                            }
                        } catch (Exception e2) {
                            rawDamage = te.getClass().getField("anvilDamage").getInt(te);
                            if (canLog(anvilKey)) {
                                LOGGER.info("从服务端TE的anvilDamage字段读取到耐久损伤值: " + rawDamage);
                            }
                        }
                    }
                    break; // 找到目标铁砧，退出循环
                }
            }
        } catch (Exception e) {
            LOGGER.error("读取服务端铁砧耐久失败", e);
        }
        return rawDamage;
    }

    // ===================== 日志频率控制工具方法 =====================
    // 功能：检查指定铁砧是否满足日志输出条件（冷却时间已过）
    // @param anvilKey 铁砧坐标键(x,y,z)
    // @return true=可输出日志，false=不可输出
    private boolean canLog(String anvilKey) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = LAST_LOG_TIME.get(anvilKey);

        // 首次记录或冷却时间已过，允许输出日志并更新时间戳
        if (lastTime == null || currentTime - lastTime > LOG_COOLDOWN) {
            LAST_LOG_TIME.put(anvilKey, currentTime);
            return true;
        }
        return false;
    }

    // ===================== 服务端世界获取工具模块 =====================
    // 功能：通过反射获取服务端世界实例（兼容独立服务器/集成服务器两种场景）
    // @return 服务端World实例，获取失败则返回null
    private Object getServerWorld() {
        try {
            // 场景1：独立服务器（MinecraftServer）
            Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            Object server = minecraftServerClass.getMethod("getServer").invoke(null);
            if (server != null) {
                Object[] worlds = (Object[]) minecraftServerClass.getField("worldServers").get(server);
                if (worlds != null && worlds.length > 0) {
                    return worlds[0];
                }
            } else {
                // 场景2：集成服务器（单机游戏）
                Class<?> minecraftClass = Class.forName("net.minecraft.Minecraft");
                Object minecraft = minecraftClass.getMethod("getMinecraft").invoke(null);
                if (minecraft != null) {
                    Object integratedServer = minecraftClass.getMethod("getIntegratedServer").invoke(minecraft);
                    if (integratedServer != null) {
                        Object[] worlds = (Object[]) integratedServer.getClass().getField("worldServers").get(integratedServer);
                        if (worlds != null && worlds.length > 0) {
                            return worlds[0];
                        }
                    }
                    // 降级方案：直接获取客户端世界（避免空指针）
                    return minecraftClass.getField("theWorld").get(minecraft);
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取服务端世界失败", e);
        }
        return null;
    }

    // ===================== TE字段读取工具方法 =====================
    // 功能：反射读取TileEntity的坐标字段（兼容不同字段名）
    // @param te 目标TileEntity实例
    // @param fieldName1 主字段名（如xCoord）
    // @param fieldName2 备用字段名（如x）
    // @return 字段值，读取失败则返回0
    private int getTEField(Object te, String fieldName1, String fieldName2) {
        try {
            return te.getClass().getField(fieldName1).getInt(te);
        } catch (Exception e) {
            try {
                return te.getClass().getField(fieldName2).getInt(te);
            } catch (Exception e2) {
                return 0;
            }
        }
    }

    // ===================== WAILA核心接口实现 =====================
    // 功能：实现WAILA的getWailaBody接口，向铁砧的悬浮提示中添加耐久信息
    // @param itemStack 物品栈（铁砧方块）
    // @param currenttip 原始提示列表
    // @param accessor WAILA数据访问器（提供TE、坐标等信息）
    // @param config WAILA配置处理器
    // @return 追加了耐久信息的提示列表
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        // 仅处理铁砧方块，非铁砧直接返回原始提示
        if (!(accessor.getBlock() instanceof BlockAnvil)) {
            return currenttip;
        }

        TileEntity clientTE = accessor.getTileEntity();
        if (!(clientTE instanceof TileEntityAnvil)) {
            return currenttip;
        }

        try {
            // 1. 获取铁砧坐标（用于匹配服务端TE）
            int x = getTEField(clientTE, "xCoord", "x");
            int y = getTEField(clientTE, "yCoord", "y");
            int z = getTEField(clientTE, "zCoord", "z");
            String anvilKey = x + "," + y + "," + z;

            // 2. 获取服务端铁砧的方块ID（用于匹配铁砧类型）
            Object serverWorld = getServerWorld();
            int blockId = 145; // 默认铁砧ID
            if (serverWorld != null) {
                Method getBlockIdMethod = serverWorld.getClass().getMethod("getBlockId", int.class, int.class, int.class);
                blockId = (Integer) getBlockIdMethod.invoke(serverWorld, x, y, z);
            }

            // 3. 匹配铁砧类型，获取最大耐久值
            AnvilType anvilType = AnvilType.getByBlockId(blockId);
            int maxDurability = anvilType.getMaxDurability();

            // 4. 核心逻辑：读取服务端真实耐久损伤值，计算剩余耐久
            int rawDamage = getServerSideAnvilDamage(x, y, z);
            rawDamage = Math.max(0, Math.min(rawDamage, maxDurability)); // 防止数值越界
            int remainingDurability = maxDurability - rawDamage;

            // 5. 计算铁砧损坏状态，生成描述文本
            float damagePercent = (float) rawDamage / maxDurability;
            String damageDesc = getAnvilDamageDesc(damagePercent);

            // 6. 向WAILA提示中添加耐久信息（仅保留纯耐久，移除次数相关）
            currenttip.add(String.format("%s铁砧 耐久剩余: %d/%d",
                    anvilType.getName(), remainingDurability, maxDurability));
            currenttip.add("状态: " + damageDesc);

            // 7. 低耐久警告提示（仅基于耐久值，移除次数相关描述）
            int lowDurabilityThreshold = maxDurability / 100 * 5;
            if (lowDurabilityThreshold < 1) lowDurabilityThreshold = 1;
            if (remainingDurability <= lowDurabilityThreshold && remainingDurability > 0) {
                currenttip.add("⚠️ 警告：耐久过低！剩余" + remainingDurability);
            } else if (remainingDurability <= 0) {
                currenttip.add("❌ 铁砧已损坏！");
            }

            // 8. 日志输出（带频率限制，仅记录耐久相关）
            if (canLog(anvilKey)) {
                LOGGER.info("[WAILA] 铁砧耐久读取成功: " + anvilType.getName() + " (" + x + "," + y + "," + z + ") - 剩余耐久" + remainingDurability);
            }

        } catch (Exception e) {
            LOGGER.error("[WAILA] 读取铁砧耐久失败", e);
            currenttip.add("铁砧耐久：读取失败");
        }

        return currenttip;
    }

    // ===================== 损坏状态描述工具方法 =====================
    // 功能：根据耐久损伤百分比，返回对应的损坏状态描述
    // @param damagePercent 损伤百分比（0.0~1.0）
    // @return 损坏状态文本
    private String getAnvilDamageDesc(float damagePercent) {
        if (damagePercent < 0.25F) {
            return "完整 (Intact)";
        } else if (damagePercent < 0.5F) {
            return "轻微损坏 (Slightly Damaged)";
        } else if (damagePercent < 0.75F) {
            return "严重损坏 (Very Damaged)";
        } else {
            return "即将销毁 (Almost Broken)";
        }
    }

    // ===================== WAILA接口默认实现 =====================
    // 功能：实现WAILA接口的默认方法，无自定义逻辑，返回原始数据
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
        return tag;
    }

    // ===================== WAILA注册模块 =====================
    // 功能：将当前处理器注册到WAILA框架，使铁砧方块显示自定义耐久提示
    public static void register() {
        try {
            ModuleRegistrar registrar = ModuleRegistrar.instance();
            registrar.registerBodyProvider(INSTANCE, BlockAnvil.class);
            LOGGER.info("[WAILA] 铁砧耐久提供者注册成功！");
        } catch (Exception e) {
            LOGGER.error("[WAILA] 铁砧耐久提供者注册失败", e);
            e.printStackTrace();
        }
    }
}