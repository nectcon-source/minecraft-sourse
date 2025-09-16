package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class LegacyStuffWrapper {
    @Deprecated
    public static int[] getPixels(ResourceManager var0, ResourceLocation var1) throws IOException {
        Object var6;
        try (Resource var2 = var0.getResource(var1)) {
            NativeImage var4 = NativeImage.read(var2.getInputStream());
            Throwable var5 = null;

            try {
                var6 = var4.makePixelArray();
            } catch (Throwable var29) {
                var6 = var29;
                var5 = var29;
                throw var29;
            } finally {
                if (var4 != null) {
                    if (var5 != null) {
                        try {
                            var4.close();
                        } catch (Throwable var28) {
                            var5.addSuppressed(var28);
                        }
                    } else {
                        var4.close();
                    }
                }

            }
        }

        return (int[])var6;
    }
}
