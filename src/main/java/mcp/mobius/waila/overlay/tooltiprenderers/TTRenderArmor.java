package mcp.mobius.waila.overlay.tooltiprenderers;

import java.awt.Dimension;

import net.minecraft.MathHelper;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.overlay.IconUI;

/**
 * Custom renderer for armor bars. Syntax : {waila.armor, narcperline, armor, maxarmor}
 *
 */
public class TTRenderArmor implements IWailaTooltipRenderer {

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        float maxarmors = Float.parseFloat(params[0]);
        float maxarmor = Float.parseFloat(params[2]);

        int armorsPerLine = (int) (Math.min(maxarmors, Math.ceil(maxarmor)));
        int nlines = (int) (Math.ceil(maxarmor / maxarmors));

        return new Dimension(8 * armorsPerLine, 10 * nlines - 2);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        float maxarmors = Float.parseFloat(params[0]);
        float armor = Float.parseFloat(params[1]);
        float maxarmor = Float.parseFloat(params[2]);

        int nhearts = MathHelper.ceiling_float_int(maxarmor);
        int heartsPerLine = (int) (Math.min(maxarmors, Math.ceil(maxarmor)));

        int offsetX = 0;
        int offsetY = 0;

        for (int iheart = 1; iheart <= nhearts; iheart++) {

            if (iheart <= MathHelper.floor_float(armor)) {
                DisplayUtil.renderIcon(offsetX, offsetY, 8, 8, IconUI.ARMOR);
                offsetX += 8;
            }

            if ((iheart > armor) && (iheart < armor + 1)) {
                DisplayUtil.renderIcon(offsetX, offsetY, 8, 8, IconUI.HARMOR);
                offsetX += 8;
            }

            if (iheart >= armor + 1) {
                DisplayUtil.renderIcon(offsetX, offsetY, 8, 8, IconUI.EARMOR);
                offsetX += 8;
            }

            if (iheart % heartsPerLine == 0) {
                offsetY += 10;
                offsetX = 0;
            }

        }
    }
}