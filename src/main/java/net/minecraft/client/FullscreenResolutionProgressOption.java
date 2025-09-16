//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class FullscreenResolutionProgressOption extends ProgressOption {
   public FullscreenResolutionProgressOption(Window var1) {
      this(var1, var1.findBestMonitor());
   }

   private FullscreenResolutionProgressOption(Window var1, @Nullable Monitor var2) {
      super("options.fullscreen.resolution", -1.0F, var2 != null ? (double)(var2.getModeCount() - 1) : (double)-1.0F, 1.0F, (var2x) -> {
         if (var2 == null) {
            return (double)-1.0F;
         } else {
            Optional<VideoMode> var3 = var1.getPreferredFullscreenVideoMode();
            return var3.map((var1x) -> (double)var2.getVideoModeIndex(var1x)).orElse((double)-1.0F);
         }
      }, (var2x, var3) -> {
         if (var2 != null) {
            if (var3 == (double)-1.0F) {
               var1.setPreferredFullscreenVideoMode(Optional.empty());
            } else {
               var1.setPreferredFullscreenVideoMode(Optional.of(var2.getMode(var3.intValue())));
            }

         }
      }, (var1x, var2x) -> {
         if (var2 == null) {
            return new TranslatableComponent("options.fullscreen.unavailable");
         } else {
            double var3 = var2x.get(var1x);
            return var3 == (double)-1.0F ? var2x.genericValueLabel(new TranslatableComponent("options.fullscreen.current")) : var2x.genericValueLabel(new TextComponent(var2.getMode((int)var3).toString()));
         }
      });
   }
}
