//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SilverfishModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;

public class SilverfishRenderer extends MobRenderer<Silverfish, SilverfishModel<Silverfish>> {
   private static final ResourceLocation SILVERFISH_LOCATION = new ResourceLocation("textures/entity/silverfish.png");

   public SilverfishRenderer(EntityRenderDispatcher var1) {
      super(var1, new SilverfishModel(), 0.3F);
   }

   protected float getFlipDegrees(Silverfish var1) {
      return 180.0F;
   }

   public ResourceLocation getTextureLocation(Silverfish var1) {
      return SILVERFISH_LOCATION;
   }
}
