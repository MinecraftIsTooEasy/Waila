package moddedmite.waila.gui;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.layer.Layer;
import fi.dy.masa.malilib.gui.screen.LayeredScreen;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.gui.list.Entry;
import moddedmite.waila.gui.list.OptionEntry;
import moddedmite.waila.gui.list.OptionsList;
import moddedmite.waila.gui.list.OptionsNav;
import moddedmite.waila.gui.util.ScreenTheme;
import moddedmite.waila.gui.widget.SearchBox;
import net.minecraft.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/** Common three-column skeleton for Jade-style Waila option screens. */
public abstract class BaseOptionsScreen extends LayeredScreen {
    protected final @Nullable GuiScreen parentScreen;
    protected final String titleKey;
    protected OptionsList options;
    protected OptionsNav nav;
    protected SearchBox searchBox;
    protected @Nullable ButtonGeneric saveButton;
    protected Runnable saver = () -> {};
    protected @Nullable Runnable canceller;
    private final Map<ConfigBase<?>, JsonElement> snapshot = new IdentityHashMap<>();
    private boolean snapshotTaken;
    private boolean saving;

    protected BaseOptionsScreen(@Nullable GuiScreen parent, String titleKey) {
        this.parentScreen = parent;
        this.titleKey = titleKey;
        this.setParent(parent);
    }

    protected abstract void buildOptions(OptionsList options);

    @Override
    protected void initBaseLayer(Layer layer) {
        String oldSearch = this.searchBox == null ? "" : this.searchBox.getValue();
        float oldScroll = this.options == null ? 0 : this.options.getScrollTarget();
        super.initBaseLayer(layer);

        this.options = new OptionsList(ScreenTheme.NAV_WIDTH, 0,
                Math.max(0, this.width - ScreenTheme.NAV_WIDTH),
                Math.max(0, this.height - ScreenTheme.FOOTER_HEIGHT));
        this.buildOptions(this.options);
        this.nav = new OptionsNav(this.options, 0, ScreenTheme.SEARCH_HEIGHT, ScreenTheme.NAV_WIDTH,
                Math.max(0, this.height - ScreenTheme.FOOTER_HEIGHT - ScreenTheme.SEARCH_HEIGHT));
        this.searchBox = new SearchBox(0, 0, ScreenTheme.NAV_WIDTH, ScreenTheme.SEARCH_HEIGHT, value -> {
            this.options.updateSearch(value);
            this.nav.refresh();
        });

        layer.addWidget(this.searchBox);
        layer.addWidget(this.nav);
        layer.addWidget(this.options);
        this.searchBox.setValue(oldSearch);
        this.options.updateSearch(oldSearch);
        this.nav.refresh();
        this.options.forceScroll(oldScroll);
        this.takeSnapshot();

        this.saveButton = ButtonGeneric.builder(ScreenTheme.TXT_SAVE
                        + StringUtils.translate("gui.waila.save_and_quit"), button -> this.saveAndClose())
                .dimensions(this.width - 100, this.height - ScreenTheme.BUTTON_BOTTOM_MARGIN,
                        ScreenTheme.BUTTON_WIDTH, ScreenTheme.BUTTON_HEIGHT).build();
        layer.addWidget(this.saveButton);
        if (this.canceller != null) {
            layer.addWidget(ButtonGeneric.builder(StringUtils.translate("screen.button.cancel"), button -> this.close())
                    .dimensions(this.width - 195, this.height - ScreenTheme.BUTTON_BOTTOM_MARGIN,
                            ScreenTheme.BUTTON_WIDTH, ScreenTheme.BUTTON_HEIGHT).build());
        }
        Keyboard.enableRepeatEvents(true);
    }

    protected void takeSnapshot() {
        if (this.snapshotTaken) {
            return;
        }
        this.snapshot.clear();
        for (Entry entry : this.options.allEntries()) {
            if (entry instanceof OptionEntry optionEntry) {
                ConfigBase<?> config = optionEntry.getConfig();
                // 不能用 JsonElement.deepCopy()：编译期是 Gson 2.10.1，但运行期
                // classpath 上同时有 MC 自带的 gson 2.2.2（无此方法），会 NoSuchMethodError。
                // ManyLib 自己实现了递归深拷贝，用它才安全。
                this.snapshot.put(config, JsonUtils.deepCopy(config.getAsJsonElement()));
            }
        }
        this.snapshotTaken = true;
    }

    protected void rollback() {
        for (Map.Entry<ConfigBase<?>, JsonElement> saved : this.snapshot.entrySet()) {
            ConfigBase<?> config = saved.getKey();
            if (config instanceof ConfigHotkey hotkey) {
                hotkey.getKeybind().resetSettingsToDefaults();
            }
            config.setValueFromJsonElement(JsonUtils.deepCopy(saved.getValue()));
        }
        if (this.options != null) {
            for (Entry entry : this.options.allEntries()) {
                if (entry instanceof OptionEntry optionEntry) {
                    optionEntry.syncFromConfig();
                    optionEntry.refreshDisabledState();
                }
            }
        }
    }

    protected void saveAndClose() {
        OptionEntry invalid = this.options.findInvalidEntry();
        if (invalid != null) {
            if (!this.options.getVisibleEntries().contains(invalid)) {
                this.searchBox.setValue("");
                this.options.updateSearch("");
                this.nav.refresh();
            }
            this.options.scrollToEntry(invalid);
            if (this.saveButton != null) {
                this.saveButton.setHoverStrings("gui.waila.invalid_value_cant_save");
            }
            return;
        }
        if (this.saveButton != null) {
            this.saveButton.clearHoverStrings();
        }
        this.saver.run();
        InputEventHandler.getKeybindManager().updateUsedKeys();
        this.saving = true;
        this.close();
    }

    @Override
    protected void close() {
        if (!this.saving && this.canceller != null) {
            this.canceller.run();
        }
        super.close();
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        if (ctrl && keyCode == Keyboard.KEY_F) {
            this.searchBox.setFocused(true);
            return true;
        }
        if (super.charTyped(chr, keyCode)) {
            return true;
        }
        if (!this.searchBox.isFocused() && Character.isLetterOrDigit(chr)) {
            this.searchBox.setFocused(true);
            return this.searchBox.onCharTyped(chr, keyCode);
        }
        return false;
    }

    @Override
    protected void tick() {
        super.tick();
        if (this.options != null) {
            this.options.tick();
        }
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
