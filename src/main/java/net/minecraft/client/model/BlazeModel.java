

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class BlazeModel<T extends Entity> extends ListModel<T> {
   private final ModelPart[] upperBodyParts;
   private final ModelPart head = new ModelPart(this, 0, 0);
   private final ImmutableList<ModelPart> parts;

   public BlazeModel() {
      this.head.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
      this.upperBodyParts = new ModelPart[12];

      for(int var1 = 0; var1 < this.upperBodyParts.length; ++var1) {
         this.upperBodyParts[var1] = new ModelPart(this, 0, 16);
         this.upperBodyParts[var1].addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);
      }

      ImmutableList.Builder<ModelPart> var2 = ImmutableList.builder();
      var2.add(this.head);
      var2.addAll(Arrays.asList(this.upperBodyParts));
      this.parts = var2.build();
   }

   public Iterable<ModelPart> parts() {
      return this.parts;
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = var4 * (float)Math.PI * -0.1F;

      for(int var8 = 0; var8 < 4; ++var8) {
         this.upperBodyParts[var8].y = -2.0F + Mth.cos(((float)(var8 * 2) + var4) * 0.25F);
         this.upperBodyParts[var8].x = Mth.cos(var7) * 9.0F;
         this.upperBodyParts[var8].z = Mth.sin(var7) * 9.0F;
         ++var7;
      }

      var7 = ((float)Math.PI / 4F) + var4 * (float)Math.PI * 0.03F;

      for(int var11 = 4; var11 < 8; ++var11) {
         this.upperBodyParts[var11].y = 2.0F + Mth.cos(((float)(var11 * 2) + var4) * 0.25F);
         this.upperBodyParts[var11].x = Mth.cos(var7) * 7.0F;
         this.upperBodyParts[var11].z = Mth.sin(var7) * 7.0F;
         ++var7;
      }

      var7 = 0.47123894F + var4 * (float)Math.PI * -0.05F;

      for(int var12 = 8; var12 < 12; ++var12) {
         this.upperBodyParts[var12].y = 11.0F + Mth.cos(((float)var12 * 1.5F + var4) * 0.5F);
         this.upperBodyParts[var12].x = Mth.cos(var7) * 5.0F;
         this.upperBodyParts[var12].z = Mth.sin(var7) * 5.0F;
         ++var7;
      }

      this.head.yRot = var5 * ((float)Math.PI / 180F);
      this.head.xRot = var6 * ((float)Math.PI / 180F);
   }
}
