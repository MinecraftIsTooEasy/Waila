package mcp.mobius.waila.api.impl;

import net.minecraft.*;

import mcp.mobius.waila.api.IWailaFMPAccessor;
import mcp.mobius.waila.utils.NBTUtil;

public class DataAccessorFMP implements IWailaFMPAccessor {

    String id;
    World world;
    EntityPlayer player;
    RaycastCollision mop;
    Vec3 renderingvec = null;
    TileEntity entity;
    NBTTagCompound partialNBT = null;
    NBTTagCompound remoteNBT = null;
    long timeLastUpdate = System.currentTimeMillis();
    double partialFrame;

    public static DataAccessorFMP instance = new DataAccessorFMP();

    public void set(World _world, EntityPlayer _player, RaycastCollision _mop, NBTTagCompound _partialNBT,
            String id) {
        this.set(_world, _player, _mop, _partialNBT, id, null, 0.0);
    }

    public void set(World _world, EntityPlayer _player, RaycastCollision _mop, NBTTagCompound _partialNBT,
                    String id, Vec3 renderVec, double partialTicks) {
        this.world = _world;
        this.player = _player;
        this.mop = _mop;
        this.entity = world.getBlockTileEntity(_mop.block_hit_x, _mop.block_hit_y, _mop.block_hit_z);
        this.partialNBT = _partialNBT;
        this.id = id;
        this.renderingvec = renderVec;
        this.partialFrame = partialTicks;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public EntityPlayer getPlayer() {
        return this.player;
    }

    @Override
    public TileEntity getTileEntity() {
        return this.entity;
    }

    @Override
    public RaycastCollision getPosition() {
        return this.mop;
    }

    @Override
    public NBTTagCompound getNBTData() {
        return this.partialNBT;
    }

    @Override
    public NBTTagCompound getFullNBTData() {
        if (this.entity == null) return null;

        if (this.isTagCorrect(this.remoteNBT)) return this.remoteNBT;

        NBTTagCompound tag = new NBTTagCompound();
        this.entity.writeToNBT(tag);
        return tag;
    }

    @Override
    public int getNBTInteger(NBTTagCompound tag, String keyname) {
        return NBTUtil.getNBTInteger(tag, keyname);
    }

    @Override
    public double getPartialFrame() {
        return this.partialFrame;
    }

    @Override
    public Vec3 getRenderingPosition() {
        return this.renderingvec;
    }

    @Override
    public String getID() {
        return this.id;
    }

    private boolean isTagCorrect(NBTTagCompound tag) {
        if (tag == null) {
            this.timeLastUpdate = System.currentTimeMillis() - 250;
            return false;
        }

        int x = tag.getInteger("x");
        int y = tag.getInteger("y");
        int z = tag.getInteger("z");

        if (x == this.mop.block_hit_x && y == this.mop.block_hit_y && z == this.mop.block_hit_z) return true;
        else {
            this.timeLastUpdate = System.currentTimeMillis() - 250;
            return false;
        }
    }
}
