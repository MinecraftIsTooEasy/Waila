package moddedmite.waila.handlers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.cbcore.LangUtil;
import moddedmite.waila.config.WailaConfig;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumDirection;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.RaycastCollision;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;
import net.minecraft.Vec3;
import net.minecraft.World;

import java.text.DecimalFormat;
import java.util.List;

public class HUDHandlerExtra implements IWailaDataProvider {
	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		RaycastCollision cast = accessor.getPosition();
		if (cast == null) return currenttip;

		Vec3 pos = Vec3.createVectorHelper(cast.block_hit_x, cast.block_hit_y, cast.block_hit_z);
		DecimalFormat distanceFormat = new DecimalFormat("0.0");
		String distance = distanceFormat.format(cast.raycast.getOrigin().distanceTo(pos));

		if (WailaConfig.position.getBooleanValue() || WailaConfig.distance.getBooleanValue() || WailaConfig.direction.getBooleanValue()) {
			StringBuilder sb = new StringBuilder(SpecialChars.GRAY);
			if (WailaConfig.position.getBooleanValue()) {
				sb.append(pos.xCoord).append(' ').append(pos.yCoord).append(' ').append(pos.zCoord);
			}
			if (WailaConfig.distance.getBooleanValue()) {
				sb.append(" [").append(distance).append(']');
			}
			if (WailaConfig.direction.getBooleanValue()) {
				sb.append(" {").append(accessor.getSide()).append('}');
			}
			currenttip.add(sb.toString());
		}
		
		if (WailaConfig.vsblock.getBooleanValue() && cast.isBlock()) {
			float hardness = accessor.getWorld().getBlockHardness(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
			float strVsBlock = accessor.getPlayer().getCurrentPlayerStrVsBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), true);
			
			if (strVsBlock <= 0.0F) {
				currenttip.add(SpecialChars.GRAY + LangUtil.translateG("hud.msg.hardness") + hardness);
			} else {
				DecimalFormat strFormat = new DecimalFormat("0.00");
				currenttip.add(SpecialChars.GRAY + LangUtil.translateG("hud.msg.hardness") + hardness + " " + LangUtil.translateG("hud.msg.str_vs_block") + strFormat.format(strVsBlock));
			}
		}
		
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public NBTTagCompound getNBTData(ServerPlayer player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
		return null;
	}

	public static void register() {
		IWailaDataProvider provider = new HUDHandlerExtra();
		ModuleRegistrar.instance().registerBodyProvider(provider, Block.class);
	}
}
