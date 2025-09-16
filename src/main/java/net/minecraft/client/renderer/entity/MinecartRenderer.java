//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
   private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
   protected final EntityModel<T> model = new MinecartModel();

   public MinecartRenderer(EntityRenderDispatcher var1) {
      super(var1);
      this.shadowRadius = 0.7F;
   }

   public void render(T var1, float var2, float var3, PoseStack var4, MultiBufferSource var5, int var6) {
      super.render(var1, var2, var3, var4, var5, var6);
      var4.pushPose();
      long var7 = (long)var1.getId() * 493286711L;
      var7 = var7 * var7 * 4392167121L + var7 * 98761L;
      float var9 = (((float)(var7 >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float var10 = (((float)(var7 >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float var11 = (((float)(var7 >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      var4.translate((double)var9, (double)var10, (double)var11);
      double var12 = Mth.lerp((double)var3, var1.xOld, var1.getX());
      double var14 = Mth.lerp((double)var3, var1.yOld, var1.getY());
      double var16 = Mth.lerp((double)var3, var1.zOld, var1.getZ());
      double var18 = (double)0.3F;
      Vec3 var20 = var1.getPos(var12, var14, var16);
      float var21 = Mth.lerp(var3, var1.xRotO, var1.xRot);
      if (var20 != null) {
         Vec3 var22 = var1.getPosOffs(var12, var14, var16, (double)0.3F);
         Vec3 var23 = var1.getPosOffs(var12, var14, var16, (double)-0.3F);
         if (var22 == null) {
            var22 = var20;
         }

         if (var23 == null) {
            var23 = var20;
         }

         var4.translate(var20.x - var12, (var22.y + var23.y) / (double)2.0F - var14, var20.z - var16);
         Vec3 var24 = var23.add(-var22.x, -var22.y, -var22.z);
         if (var24.length() != (double)0.0F) {
            var24 = var24.normalize();
            var2 = (float)(Math.atan2(var24.z, var24.x) * (double)180.0F / Math.PI);
            var21 = (float)(Math.atan(var24.y) * (double)73.0F);
         }
      }

      var4.translate((double)0.0F, (double)0.375F, (double)0.0F);
      var4.mulPose(Vector3f.YP.rotationDegrees(180.0F - var2));
      var4.mulPose(Vector3f.ZP.rotationDegrees(-var21));
      float var28 = (float)var1.getHurtTime() - var3;
      float var29 = var1.getDamage() - var3;
      if (var29 < 0.0F) {
         var29 = 0.0F;
      }

      if (var28 > 0.0F) {
         var4.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(var28) * var28 * var29 / 10.0F * (float)var1.getHurtDir()));
      }

      int var31 = var1.getDisplayOffset();
      BlockState var25 = var1.getDisplayBlockState();
      if (var25.getRenderShape() != RenderShape.INVISIBLE) {
         var4.pushPose();
         float var26 = 0.75F;
         var4.scale(0.75F, 0.75F, 0.75F);
         var4.translate((double)-0.5F, (double)((float)(var31 - 8) / 16.0F), (double)0.5F);
         var4.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         this.renderMinecartContents(var1, var3, var25, var4, var5, var6);
         var4.popPose();
      }

      var4.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(var1, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer var32 = var5.getBuffer(this.model.renderType(this.getTextureLocation(var1)));
      this.model.renderToBuffer(var4, var32, var6, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      var4.popPose();
   }

   public ResourceLocation getTextureLocation(T var1) {
      return MINECART_LOCATION;
   }

   protected void renderMinecartContents(T var1, float var2, BlockState var3, PoseStack var4, MultiBufferSource var5, int var6) {
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(var3, var4, var5, var6, OverlayTexture.NO_OVERLAY);
   }
}
