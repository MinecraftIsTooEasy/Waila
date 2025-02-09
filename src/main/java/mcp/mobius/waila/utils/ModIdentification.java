package mcp.mobius.waila.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import net.minecraft.Entity;
import net.minecraft.EntityList;
import net.minecraft.ItemStack;
import net.xiaoyu233.fml.api.block.IBlock;
import net.xiaoyu233.fml.api.entity.IEntity;
import net.xiaoyu233.fml.api.item.IItem;

public class ModIdentification {

    public static HashMap<String, String> modSource_Name = new HashMap<>();
    public static HashMap<String, String> modSource_ID = new HashMap<>();

    public static void init() {

//        for (ModContainer mod : FishModLoader.getModList()) {
//            modSource_Name.put(mod.getSource().getName(), mod.getName());
//            modSource_ID.put(mod.getSource().getName(), mod.getModId());
//        }

        // TODO : Update this to match new version (1.7.2)
        modSource_Name.put("1.6.2.jar", "Minecraft");
        modSource_Name.put("1.6.3.jar", "Minecraft");
        modSource_Name.put("1.6.4.jar", "Minecraft");
        modSource_Name.put("1.7.2.jar", "Minecraft");
        modSource_Name.put("Forge", "Minecraft");
        modSource_ID.put("1.6.2.jar", "Minecraft");
        modSource_ID.put("1.6.3.jar", "Minecraft");
        modSource_ID.put("1.6.4.jar", "Minecraft");
        modSource_ID.put("1.7.2.jar", "Minecraft");
        modSource_ID.put("Forge", "Minecraft");
    }

    public static String nameFromObject(Object obj) {
        String objPath = obj.getClass().getProtectionDomain().getCodeSource().getLocation().toString();

        try {
            objPath = URLDecoder.decode(objPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String modName = "<Unknown>";
        for (String s : modSource_Name.keySet())
            if (objPath.contains(s)) {
                modName = modSource_Name.get(s);
                break;
            }

        if (modName.equals("Minecraft Coder Pack")) modName = "Minecraft";

        return modName;
    }

    public static String nameFromStack(ItemStack itemStack) {
        try {
            int id = itemStack.itemID;
            String mod = "Minecraft";
            if (itemStack.isBlock()) {
                if (isMITEBlock(id)) {
                    mod = "MITE";
                } else if (isMinecraftBlock(id)) {
                    mod = "Minecraft";
                } else {
                    mod = (((IBlock) itemStack.getItemAsBlock().getBlock())).getNamespace();
                }
            } else {
                if (isMITEItem(id)) {
                    mod = "MITE";
                } else if (isMinecraftItem(id)) {
                    mod = "Minecraft";
                } else {
                    mod = ((IItem) itemStack.getItem()).getNamespace();
                }
            }
            return mod == null ? "Minecraft" : mod;
        } catch (NullPointerException e) {
            return "";
        }
    }

    public static String getEntityMod(Entity entity) {
        String mod;
        int id = EntityList.getEntityID(entity);
        if (id <= 100 || id == 120 || id == 200) {
            mod = "Minecraft";
        } else if (id >= 512 && id <= 540) {
            mod = "MITE";
        } else {
            mod = ((IEntity) entity).getNamespace();
        }
        return mod;
    }

    private static boolean isMITEBlock(int id) {
        if (id >= 198 && id < 256 || id == 95)
            return true;
        return id >= 164 && id < 170;
    }

    private static boolean isMinecraftBlock(int id) {
        if (id >= 170 && id <= 174)
            return true;
        return id <= 163;
    }

    private static boolean isMITEItem(int id) {
        if (id > 955 && id < 1283) {
            if (!(id >= 1058 && id <= 1066))
                return true;
            if (!(id >= 1135 && id <= 1141))
                return true;
            if (!(id >= 1168 && id <= 1171))
                return true;
            if (!(id >= 1265 && id <= 1275))
                return true;
            if (id != 1116 && id != 1026 && id != 1027)
                return true;
        }
        return id >= 2276 && id <= 2279;
    }

    private static boolean isMinecraftItem(int id) {
        if (id > 256 && id <= 422) {
            if (!(id > 269 && id < 280))
                return true;
            if (!(id > 309 && id < 314))
                return true;
            if (!(id > 408 && id < 417))
                return true;
            if (id != 262 && id != 268 && id != 290 && id != 291 && id != 293 && id != 419)
                return true;
        }
        return id >= 2256 && id <= 2267;
    }
}
