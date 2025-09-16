package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Screenshot {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

   public static void grab(File var0, int var1, int var2, RenderTarget var3, Consumer<Component> var4) {
      grab(var0, null, var1, var2, var3, var4);
   }

   public static void grab(File var0, @Nullable String var1, int var2, int var3, RenderTarget var4, Consumer<Component> var5) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> _grab(var0, var1, var2, var3, var4, var5));
      } else {
         _grab(var0, var1, var2, var3, var4, var5);
      }
   }

   private static void _grab(File var0, @Nullable String var1, int var2, int var3, RenderTarget var4, Consumer<Component> var5) {
      NativeImage var6x = takeScreenshot(var2, var3, var4);
      File var7xx = new File(var0, "screenshots");
      var7xx.mkdir();
      File var8;
      if (var1 == null) {
         var8 = getFile(var7xx);
      } else {
         var8 = new File(var7xx, var1);
      }

      Util.ioPool()
         .execute(
            () -> {
               try {
                  var6x.writeToFile(var8);
                  Component var3x = new TextComponent(var8.getName())
                     .withStyle(ChatFormatting.UNDERLINE)
                     .withStyle(var1xx -> var1xx.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var8.getAbsolutePath())));
                  var5.accept(new TranslatableComponent("screenshot.success", var3x));
               } catch (Exception var7x) {
                  LOGGER.warn("Couldn't save screenshot", var7x);
                  var5.accept(new TranslatableComponent("screenshot.failure", var7x.getMessage()));
               } finally {
                  var6x.close();
               }
            }
         );
   }

   public static NativeImage takeScreenshot(int var0, int var1, RenderTarget var2) {
      var0 = var2.width;
      var1 = var2.height;
      NativeImage var3 = new NativeImage(var0, var1, false);
      RenderSystem.bindTexture(var2.getColorTextureId());
      var3.downloadTexture(0, true);
      var3.flipY();
      return var3;
   }

   private static File getFile(File var0) {
      String var1 = DATE_FORMAT.format(new Date());
      int var2x = 1;

      while(true) {
         File var3xx = new File(var0, var1 + (var2x == 1 ? "" : "_" + var2x) + ".png");
         if (!var3xx.exists()) {
            return var3xx;
         }

         ++var2x;
      }
   }
}
