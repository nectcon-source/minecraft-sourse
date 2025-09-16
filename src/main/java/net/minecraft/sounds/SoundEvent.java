package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
   public static final Codec<SoundEvent> CODEC = ResourceLocation.CODEC.xmap(SoundEvent::new, var0 -> var0.location);
   private final ResourceLocation location;

   public SoundEvent(ResourceLocation var1) {
      this.location = var1;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }
}
