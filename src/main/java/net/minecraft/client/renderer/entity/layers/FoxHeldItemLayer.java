//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
   public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> var1) {
      super(var1);
   }

   public void render(PoseStack var1, MultiBufferSource var2, int var3, Fox var4, float var5, float var6, float var7, float var8, float var9, float var10) {
      boolean var11 = var4.isSleeping();
      boolean var12 = var4.isBaby();
      var1.pushPose();
      if (var12) {
         float var13 = 0.75F;
         var1.scale(0.75F, 0.75F, 0.75F);
         var1.translate((double)0.0F, (double)0.5F, (double)0.209375F);
      }

      var1.translate((double)(((FoxModel)this.getParentModel()).head.x / 16.0F), (double)(((FoxModel)this.getParentModel()).head.y / 16.0F), (double)(((FoxModel)this.getParentModel()).head.z / 16.0F));
      float var15 = var4.getHeadRollAngle(var7);
      var1.mulPose(Vector3f.ZP.rotation(var15));
      var1.mulPose(Vector3f.YP.rotationDegrees(var9));
      var1.mulPose(Vector3f.XP.rotationDegrees(var10));
      if (var4.isBaby()) {
         if (var11) {
            var1.translate((double)0.4F, (double)0.26F, (double)0.15F);
         } else {
            var1.translate((double)0.06F, (double)0.26F, (double)-0.5F);
         }
      } else if (var11) {
         var1.translate((double)0.46F, (double)0.26F, (double)0.22F);
      } else {
         var1.translate((double)0.06F, (double)0.27F, (double)-0.5F);
      }

      var1.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (var11) {
         var1.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

      ItemStack var14 = var4.getItemBySlot(EquipmentSlot.MAINHAND);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(var4, var14, TransformType.GROUND, false, var1, var2, var3);
      var1.popPose();
   }
}
