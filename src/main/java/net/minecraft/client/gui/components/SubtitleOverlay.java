

package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SubtitleOverlay extends GuiComponent implements SoundEventListener {
   private final Minecraft minecraft;
   private final List<Subtitle> subtitles = Lists.newArrayList();
   private boolean isListening;

   public SubtitleOverlay(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(PoseStack var1) {
      if (!this.isListening && this.minecraft.options.showSubtitles) {
         this.minecraft.getSoundManager().addListener(this);
         this.isListening = true;
      } else if (this.isListening && !this.minecraft.options.showSubtitles) {
         this.minecraft.getSoundManager().removeListener(this);
         this.isListening = false;
      }

      if (this.isListening && !this.subtitles.isEmpty()) {
         RenderSystem.pushMatrix();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Vec3 var2 = new Vec3(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ());
         Vec3 var3 = (new Vec3((double)0.0F, (double)0.0F, (double)-1.0F)).xRot(-this.minecraft.player.xRot * ((float)Math.PI / 180F)).yRot(-this.minecraft.player.yRot * ((float)Math.PI / 180F));
         Vec3 var4 = (new Vec3((double)0.0F, (double)1.0F, (double)0.0F)).xRot(-this.minecraft.player.xRot * ((float)Math.PI / 180F)).yRot(-this.minecraft.player.yRot * ((float)Math.PI / 180F));
         Vec3 var5 = var3.cross(var4);
         int var6 = 0;
         int var7 = 0;
         Iterator<Subtitle> var8 = this.subtitles.iterator();

         while(var8.hasNext()) {
            Subtitle var9 = (Subtitle)var8.next();
            if (var9.getTime() + 3000L <= Util.getMillis()) {
               var8.remove();
            } else {
               var7 = Math.max(var7, this.minecraft.font.width(var9.getText()));
            }
         }

         var7 += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

         for(Subtitle var27 : this.subtitles) {
            int var10 = 255;
            Component var11 = var27.getText();
            Vec3 var12 = var27.getLocation().subtract(var2).normalize();
            double var13 = -var5.dot(var12);
            double var15 = -var3.dot(var12);
            boolean var17 = var15 > (double)0.5F;
            int var18 = var7 / 2;
            this.minecraft.font.getClass();
            int var19 = 9;
            int var20 = var19 / 2;
            float var21 = 1.0F;
            int var22 = this.minecraft.font.width(var11);
            int var23 = Mth.floor(Mth.clampedLerp((double)255.0F, (double)75.0F, (double)((float)(Util.getMillis() - var27.getTime()) / 3000.0F)));
            int var24 = var23 << 16 | var23 << 8 | var23;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)this.minecraft.getWindow().getGuiScaledWidth() - (float)var18 * 1.0F - 2.0F, (float)(this.minecraft.getWindow().getGuiScaledHeight() - 30) - (float)(var6 * (var19 + 1)) * 1.0F, 0.0F);
            RenderSystem.scalef(1.0F, 1.0F, 1.0F);
            fill(var1, -var18 - 1, -var20 - 1, var18 + 1, var20 + 1, this.minecraft.options.getBackgroundColor(0.8F));
            RenderSystem.enableBlend();
            if (!var17) {
               if (var13 > (double)0.0F) {
                  this.minecraft.font.draw(var1, ">", (float)(var18 - this.minecraft.font.width(">")), (float)(-var20), var24 + -16777216);
               } else if (var13 < (double)0.0F) {
                  this.minecraft.font.draw(var1, "<", (float)(-var18), (float)(-var20), var24 + -16777216);
               }
            }

            this.minecraft.font.draw(var1, var11, (float)(-var22 / 2), (float)(-var20), var24 + -16777216);
            RenderSystem.popMatrix();
            ++var6;
         }

         RenderSystem.disableBlend();
         RenderSystem.popMatrix();
      }
   }

   public void onPlaySound(SoundInstance var1, WeighedSoundEvents var2) {
      if (var2.getSubtitle() != null) {
         Component var3 = var2.getSubtitle();
         if (!this.subtitles.isEmpty()) {
            for(Subtitle var5 : this.subtitles) {
               if (var5.getText().equals(var3)) {
                  var5.refresh(new Vec3(var1.getX(), var1.getY(), var1.getZ()));
                  return;
               }
            }
         }

         this.subtitles.add(new Subtitle(var3, new Vec3(var1.getX(), var1.getY(), var1.getZ())));
      }
   }

   public class Subtitle {
      private final Component text;
      private long time;
      private Vec3 location;

      public Subtitle(Component var2, Vec3 var3) {
         this.text = var2;
         this.location = var3;
         this.time = Util.getMillis();
      }

      public Component getText() {
         return this.text;
      }

      public long getTime() {
         return this.time;
      }

      public Vec3 getLocation() {
         return this.location;
      }

      public void refresh(Vec3 var1) {
         this.location = var1;
         this.time = Util.getMillis();
      }
   }
}
