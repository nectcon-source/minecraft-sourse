

package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementToast implements Toast {
   private final Advancement advancement;
   private boolean playedSound;

   public AdvancementToast(Advancement var1) {
      this.advancement = var1;
   }

   public Toast.Visibility render(PoseStack var1, ToastComponent var2, long var3) {
      var2.getMinecraft().getTextureManager().bind(TEXTURE);
      RenderSystem.color3f(1.0F, 1.0F, 1.0F);
      DisplayInfo var5 = this.advancement.getDisplay();
      var2.blit(var1, 0, 0, 0, 0, this.width(), this.height());
      if (var5 != null) {
         List<FormattedCharSequence> var6 = var2.getMinecraft().font.split(var5.getTitle(), 125);
         int var7 = var5.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
         if (var6.size() == 1) {
            var2.getMinecraft().font.draw(var1, var5.getFrame().getDisplayName(), 30.0F, 7.0F, var7 | -16777216);
            var2.getMinecraft().font.draw(var1, (FormattedCharSequence)var6.get(0), 30.0F, 18.0F, -1);
         } else {
            int var8 = 1500;
            float var9 = 300.0F;
            if (var3 < 1500L) {
               int var10 = Mth.floor(Mth.clamp((float)(1500L - var3) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
               var2.getMinecraft().font.draw(var1, var5.getFrame().getDisplayName(), 30.0F, 11.0F, var7 | var10);
            } else {
               int var14 = Mth.floor(Mth.clamp((float)(var3 - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
               var2.getMinecraft().font.getClass();
               int var11 = this.height() / 2 - var6.size() * 9 / 2;

               for(FormattedCharSequence var13 : var6) {
                  var2.getMinecraft().font.draw(var1, var13, 30.0F, (float)var11, 16777215 | var14);
                  var2.getMinecraft().font.getClass();
                  var11 += 9;
               }
            }
         }

         if (!this.playedSound && var3 > 0L) {
            this.playedSound = true;
            if (var5.getFrame() == FrameType.CHALLENGE) {
               var2.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
         }

         var2.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(var5.getIcon(), 8, 8);
         return var3 >= 5000L ? Visibility.HIDE : Visibility.SHOW;
      } else {
         return Visibility.HIDE;
      }
   }
}
