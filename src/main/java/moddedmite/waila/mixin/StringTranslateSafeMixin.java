package moddedmite.waila.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import net.minecraft.StringTranslate;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StringTranslate.class)
public class StringTranslateSafeMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/commons/io/IOUtils;readLines(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Ljava/util/List;"))
    private List<String> waila$readLinesSafe(InputStream input, Charset encoding) throws IOException
    {
        if (input != null)
        {
            try (InputStream stream = input)
            {
                return IOUtils.readLines(stream, encoding);
            }
        }

        InputStream fallback = StringTranslate.class.getResourceAsStream("/assets/minecraft/lang/en_US.lang");

        if (fallback == null)
        {
            fallback = StringTranslate.class.getResourceAsStream("/assets/minecraft/lang/MITE.lang");
        }

        if (fallback == null)
        {
            return Collections.emptyList();
        }

        try (InputStream stream = fallback)
        {
            return IOUtils.readLines(stream, encoding);
        }
    }
}
