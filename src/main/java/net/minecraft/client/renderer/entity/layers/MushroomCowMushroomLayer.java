//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
   public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> var1) {
      super(var1);
   }

   public void render(PoseStack var1, MultiBufferSource var2, int var3, T var4, float var5, float var6, float var7, float var8, float var9, float var10) {
      if (!var4.isBaby() && !var4.isInvisible()) {
         BlockRenderDispatcher var11 = Minecraft.getInstance().getBlockRenderer();
         BlockState var12 = var4.getMushroomType().getBlockState();
         int var13 = LivingEntityRenderer.getOverlayCoords(var4, 0.0F);
         var1.pushPose();
         var1.translate((double)0.2F, (double)-0.35F, (double)0.5F);
         var1.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         var1.scale(-1.0F, -1.0F, 1.0F);
         var1.translate((double)-0.5F, (double)-0.5F, (double)-0.5F);
         var11.renderSingleBlock(var12, var1, var2, var3, var13);
         var1.popPose();
         var1.pushPose();
         var1.translate((double)0.2F, (double)-0.35F, (double)0.5F);
         var1.mulPose(Vector3f.YP.rotationDegrees(42.0F));
         var1.translate((double)0.1F, (double)0.0F, (double)-0.6F);
         var1.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         var1.scale(-1.0F, -1.0F, 1.0F);
         var1.translate((double)-0.5F, (double)-0.5F, (double)-0.5F);
         var11.renderSingleBlock(var12, var1, var2, var3, var13);
         var1.popPose();
         var1.pushPose();
         ((CowModel)this.getParentModel()).getHead().translateAndRotate(var1);
         var1.translate((double)0.0F, (double)-0.7F, (double)-0.2F);
         var1.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
         var1.scale(-1.0F, -1.0F, 1.0F);
         var1.translate((double)-0.5F, (double)-0.5F, (double)-0.5F);
         var11.renderSingleBlock(var12, var1, var2, var3, var13);
         var1.popPose();
      }
   }
}
