package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class WeighedSoundEvents implements Weighted<Sound> {
   private final List<Weighted<Sound>> list = Lists.newArrayList();
   private final Random random = new Random();
   private final ResourceLocation location;
   @Nullable
   private final Component subtitle;

   public WeighedSoundEvents(ResourceLocation var1, @Nullable String var2) {
      this.location = var1;
      this.subtitle = var2 == null ? null : new TranslatableComponent(var2);
   }

   @Override
   public int getWeight() {
      int var1 = 0;

      for(Weighted<Sound> var2 : this.list) {
         var1 += var2.getWeight();
      }

      return var1;
   }

   public Sound getSound() {
      int var1 = this.getWeight();
      if (!this.list.isEmpty() && var1 != 0) {
         int var2x = this.random.nextInt(var1);

         for(Weighted<Sound> var3 : this.list) {
            var2x -= var3.getWeight();
            if (var2x < 0) {
               return var3.getSound();
            }
         }

         return SoundManager.EMPTY_SOUND;
      } else {
         return SoundManager.EMPTY_SOUND;
      }
   }

   public void addSound(Weighted<Sound> var1) {
      this.list.add(var1);
   }

   @Nullable
   public Component getSubtitle() {
      return this.subtitle;
   }

   @Override
   public void preloadIfRequired(SoundEngine var1) {
      for(Weighted<Sound> var2 : this.list) {
         var2.preloadIfRequired(var1);
      }
   }
}
