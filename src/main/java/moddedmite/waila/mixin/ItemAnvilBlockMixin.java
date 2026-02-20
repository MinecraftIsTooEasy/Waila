package moddedmite.waila.mixin;

import net.minecraft.Block;
import net.minecraft.BlockAnvil;
import net.minecraft.ItemAnvilBlock;
import net.minecraft.ItemMultiTextureTile;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemAnvilBlock.class)
public class ItemAnvilBlockMixin extends ItemMultiTextureTile {
	public ItemAnvilBlockMixin(Block block, String[] names) {
		super(block, names);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack == null) {
			return super.getUnlocalizedName();
		} else {
			int metadata = (stack.getItemSubtype() >> 2) % BlockAnvil.statuses.length;
			return super.getUnlocalizedName() + "." + BlockAnvil.statuses[metadata];
		}
	}
}
