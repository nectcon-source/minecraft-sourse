//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources.sounds;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;

public class Sound implements Weighted<Sound> {
   private final ResourceLocation location;
   private final float volume;
   private final float pitch;
   private final int weight;
   private final Type type;
   private final boolean stream;
   private final boolean preload;
   private final int attenuationDistance;

   public Sound(String var1, float var2, float var3, int var4, Type var5, boolean var6, boolean var7, int var8) {
      this.location = new ResourceLocation(var1);
      this.volume = var2;
      this.pitch = var3;
      this.weight = var4;
      this.type = var5;
      this.stream = var6;
      this.preload = var7;
      this.attenuationDistance = var8;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   public ResourceLocation getPath() {
      return new ResourceLocation(this.location.getNamespace(), "sounds/" + this.location.getPath() + ".ogg");
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public int getWeight() {
      return this.weight;
   }

   public Sound getSound() {
      return this;
   }

   public void preloadIfRequired(SoundEngine var1) {
      if (this.preload) {
         var1.requestPreload(this);
      }

   }

   public Type getType() {
      return this.type;
   }

   public boolean shouldStream() {
      return this.stream;
   }

   public boolean shouldPreload() {
      return this.preload;
   }

   public int getAttenuationDistance() {
      return this.attenuationDistance;
   }

   public String toString() {
      return "Sound[" + this.location + "]";
   }

   public static enum Type {
      FILE("file"),
      SOUND_EVENT("event");

      private final String name;

      private Type(String var3) {
         this.name = var3;
      }

      public static Type getByName(String var0) {
         for(Type var4 : values()) {
            if (var4.name.equals(var0)) {
               return var4;
            }
         }

         return null;
      }
   }
}
