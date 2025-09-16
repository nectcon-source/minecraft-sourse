//package net.minecraft.client.renderer.texture;
//
//import com.google.common.collect.Lists;
//import com.mojang.blaze3d.platform.NativeImage;
//import javax.annotation.Nullable;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.resources.metadata.animation.AnimationFrame;
//import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.LazyLoadedValue;
//
//public final class MissingTextureAtlasSprite extends TextureAtlasSprite {
//   private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
//   @Nullable
//   private static DynamicTexture missingTexture;
//   private static final LazyLoadedValue<NativeImage> MISSING_IMAGE_DATA = new LazyLoadedValue(() -> {
//      NativeImage var0 = new NativeImage(16, 16, false);
//      int var1 = -16777216;
//      int var2 = -524040;
//
//      for(int var3 = 0; var3 < 16; ++var3) {
//         for(int var4 = 0; var4 < 16; ++var4) {
//            if (var3 < 8 ^ var4 < 8) {
//               var0.setPixelRGBA(var4, var3, -524040);
//            } else {
//               var0.setPixelRGBA(var4, var3, -16777216);
//            }
//         }
//      }
//
//      var0.untrack();
//      return var0;
//   });
//   private static final TextureAtlasSprite.Info INFO = new TextureAtlasSprite.Info(MISSING_TEXTURE_LOCATION, 16, 16, new AnimationMetadataSection(Lists.newArrayList(new AnimationFrame(0, -1)), 16, 16, 1, false));;
//
//   private MissingTextureAtlasSprite(TextureAtlas var1, int var2, int var3, int var4, int var5, int var6) {
//      super(var1, INFO, var2, var3, var4, var5, var6, MISSING_IMAGE_DATA.get());
//   }
//
//   public static MissingTextureAtlasSprite newInstance(TextureAtlas var0, int var1, int var2, int var3, int var4, int var5) {
//      return new MissingTextureAtlasSprite(var0, var1, var2, var3, var4, var5);
//   }
//
//   public static ResourceLocation getLocation() {
//      return MISSING_TEXTURE_LOCATION;
//   }
//
//   public static TextureAtlasSprite.Info info() {
//      return INFO;
//   }
//
//   public void close() {
//      for(int var1 = 1; var1 < this.mainImage.length; ++var1) {
//         this.mainImage[var1].close();
//      }
//
//   }
//
//   public static DynamicTexture getTexture() {
//      if (missingTexture == null) {
//         missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
//         Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
//      }
//
//      return missingTexture;
//   }
//}
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MissingTextureAtlasSprite extends TextureAtlasSprite {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
    @Nullable
    private static DynamicTexture missingTexture;

    private static final LazyLoadedValue<NativeImage> MISSING_IMAGE_DATA = new LazyLoadedValue<>(() -> {
        LOGGER.debug("▶ Генерация изображения missing texture 16x16...");
        NativeImage var0 = new NativeImage(16, 16, false);
        int black = -16777216;
        int purple = -524040;

        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                if (y < 8 ^ x < 8) {
                    var0.setPixelRGBA(x, y, purple);
                } else {
                    var0.setPixelRGBA(x, y, black);
                }
            }
        }

        var0.untrack();
        LOGGER.debug("✔ Изображение missing texture сгенерировано");
        return var0;
    });

    private static final TextureAtlasSprite.Info INFO =
            new TextureAtlasSprite.Info(
                    MISSING_TEXTURE_LOCATION, 16, 16,
                    new AnimationMetadataSection(Lists.newArrayList(new AnimationFrame(0, -1)), 16, 16, 1, false)
            );

    private MissingTextureAtlasSprite(TextureAtlas atlas, int x, int y, int w, int h, int mip) {
        super(atlas, INFO, x, y, w, h, mip, MISSING_IMAGE_DATA.get());
        LOGGER.warn("⚠ Конструктор MissingTextureAtlasSprite вызван (atlas={}, x={}, y={}, w={}, h={}, mip={})",
                atlas.location(), x, y, w, h, mip);
    }

    public static MissingTextureAtlasSprite newInstance(TextureAtlas atlas, int x, int y, int w, int h, int mip) {
        LOGGER.error("⚠ newInstance() → используется missing texture! (atlas={}, x={}, y={}, w={}, h={}, mip={})",
                atlas.location(), x, y, w, h, mip);
        return new MissingTextureAtlasSprite(atlas, x, y, w, h, mip);
    }

    public static ResourceLocation getLocation() {
        LOGGER.debug("▶ getLocation() вызван → {}", MISSING_TEXTURE_LOCATION);
        return MISSING_TEXTURE_LOCATION;
    }

    public static TextureAtlasSprite.Info info() {
        LOGGER.debug("▶ info() вызван");
        return INFO;
    }

    @Override
    public void close() {
        LOGGER.warn("✖ close() вызван → освобождаем ресурсы missing texture");
        for (int i = 1; i < this.mainImage.length; ++i) {
            this.mainImage[i].close();
        }
    }

    public static DynamicTexture getTexture() {
        LOGGER.debug("▶ getTexture() вызван");
        if (missingTexture == null) {
            LOGGER.warn("⚠ DynamicTexture для missing texture ещё не создан → создаём...");
            missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
            Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
            LOGGER.info("✔ Missing DynamicTexture зарегистрирован [{}]", MISSING_TEXTURE_LOCATION);
        } else {
            LOGGER.debug("✔ Missing DynamicTexture уже существует [{}]", MISSING_TEXTURE_LOCATION);
        }
        return missingTexture;
    }
}
