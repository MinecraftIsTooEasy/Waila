package mcp.mobius.waila.overlay;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.*;

import java.util.ArrayList;
import java.util.List;

public class RayTracing {

    private static RayTracing _instance;

    private RayTracing() {
    }

    public static RayTracing instance() {
        if (_instance == null) _instance = new RayTracing();
        return _instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    private RaycastCollision target = null;// referring to mc.objectMouseOver is unreliable

    public void fire() {
        if (mc.objectMouseOver != null
//                &&
//                target.isEntity()
//                && !shouldHidePlayer(mc.objectMouseOver.getEntityHit())
        ) {
            this.target = mc.objectMouseOver;
            return;
        }

        EntityLivingBase viewpoint = mc.renderViewEntity;
        if (viewpoint == null) return;

        this.target = this.rayTrace(viewpoint, this.getReach(), 0);
    }

    private Float getReach() {
        RaycastCollision rc = this.mc.objectMouseOver;
        float reach = 0;
        if (rc != null) {
            if (rc.isBlock()) {
                reach = mc.thePlayer.getReach(rc.getBlockHit(), rc.world.getBlockMetadata(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z));
            } else if (rc.isEntity()) {
                reach = mc.thePlayer.getReach(EnumEntityReachContext.FOR_MELEE_ATTACK, rc.getEntityHit());
            }
        }
        return reach;
    }

//    private static boolean shouldHidePlayer(Entity targetEnt) {
//        // Check if entity is player with invisibility effect
//        if (targetEnt instanceof EntityPlayer thePlayer) {
//            boolean shouldHidePlayerSetting = !WailaConfig.showInvisiblePlayers.getBooleanValue();
//            return shouldHidePlayerSetting && thePlayer.isInvisible();
//        }
//        return false;
//    }

    public RaycastCollision getTarget() {
        return this.target;
    }

    public ItemStack getTargetStack() {
        return this.target != null && this.target.isBlock() ? this.getIdentifierStack() : null;
    }

    public Entity getTargetEntity() {
        return this.target != null && this.target.isEntity() ? this.getIdentifierEntity()
                : null;
    }

    public RaycastCollision rayTrace(EntityLivingBase entity, double par1, float par3) {
        Vec3 vec3 = entity.getPosition(par3);
        Vec3 vec31 = entity.getLook(par3);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * par1, vec31.yCoord * par1, vec31.zCoord * par1);

        if (WailaConfig.liquid.getBooleanValue())
            return entity.worldObj.getBlockCollisionForSelection(vec3, vec32, true);
        else return entity.worldObj.getBlockCollisionForSelection(vec3, vec32, false);
    }

    public ItemStack getIdentifierStack() {
        ArrayList<ItemStack> items = this.getIdentifierItems();

        if (items.isEmpty()) return null;

        items.sort((stack0, stack1) -> stack1.getItemSubtype() - stack0.getItemSubtype());

        return items.get(0);
    }

    public Entity getIdentifierEntity() {
        ArrayList<Entity> ents = new ArrayList<>();
        RaycastCollision raycastCollision = Minecraft.getMinecraft().objectMouseOver;

        if (this.target == null) return null;

        if (ModuleRegistrar.instance().hasOverrideEntityProviders(this.target.getEntityHit())) {
            for (List<IWailaEntityProvider> listProviders : ModuleRegistrar.instance()
                    .getOverrideEntityProviders(this.target.getEntityHit()).values()) {
                for (IWailaEntityProvider provider : listProviders) {
                    ents.add(provider.getWailaOverride(DataAccessorCommon.instance, WailaConfig.getInstance()));
                }
            }
        }

        if (!ents.isEmpty()) return ents.get(0);
        else return this.target.getEntityHit();
    }

    public ArrayList<ItemStack> getIdentifierItems() {
        ArrayList<ItemStack> items = new ArrayList<>();

        if (this.target == null) return items;

        World world = mc.theWorld;

        int x = this.target.block_hit_x;
        int y = this.target.block_hit_y;
        int z = this.target.block_hit_z;

        Block mouseoverBlock = world.getBlock(x, y, z);
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        if (mouseoverBlock == null) return items;

        if (ModuleRegistrar.instance().hasStackProviders(mouseoverBlock)) {
            for (List<IWailaDataProvider> providersList : ModuleRegistrar.instance().getStackProviders(mouseoverBlock)
                    .values()) {
                for (IWailaDataProvider provider : providersList) {
                    ItemStack providerStack = provider
                            .getWailaStack(DataAccessorCommon.instance, WailaConfig.getInstance());
                    if (providerStack != null) {

                        if (providerStack.getItem() == null) return new ArrayList<>();

                        items.add(providerStack);
                    }
                }
            }
        }

        if (tileEntity != null && ModuleRegistrar.instance().hasStackProviders(tileEntity)) {
            for (List<IWailaDataProvider> providersList : ModuleRegistrar.instance().getStackProviders(tileEntity)
                    .values()) {

                for (IWailaDataProvider provider : providersList) {
                    ItemStack providerStack = provider
                            .getWailaStack(DataAccessorCommon.instance, WailaConfig.getInstance());
                    if (providerStack != null) {

                        if (providerStack.getItem() == null) return new ArrayList<>();

                        items.add(providerStack);
                    }
                }
            }
        }

        if (!items.isEmpty()) return items;

        if (world.getBlockTileEntity(x, y, z) == null) {
            try {
                ItemStack block = new ItemStack(mouseoverBlock, 1, world.getBlockMetadata(x, y, z));

                if (block.getItem() != null) items.add(block);

            } catch (Exception ignored) {
            }
        }

//        if (!items.isEmpty()) return items;

//        try {
//            ItemStack pick = mouseoverBlock.getPickBlock(this.target, world, x, y, z);
//            if (pick != null) items.add(pick);
//        } catch (Exception ignored) {}

//        if (!items.isEmpty()) return items;

//        if (mouseoverBlock instanceof IShearable shearable) {
//            if (shearable.isShearable(new ItemStack(Item.shears), world, x, y, z)) {
//                items.addAll(shearable.onSheared(new ItemStack(Item.shears), world, x, y, z, 0));
//            }
//        }

        if (items.isEmpty()) items.add(0, new ItemStack(mouseoverBlock, 1, world.getBlockMetadata(x, y, z)));

        return items;
    }

}
