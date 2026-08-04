package moddedmite.waila.gui.util;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.StringUtils;
import moddedmite.waila.config.WailaConfig;

/** Resolves dynamic config-tooltip placeholders at render time. */
public final class ConfigVariables {
    private ConfigVariables() {
    }

    public static String replaceKeybindVariables(String text) {
        String result = text;
        result = replace(result, "KEY_CONFIG", WailaConfig.wailaconfig);
        result = replace(result, "KEY_DISPLAY", WailaConfig.wailadisplay);
        result = replace(result, "KEY_LIQUID", WailaConfig.keyliquid);
        result = replace(result, "KEY_RECIPE", WailaConfig.recipe);
        result = replace(result, "KEY_USAGE", WailaConfig.usage);
        return result;
    }

    private static String replace(String text, String placeholder, ConfigHotkey config) {
        String display = config.getKeybind().getKeysDisplayString();
        if (display.isEmpty()) {
            display = StringUtils.translate("gui.waila.keybind.none");
        }
        return TextUtil.replaceVariable(text, placeholder, display);
    }
}
