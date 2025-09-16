//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class FogRenderer {
   private static float fogRed;
   private static float fogGreen;
   private static float fogBlue;
   private static int targetBiomeFog = -1;
   private static int previousBiomeFog = -1;
   private static long biomeChangedTime = -1L;

   public static void setupColor(Camera var0, float var1, ClientLevel var2, int var3, float var4) {
      FluidState var5 = var0.getFluidInCamera();
      if (var5.is(FluidTags.WATER)) {
         long var6 = Util.getMillis();
         int var8 = var2.getBiome(new BlockPos(var0.getPosition())).getWaterFogColor();
         if (biomeChangedTime < 0L) {
            targetBiomeFog = var8;
            previousBiomeFog = var8;
            biomeChangedTime = var6;
         }

         int var9 = targetBiomeFog >> 16 & 255;
         int var10 = targetBiomeFog >> 8 & 255;
         int var11 = targetBiomeFog & 255;
         int var12 = previousBiomeFog >> 16 & 255;
         int var13 = previousBiomeFog >> 8 & 255;
         int var14 = previousBiomeFog & 255;
         float var15 = Mth.clamp((float)(var6 - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
         float var16 = Mth.lerp(var15, (float)var12, (float)var9);
         float var17 = Mth.lerp(var15, (float)var13, (float)var10);
         float var18 = Mth.lerp(var15, (float)var14, (float)var11);
         fogRed = var16 / 255.0F;
         fogGreen = var17 / 255.0F;
         fogBlue = var18 / 255.0F;
         if (targetBiomeFog != var8) {
            targetBiomeFog = var8;
            previousBiomeFog = Mth.floor(var16) << 16 | Mth.floor(var17) << 8 | Mth.floor(var18);
            biomeChangedTime = var6;
         }
      } else if (var5.is(FluidTags.LAVA)) {
         fogRed = 0.6F;
         fogGreen = 0.1F;
         fogBlue = 0.0F;
         biomeChangedTime = -1L;
      } else {
         float var19 = 0.25F + 0.75F * (float)var3 / 32.0F;
         var19 = 1.0F - (float)Math.pow((double)var19, (double)0.25F);
         Vec3 var7 = var2.getSkyColor(var0.getBlockPosition(), var1);
         float var23 = (float)var7.x;
         float var27 = (float)var7.y;
         float var31 = (float)var7.z;
         float var32 = Mth.clamp(Mth.cos(var2.getTimeOfDay(var1) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
         BiomeManager var33 = var2.getBiomeManager();
         Vec3 var34 = var0.getPosition().subtract((double)2.0F, (double)2.0F, (double)2.0F).scale((double)0.25F);
         Vec3 var35 = CubicSampler.gaussianSampleVec3(var34, (var3x, var4x, var5x) -> var2.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(var33.getNoiseBiomeAtQuart(var3x, var4x, var5x).getFogColor()), var32));
         fogRed = (float)var35.x();
         fogGreen = (float)var35.y();
         fogBlue = (float)var35.z();
         if (var3 >= 4) {
            float var36 = Mth.sin(var2.getSunAngle(var1)) > 0.0F ? -1.0F : 1.0F;
            Vector3f var38 = new Vector3f(var36, 0.0F, 0.0F);
            float var41 = var0.getLookVector().dot(var38);
            if (var41 < 0.0F) {
               var41 = 0.0F;
            }

            if (var41 > 0.0F) {
               float[] var45 = var2.effects().getSunriseColor(var2.getTimeOfDay(var1), var1);
               if (var45 != null) {
                  var41 *= var45[3];
                  fogRed = fogRed * (1.0F - var41) + var45[0] * var41;
                  fogGreen = fogGreen * (1.0F - var41) + var45[1] * var41;
                  fogBlue = fogBlue * (1.0F - var41) + var45[2] * var41;
               }
            }
         }

         fogRed += (var23 - fogRed) * var19;
         fogGreen += (var27 - fogGreen) * var19;
         fogBlue += (var31 - fogBlue) * var19;
         float var37 = var2.getRainLevel(var1);
         if (var37 > 0.0F) {
            float var39 = 1.0F - var37 * 0.5F;
            float var43 = 1.0F - var37 * 0.4F;
            fogRed *= var39;
            fogGreen *= var39;
            fogBlue *= var43;
         }

         float var40 = var2.getThunderLevel(var1);
         if (var40 > 0.0F) {
            float var44 = 1.0F - var40 * 0.5F;
            fogRed *= var44;
            fogGreen *= var44;
            fogBlue *= var44;
         }

         biomeChangedTime = -1L;
      }

      double var21 = var0.getPosition().y * var2.getLevelData().getClearColorScale();
      if (var0.getEntity() instanceof LivingEntity && ((LivingEntity)var0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
         int var24 = ((LivingEntity)var0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
         if (var24 < 20) {
            var21 *= (double)(1.0F - (float)var24 / 20.0F);
         } else {
            var21 = (double)0.0F;
         }
      }

      if (var21 < (double)1.0F && !var5.is(FluidTags.LAVA)) {
         if (var21 < (double)0.0F) {
            var21 = (double)0.0F;
         }

         var21 *= var21;
         fogRed = (float)((double)fogRed * var21);
         fogGreen = (float)((double)fogGreen * var21);
         fogBlue = (float)((double)fogBlue * var21);
      }

      if (var4 > 0.0F) {
         fogRed = fogRed * (1.0F - var4) + fogRed * 0.7F * var4;
         fogGreen = fogGreen * (1.0F - var4) + fogGreen * 0.6F * var4;
         fogBlue = fogBlue * (1.0F - var4) + fogBlue * 0.6F * var4;
      }

      if (var5.is(FluidTags.WATER)) {
         float var25 = 0.0F;
         if (var0.getEntity() instanceof LocalPlayer) {
            LocalPlayer var28 = (LocalPlayer)var0.getEntity();
            var25 = var28.getWaterVision();
         }

         float var29 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
         fogRed = fogRed * (1.0F - var25) + fogRed * var29 * var25;
         fogGreen = fogGreen * (1.0F - var25) + fogGreen * var29 * var25;
         fogBlue = fogBlue * (1.0F - var25) + fogBlue * var29 * var25;
      } else if (var0.getEntity() instanceof LivingEntity && ((LivingEntity)var0.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
         float var26 = GameRenderer.getNightVisionScale((LivingEntity)var0.getEntity(), var1);
         float var30 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
         fogRed = fogRed * (1.0F - var26) + fogRed * var30 * var26;
         fogGreen = fogGreen * (1.0F - var26) + fogGreen * var30 * var26;
         fogBlue = fogBlue * (1.0F - var26) + fogBlue * var30 * var26;
      }

      RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
   }

   public static void setupNoFog() {
      RenderSystem.fogDensity(0.0F);
      RenderSystem.fogMode(com.mojang.blaze3d.platform.GlStateManager.FogMode.EXP2);
   }

   public static void setupFog(Camera var0, FogMode var1, float var2, boolean var3) {
      FluidState var4 = var0.getFluidInCamera();
      Entity var5 = var0.getEntity();
      if (var4.is(FluidTags.WATER)) {
         float var6 = 1.0F;
         var6 = 0.05F;
         if (var5 instanceof LocalPlayer) {
            LocalPlayer var7 = (LocalPlayer)var5;
            var6 -= var7.getWaterVision() * var7.getWaterVision() * 0.03F;
            Biome var8 = var7.level.getBiome(var7.blockPosition());
            if (var8.getBiomeCategory() == BiomeCategory.SWAMP) {
               var6 += 0.005F;
            }
         }

         RenderSystem.fogDensity(var6);
         RenderSystem.fogMode(com.mojang.blaze3d.platform.GlStateManager.FogMode.EXP2);
      } else {
         float var11;
         float var12;
         if (var4.is(FluidTags.LAVA)) {
            if (var5 instanceof LivingEntity && ((LivingEntity)var5).hasEffect(MobEffects.FIRE_RESISTANCE)) {
               var11 = 0.0F;
               var12 = 3.0F;
            } else {
               var11 = 0.25F;
               var12 = 1.0F;
            }
         } else if (var5 instanceof LivingEntity && ((LivingEntity)var5).hasEffect(MobEffects.BLINDNESS)) {
            int var13 = ((LivingEntity)var5).getEffect(MobEffects.BLINDNESS).getDuration();
            float var9 = Mth.lerp(Math.min(1.0F, (float)var13 / 20.0F), var2, 5.0F);
            if (var1 == FogRenderer.FogMode.FOG_SKY) {
               var11 = 0.0F;
               var12 = var9 * 0.8F;
            } else {
               var11 = var9 * 0.25F;
               var12 = var9;
            }
         } else if (var3) {
            var11 = var2 * 0.05F;
            var12 = Math.min(var2, 192.0F) * 0.5F;
         } else if (var1 == FogRenderer.FogMode.FOG_SKY) {
            var11 = 0.0F;
            var12 = var2;
         } else {
            var11 = var2 * 0.75F;
            var12 = var2;
         }

         RenderSystem.fogStart(var11);
         RenderSystem.fogEnd(var12);
         RenderSystem.fogMode(com.mojang.blaze3d.platform.GlStateManager.FogMode.LINEAR);
         RenderSystem.setupNvFogDistance();
      }

   }

   public static void levelFogColor() {
      RenderSystem.fog(2918, fogRed, fogGreen, fogBlue, 1.0F);
   }

   public static enum FogMode {
      FOG_SKY,
      FOG_TERRAIN;

      private FogMode() {
      }
   }
}
