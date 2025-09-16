//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
   private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
   private int puffStateO = 3;
   private final PufferfishSmallModel<Pufferfish> small = new PufferfishSmallModel();
   private final PufferfishMidModel<Pufferfish> mid = new PufferfishMidModel();
   private final PufferfishBigModel<Pufferfish> big = new PufferfishBigModel();

   public PufferfishRenderer(EntityRenderDispatcher var1) {
      super(var1, new PufferfishBigModel(), 0.2F);
   }

   public ResourceLocation getTextureLocation(Pufferfish var1) {
      return PUFFER_LOCATION;
   }

   public void render(Pufferfish var1, float var2, float var3, PoseStack var4, MultiBufferSource var5, int var6) {
      int var7 = var1.getPuffState();
      if (var7 != this.puffStateO) {
         if (var7 == 0) {
            this.model = this.small;
         } else if (var7 == 1) {
            this.model = this.mid;
         } else {
            this.model = this.big;
         }
      }

      this.puffStateO = var7;
      this.shadowRadius = 0.1F + 0.1F * (float)var7;
      super.render(var1, var2, var3, var4, var5, var6);
   }

   protected void setupRotations(Pufferfish var1, PoseStack var2, float var3, float var4, float var5) {
      var2.translate((double)0.0F, (double)(Mth.cos(var3 * 0.05F) * 0.08F), (double)0.0F);
      super.setupRotations(var1, var2, var3, var4, var5);
   }
}
