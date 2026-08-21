package moddedmite.waila.gui.list;

import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.DrawContext;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.layer.KeySettingsLayer;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindCategory;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.util.ScreenTheme;
import net.minecraft.EnumChatFormatting;
import net.minecraft.GuiScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/** Hotkey capture row with conflict reporting and advanced settings layer. */
public class KeybindEntry extends OptionEntry {
    private final IKeybind keybind;
    private final ButtonGeneric captureButton;
    private final ButtonGeneric settingsButton;
    private final GuiScreen screen;
    private final Consumer<Supplier<Layer>> layerOpener;
    private final List<String> conflicts = new ArrayList<>();
    private @Nullable List<Integer> lastCheckedKeys;
    private boolean editing;

    public KeybindEntry(ConfigHotkey config, GuiScreen screen, Consumer<Supplier<Layer>> layerOpener) {
        super(config);
        this.keybind = config.getKeybind();
        this.screen = screen;
        this.layerOpener = layerOpener;
        this.captureButton = ButtonGeneric.builder("", button -> {
                    this.editing = true;
                    this.keybind.clearKeys();
                }).size(ScreenTheme.WIDGET_WIDTH - 24, ScreenTheme.WIDGET_HEIGHT).build();
        this.settingsButton = ButtonGeneric.builder(StringUtils.translate("manyLib.gui.button.keySettings"),
                        button -> this.layerOpener.accept(() -> new KeySettingsLayer(this.screen, this.keybind)))
                .size(20, ScreenTheme.WIDGET_HEIGHT).build();
    }

    @Override
    protected void renderValueWidget(int mouseX, int mouseY, DrawContext context, int valueX, int valueY) {
        this.updateDisplay();
        this.captureButton.setPosition(valueX, valueY);
        this.captureButton.render(mouseX, mouseY, false, context);
        this.settingsButton.setPosition(valueX + ScreenTheme.WIDGET_WIDTH - 22, valueY);
        this.settingsButton.render(mouseX, mouseY, false, context);
    }

    @Override
    protected List<? extends WidgetBase> valueWidgets() {
        return List.of(this.captureButton, this.settingsButton);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (super.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.editing && !this.captureButton.isMouseOver(mouseX, mouseY)) {
            this.editing = false;
            return true;
        }
        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int button) {
        super.onMouseReleased(mouseX, mouseY, button);
        if (!this.captureButton.isMouseOver(mouseX, mouseY)) {
            this.editing = false;
        }
    }

    @Override
    public boolean onCharTyped(char chr, int keyCode) {
        if (!this.editing) {
            return super.onCharTyped(chr, keyCode);
        }
        if (keyCode == 1 || keyCode == 28 || keyCode == 156) {
            this.editing = false;
        } else {
            this.keybind.addKey(keyCode);
        }
        return true;
    }

    @Override
    public void syncFromConfig() {
        this.editing = false;
        // 重置会改按键，强制下一帧重算冲突。
        this.lastCheckedKeys = null;
        this.updateDisplay();
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY, DrawContext context) {
        if (this.captureButton.isMouseOver(mouseX, mouseY) && !this.conflicts.isEmpty()) {
            RenderUtils.drawHoverText(mouseX, mouseY, this.conflicts, context);
            return;
        }
        if (this.settingsButton.isMouseOver(mouseX, mouseY)) {
            List<String> lines = new ArrayList<>();
            lines.add(StringUtils.translate("manyLib.keybind.settings") + ":");
            lines.addAll(this.keybind.getSettings().toStringList());
            RenderUtils.drawTextList(lines, mouseX, mouseY, context);
            return;
        }
        super.renderTooltip(mouseX, mouseY, context);
    }

    private void updateDisplay() {
        List<Integer> keys = this.keybind.getKeys();
        if (this.lastCheckedKeys == null || !this.lastCheckedKeys.equals(keys)) {
            this.lastCheckedKeys = new ArrayList<>(keys);
            this.updateConflicts();
        }
        String display = this.keybind.getKeysDisplayString();
        if (display.isEmpty()) {
            display = StringUtils.translate("gui.waila.keybind.none");
        }
        if (this.editing) {
            display = ScreenTheme.TXT_CAPTURING + "> " + display + " <";
        } else if (!this.conflicts.isEmpty()) {
            display = EnumChatFormatting.GOLD + display;
        }
        this.captureButton.setDisplayString(display);
    }

    private void updateConflicts() {
        this.conflicts.clear();
        if (this.keybind.getKeys().isEmpty()) {
            this.captureButton.clearHoverStrings();
            return;
        }
        for (KeybindCategory category : InputEventHandler.getKeybindManager().getKeybindCategories()) {
            List<IHotkey> overlaps = new ArrayList<>();
            for (IHotkey hotkey : category.getHotkeys()) {
                if (hotkey.getKeybind() != this.keybind && this.keybind.overlaps(hotkey.getKeybind())) {
                    overlaps.add(hotkey);
                }
            }
            if (!overlaps.isEmpty()) {
                if (this.conflicts.isEmpty()) {
                    this.conflicts.add(StringUtils.translate("gui.waila.keybind.conflict"));
                } else {
                    this.conflicts.add("-----");
                }
                this.conflicts.add(category.getModName());
                this.conflicts.add(" > " + category.getCategory());
                for (IHotkey overlap : overlaps) {
                    this.conflicts.add("    - " + overlap.getConfigGuiDisplayName() + " [ "
                            + EnumChatFormatting.GOLD + overlap.getKeybind().getKeysDisplayString()
                            + EnumChatFormatting.RESET + " ]");
                }
            }
        }
        this.captureButton.setHoverStrings(this.conflicts);
    }
}
