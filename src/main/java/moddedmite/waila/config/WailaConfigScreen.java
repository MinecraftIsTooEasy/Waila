package moddedmite.waila.config;

import fi.dy.masa.malilib.config.interfaces.IConfigHandler;
import fi.dy.masa.malilib.gui.screen.DefaultConfigScreen;
import fi.dy.masa.malilib.gui.screen.util.ProgressSaving;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.api.impl.TipList;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.Tooltip;
import net.minecraft.*;
import net.xiaoyu233.fml.api.block.IBlock;

import java.util.List;

public class WailaConfigScreen extends DefaultConfigScreen {
    public static Tooltip tooltip;

    public WailaConfigScreen(GuiScreen parentScreen, IConfigHandler configInstance) {
        super(parentScreen, configInstance);
    }

//    @Override
//    protected void tickScreen() {
//        super.tickScreen();
//        OverlayRenderer overlayRenderer = new OverlayRenderer();
//        this.tickClient();
//        overlayRenderer.renderOverlay(tooltip);
//    }

    public void tickClient() {
        ItemStack targetStack = new ItemStack(Block.runestoneAdamantium);
        List<String> currenttip;
        List<String> currenttipHead;
        List<String> currenttipBody;
        List<String> currenttipTail;
        currenttip = new TipList<String, String>();
        currenttipHead = new TipList<String, String>();
        currenttipBody = new TipList<String, String>();
        currenttipTail = new TipList<String, String>();
        currenttipHead.add(targetStack.getDisplayName());
        currenttipBody.add("Show");
        currenttipTail.add(SpecialChars.BLUE + SpecialChars.ITALIC + ((IBlock) targetStack.getItemAsBlock().getBlock()).getNamespace());
        currenttip.addAll(currenttipHead);
        currenttip.addAll(currenttipBody);
        currenttip.addAll(currenttipTail);
        tooltip = new Tooltip(currenttip, true, targetStack);
    }

}
