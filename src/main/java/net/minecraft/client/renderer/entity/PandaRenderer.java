//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Panda.Gene;

public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
   private static final Map<Panda.Gene, ResourceLocation> TEXTURES = (Map)Util.make(Maps.newEnumMap(Panda.Gene.class), (var0) -> {
      var0.put(Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
      var0.put(Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
      var0.put(Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
      var0.put(Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
      var0.put(Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
      var0.put(Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
      var0.put(Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaRenderer(EntityRenderDispatcher var1) {
      super(var1, new PandaModel(9, 0.0F), 0.9F);
      this.addLayer(new PandaHoldsItemLayer(this));
   }

   public ResourceLocation getTextureLocation(Panda var1) {
      return (ResourceLocation)TEXTURES.getOrDefault(var1.getVariant(), TEXTURES.get(Gene.NORMAL));
   }

   protected void setupRotations(Panda var1, PoseStack var2, float var3, float var4, float var5) {
      super.setupRotations(var1, var2, var3, var4, var5);
      if (var1.rollCounter > 0) {
         int var6 = var1.rollCounter;
         int var7 = var6 + 1;
         float var8 = 7.0F;
         float var9 = var1.isBaby() ? 0.3F : 0.8F;
         if (var6 < 8) {
            float var11 = (float)(90 * var6) / 7.0F;
            float var12 = (float)(90 * var7) / 7.0F;
            float var10 = this.getAngle(var11, var12, var7, var5, 8.0F);
            var2.translate((double)0.0F, (double)((var9 + 0.2F) * (var10 / 90.0F)), (double)0.0F);
            var2.mulPose(Vector3f.XP.rotationDegrees(-var10));
         } else if (var6 < 16) {
            float var21 = ((float)var6 - 8.0F) / 7.0F;
            float var24 = 90.0F + 90.0F * var21;
            float var13 = 90.0F + 90.0F * ((float)var7 - 8.0F) / 7.0F;
            float var18 = this.getAngle(var24, var13, var7, var5, 16.0F);
            var2.translate((double)0.0F, (double)(var9 + 0.2F + (var9 - 0.2F) * (var18 - 90.0F) / 90.0F), (double)0.0F);
            var2.mulPose(Vector3f.XP.rotationDegrees(-var18));
         } else if ((float)var6 < 24.0F) {
            float var22 = ((float)var6 - 16.0F) / 7.0F;
            float var25 = 180.0F + 90.0F * var22;
            float var27 = 180.0F + 90.0F * ((float)var7 - 16.0F) / 7.0F;
            float var19 = this.getAngle(var25, var27, var7, var5, 24.0F);
            var2.translate((double)0.0F, (double)(var9 + var9 * (270.0F - var19) / 90.0F), (double)0.0F);
            var2.mulPose(Vector3f.XP.rotationDegrees(-var19));
         } else if (var6 < 32) {
            float var23 = ((float)var6 - 24.0F) / 7.0F;
            float var26 = 270.0F + 90.0F * var23;
            float var28 = 270.0F + 90.0F * ((float)var7 - 24.0F) / 7.0F;
            float var20 = this.getAngle(var26, var28, var7, var5, 32.0F);
            var2.translate((double)0.0F, (double)(var9 * ((360.0F - var20) / 90.0F)), (double)0.0F);
            var2.mulPose(Vector3f.XP.rotationDegrees(-var20));
         }
      }

      float var14 = var1.getSitAmount(var5);
      if (var14 > 0.0F) {
         var2.translate((double)0.0F, (double)(0.8F * var14), (double)0.0F);
         var2.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(var14, var1.xRot, var1.xRot + 90.0F)));
         var2.translate((double)0.0F, (double)(-1.0F * var14), (double)0.0F);
         if (var1.isScared()) {
            float var15 = (float)(Math.cos((double)var1.tickCount * (double)1.25F) * Math.PI * (double)0.05F);
            var2.mulPose(Vector3f.YP.rotationDegrees(var15));
            if (var1.isBaby()) {
               var2.translate((double)0.0F, (double)0.8F, (double)0.55F);
            }
         }
      }

      float var16 = var1.getLieOnBackAmount(var5);
      if (var16 > 0.0F) {
         float var17 = var1.isBaby() ? 0.5F : 1.3F;
         var2.translate((double)0.0F, (double)(var17 * var16), (double)0.0F);
         var2.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(var16, var1.xRot, var1.xRot + 180.0F)));
      }

   }

   private float getAngle(float var1, float var2, int var3, float var4, float var5) {
      return (float)var3 < var5 ? Mth.lerp(var4, var1, var2) : var1;
   }
}
