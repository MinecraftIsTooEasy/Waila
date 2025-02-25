package moddedmite.xylose.hwite.info;

import net.minecraft.EntityPlayer;
import net.minecraft.RaycastCollision;

import java.util.List;

public record InfoContext(List<String> tooltip, RaycastCollision rc, EntityPlayer player, int breakProgress) {
}
