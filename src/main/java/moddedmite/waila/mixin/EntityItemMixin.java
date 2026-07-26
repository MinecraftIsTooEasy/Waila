package moddedmite.waila.mixin;

import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends Entity {
	public EntityItemMixin(World par1World) {
		super(par1World);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
}
