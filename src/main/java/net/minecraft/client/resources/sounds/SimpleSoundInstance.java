//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SimpleSoundInstance extends AbstractSoundInstance {
   public SimpleSoundInstance(SoundEvent var1, SoundSource var2, float var3, float var4, BlockPos var5) {
      this(var1, var2, var3, var4, (double)var5.getX() + (double)0.5F, (double)var5.getY() + (double)0.5F, (double)var5.getZ() + (double)0.5F);
   }

   public static SimpleSoundInstance forUI(SoundEvent var0, float var1) {
      return forUI(var0, var1, 0.25F);
   }

   public static SimpleSoundInstance forUI(SoundEvent var0, float var1, float var2) {
      return new SimpleSoundInstance(var0.getLocation(), SoundSource.MASTER, var2, var1, false, 0, Attenuation.NONE, (double)0.0F, (double)0.0F, (double)0.0F, true);
   }

   public static SimpleSoundInstance forMusic(SoundEvent var0) {
      return new SimpleSoundInstance(var0.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, false, 0, Attenuation.NONE, (double)0.0F, (double)0.0F, (double)0.0F, true);
   }

   public static SimpleSoundInstance forRecord(SoundEvent var0, double var1, double var3, double var5) {
      return new SimpleSoundInstance(var0, SoundSource.RECORDS, 4.0F, 1.0F, false, 0, Attenuation.LINEAR, var1, var3, var5);
   }

   public static SimpleSoundInstance forLocalAmbience(SoundEvent var0, float var1, float var2) {
      return new SimpleSoundInstance(var0.getLocation(), SoundSource.AMBIENT, var2, var1, false, 0, Attenuation.NONE, (double)0.0F, (double)0.0F, (double)0.0F, true);
   }

   public static SimpleSoundInstance forAmbientAddition(SoundEvent var0) {
      return forLocalAmbience(var0, 1.0F, 1.0F);
   }

   public static SimpleSoundInstance forAmbientMood(SoundEvent var0, double var1, double var3, double var5) {
      return new SimpleSoundInstance(var0, SoundSource.AMBIENT, 1.0F, 1.0F, false, 0, Attenuation.LINEAR, var1, var3, var5);
   }

   public SimpleSoundInstance(SoundEvent var1, SoundSource var2, float var3, float var4, double var5, double var7, double var9) {
      this(var1, var2, var3, var4, false, 0, Attenuation.LINEAR, var5, var7, var9);
   }

   private SimpleSoundInstance(SoundEvent var1, SoundSource var2, float var3, float var4, boolean var5, int var6, SoundInstance.Attenuation var7, double var8, double var10, double var12) {
      this(var1.getLocation(), var2, var3, var4, var5, var6, var7, var8, var10, var12, false);
   }

   public SimpleSoundInstance(ResourceLocation var1, SoundSource var2, float var3, float var4, boolean var5, int var6, SoundInstance.Attenuation var7, double var8, double var10, double var12, boolean var14) {
      super(var1, var2);
      this.volume = var3;
      this.pitch = var4;
      this.x = var8;
      this.y = var10;
      this.z = var12;
      this.looping = var5;
      this.delay = var6;
      this.attenuation = var7;
      this.relative = var14;
   }
}
